package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.mapper.MCQMapper.INSTANCE;
import static com.quemistry.quiz_ms.model.QuizStatus.COMPLETED;
import static com.quemistry.quiz_ms.model.QuizStatus.IN_PROGRESS;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.AttemptAlreadyExistsException;
import com.quemistry.quiz_ms.exception.InProgressQuizAlreadyExistsException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.mapper.MCQMapper;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizAttempt;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.repository.QuizAttemptRepository;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CacheConfig(cacheNames = "quiz")
@Scope(proxyMode = TARGET_CLASS)
public class QuizService {
  private final QuizRepository quizRepository;
  private final QuizAttemptRepository attemptRepository;
  private final QuestionClient questionClient;
  private final MCQMapper mcqMapper = INSTANCE;
  private final QuizService self;

  @Autowired
  public QuizService(
      QuizRepository quizRepository,
      QuizAttemptRepository attemptRepository,
      QuestionClient questionClient,
      QuizService self) {
    this.quizRepository = quizRepository;
    this.attemptRepository = attemptRepository;
    this.questionClient = questionClient;
    this.self = self;
  }

  public QuizResponse createQuiz(UserContext userContext, QuizRequest quizRequest) {
    Optional<Quiz> existingQuiz =
        quizRepository.findOneByStudentIdAndStatus(userContext.getUserId(), IN_PROGRESS);
    if (existingQuiz.isPresent()) {
      throw new InProgressQuizAlreadyExistsException();
    }

    Quiz quiz = Quiz.create(userContext.getUserId());
    long quizId = quizRepository.save(quiz).getId();

    List<Long> previousQuizIds =
        quizRepository.findAllByStudentId(userContext.getUserId()).stream()
            .map(Quiz::getId)
            .toList();
    List<Long> excludeMcqIds =
        previousQuizIds.isEmpty()
            ? List.of()
            : attemptRepository.findAllByQuizIdIn(previousQuizIds).stream()
                .filter(QuizAttempt::isCorrect)
                .map(QuizAttempt::getMcqId)
                .toList();
    RetrieveMCQResponse retrieveMCQRequests =
        getMCQByQuestionClient(quizRequest, excludeMcqIds, userContext);

    if (retrieveMCQRequests.getMcqs().isEmpty()) {
      quiz.complete();
      quizRepository.save(quiz);
    } else {
      List<Long> mcqIds = retrieveMCQRequests.getMcqs().stream().map(MCQDto::getId).toList();
      mcqIds.parallelStream()
          .forEach(mcqId -> attemptRepository.save(QuizAttempt.create(quizId, mcqId)));
    }

    Page<MCQResponse> mcqs =
        new PageImpl<>(
            retrieveMCQRequests.getMcqs().stream()
                .map(mcqMapper::toMCQResponse)
                .limit(quizRequest.getPageSize())
                .collect(Collectors.toList()),
            PageRequest.of(0, quizRequest.getPageSize()),
            retrieveMCQRequests.getTotalRecords());

    return QuizResponse.builder().id(quizId).mcqs(mcqs).status(quiz.getStatus()).build();
  }

  public QuizResponse getQuiz(
      Long id, UserContext userContext, Integer pageNumber, Integer pageSize) {
    Optional<Quiz> quiz = quizRepository.findOneByIdAndStudentId(id, userContext.getUserId());
    return convertQuiz(pageNumber, pageSize, quiz, userContext);
  }

  public QuizResponse getInProgressQuiz(
      UserContext userContext, Integer pageNumber, Integer pageSize) {
    Optional<Quiz> quiz =
        quizRepository.findOneByStudentIdAndStatus(userContext.getUserId(), IN_PROGRESS);

    return convertQuiz(pageNumber, pageSize, quiz, userContext);
  }

  public Page<SimpleQuizResponse> getCompletedQuiz(
      UserContext userContext, Integer pageNumber, Integer pageSize) {
    return getQuizDetail(
        userContext,
        quizRepository.findPageByStudentIdAndStatus(
            userContext.getUserId(), COMPLETED, PageRequest.of(pageNumber, pageSize)));
  }

  public void updateAttempt(
      Long quizId, Long mcqId, String studentId, Integer attemptOption, UserContext userContext) {
    if (!quizRepository.existsByIdAndStudentId(quizId, studentId)) {
      throw new NotFoundException("Quiz not found");
    }

    QuizAttempt attempt =
        attemptRepository
            .findByQuizIdAndMcqId(quizId, mcqId)
            .orElseThrow(() -> new NotFoundException("Attempt not found"));

    if (attempt.getOptionNo() != null) {
      throw new AttemptAlreadyExistsException();
    }

    int correctOptionNo = getCorrectOptionNo(quizId, mcqId, userContext);

    attempt.updateAttempt(attemptOption, correctOptionNo);
    attemptRepository.save(attempt);

    if (!attemptRepository.existsByQuizIdAndOptionNoIsNull(quizId)) {
      Quiz quiz =
          quizRepository
              .findOneByIdAndStudentId(quizId, studentId)
              .orElseThrow(() -> new NotFoundException("Quiz not found"));
      quiz.complete();
      quizRepository.save(quiz);
    }
  }

  public void abandonQuiz(Long id, String studentId) {
    Optional<Quiz> quiz = quizRepository.findOneByIdAndStudentId(id, studentId);
    if (quiz.isEmpty()) {
      throw new NotFoundException("Quiz not found");
    }

    Quiz quizEntity = quiz.get();
    if (quizEntity.getStatus() != IN_PROGRESS) {
      throw new NotFoundException("Quiz not in progress");
    }

    quizEntity.abandon();
    quizRepository.save(quizEntity);
  }

  @Cacheable(value = "quiz_mcqs", key = "#quizId")
  public RetrieveMCQResponse getMCQByQuestionClient(Long quizId, UserContext userContext) {
    List<Long> attemptIds =
        attemptRepository.findAllByQuizId(quizId).stream().map(QuizAttempt::getMcqId).toList();
    return getRetrieveMCQResponse(userContext, attemptIds);
  }

  private int getCorrectOptionNo(Long quizId, Long mcqId, UserContext userContext) {
    return self.getMCQByQuestionClient(quizId, userContext).getMcqs().stream()
        .filter(mcq -> mcq.getId().equals(mcqId))
        .flatMap(mcq -> mcq.getOptions().stream())
        .filter(MCQDto.OptionDto::getIsAnswer)
        .map(MCQDto.OptionDto::getNo)
        .findFirst()
        .orElse(-1);
  }

  private Page<SimpleQuizResponse> getQuizDetail(UserContext userContext, Page<Quiz> quizzes) {
    List<QuizAttempt> attempts =
        attemptRepository.findAllByQuizIdIn(quizzes.stream().map(Quiz::getId).toList());
    List<Long> attemptIds =
        attempts.stream().map(QuizAttempt::getMcqId).collect(Collectors.toList());
    List<MCQDto> mcqs =
        quizzes.isEmpty()
            ? List.of()
            : questionClient
                .retrieveMCQsByIds(
                    RetrieveMCQByIdsRequest.builder()
                        .ids(attemptIds)
                        .pageNumber(0)
                        .pageSize(quizzes.getNumberOfElements() * 60)
                        .build(),
                    userContext.getUserId(),
                    userContext.getUserEmail(),
                    userContext.getUserRoles())
                .getMcqs();
    return quizzes.map(
        quiz -> {
          List<QuizAttempt> quizAttempts =
              attempts.stream()
                  .filter(attempt -> attempt.getQuizId().equals(quiz.getId()))
                  .collect(Collectors.toList());
          List<MCQResponse> quizMcqs = getMcqResponses(mcqs, quizAttempts);
          return SimpleQuizResponse.builder()
              .id(quiz.getId())
              .status(quiz.getStatus())
              .mcqs(quizMcqs)
              .points(calculatePoints(quizAttempts))
              .build();
        });
  }

  private RetrieveMCQResponse getMCQByQuestionClient(
      QuizRequest quizRequest, List<Long> excludeMcqIds, UserContext userContext) {
    return questionClient.retrieveMCQs(
        RetrieveMCQRequest.builder()
            .topics(quizRequest.getTopics())
            .skills(quizRequest.getSkills())
            .excludeIds(excludeMcqIds)
            .pageNumber(0)
            .pageSize(Math.toIntExact(quizRequest.getTotalSize()))
            .build(),
        userContext.getUserId(),
        userContext.getUserEmail(),
        userContext.getUserRoles());
  }

  private RetrieveMCQResponse getRetrieveMCQResponse(
      UserContext userContext, List<Long> attemptIds) {
    return questionClient.retrieveMCQsByIds(
        RetrieveMCQByIdsRequest.builder().ids(attemptIds).pageNumber(0).pageSize(60).build(),
        userContext.getUserId(),
        userContext.getUserEmail(),
        userContext.getUserRoles());
  }

  private QuizResponse convertQuiz(
      Integer pageNumber, Integer pageSize, Optional<Quiz> quizResponse, UserContext userContext) {
    if (quizResponse.isEmpty()) {
      throw new NotFoundException("Quiz not found");
    }
    Quiz quiz = quizResponse.get();
    Page<QuizAttempt> attempts =
        attemptRepository.findPageByQuizId(quiz.getId(), PageRequest.of(pageNumber, pageSize));
    List<Long> attemptIds = attempts.stream().map(QuizAttempt::getMcqId).toList();

    RetrieveMCQResponse mcqs = getRetrieveMCQResponse(userContext, attemptIds);

    Integer points =
        (quiz.getStatus() == COMPLETED)
            ? calculatePoints(attemptRepository.findAllByQuizId(quiz.getId()))
            : null;

    return QuizResponse.builder()
        .id(quiz.getId())
        .mcqs(getPageMcqResponses(mcqs.getMcqs(), attempts))
        .status(quiz.getStatus())
        .points(points)
        .build();
  }

  private Page<MCQResponse> getPageMcqResponses(List<MCQDto> mcqs, Page<QuizAttempt> attempts) {
    return attempts.map(getQuizAttemptMCQResponse(mcqs));
  }

  private List<MCQResponse> getMcqResponses(List<MCQDto> mcqs, List<QuizAttempt> attempts) {
    return attempts.stream().map(getQuizAttemptMCQResponse(mcqs)).collect(Collectors.toList());
  }

  private static Function<QuizAttempt, MCQResponse> getQuizAttemptMCQResponse(List<MCQDto> mcqs) {
    return attempt ->
        MCQResponse.from(
            attempt,
            mcqs.stream()
                .filter(mcq -> mcq.getId().equals(attempt.getMcqId()))
                .findFirst()
                .orElse(null));
  }

  private int calculatePoints(List<QuizAttempt> attempts) {
    return (int) attempts.stream().filter(QuizAttempt::isCorrect).count();
  }
}
