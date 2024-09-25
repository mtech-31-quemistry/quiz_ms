package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.mapper.MCQMapper.INSTANCE;
import static com.quemistry.quiz_ms.model.QuizStatus.COMPLETED;
import static com.quemistry.quiz_ms.model.QuizStatus.IN_PROGRESS;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuizService {
  private final QuizRepository quizRepository;
  private final QuizAttemptRepository attemptRepository;
  private final QuestionClient questionClient;
  private final MCQMapper mcqMapper = INSTANCE;
  private RetrieveMCQResponse mcq;

  @Autowired
  public QuizService(
      QuizRepository quizRepository,
      QuizAttemptRepository attemptRepository,
      QuestionClient questionClient) {
    this.quizRepository = quizRepository;
    this.attemptRepository = attemptRepository;
    this.questionClient = questionClient;
  }

  public QuizResponse createQuiz(UserContext userContext, QuizRequest quizRequest) {
    Optional<Quiz> existingQuiz =
        quizRepository.findOneByStudentIdAndStatus(userContext.getUserId(), IN_PROGRESS);
    if (existingQuiz.isPresent()) {
      throw new InProgressQuizAlreadyExistsException();
    }

    Quiz quiz = Quiz.create(userContext.getUserId());
    quiz = quizRepository.save(quiz);
    long quizId = quiz.getId();

    RetrieveMCQResponse retrieveMCQRequests =
        questionClient.retrieveMCQs(
            RetrieveMCQRequest.builder()
                .topics(quizRequest.getTopics())
                .skills(quizRequest.getSkills())
                .pageNumber(0)
                .pageSize(60)
                .build(),
            userContext.getUserId(),
            userContext.getUserEmail(),
            userContext.getUserRoles());

    List<Long> mcqIds = retrieveMCQRequests.getMcqs().stream().map(MCQDto::getId).toList();

    mcqIds.parallelStream()
        .forEach(mcqId -> attemptRepository.save(QuizAttempt.create(quizId, mcqId)));

    long totalRecords = Math.min(retrieveMCQRequests.getTotalRecords(), quizRequest.getTotalSize());
    Page<MCQResponse> mcqs =
        new PageImpl<>(
            retrieveMCQRequests.getMcqs().stream()
                .map(mcqMapper::toMCQResponse)
                .limit(quizRequest.getPageSize())
                .collect(Collectors.toList()),
            PageRequest.of(0, quizRequest.getPageSize()),
            totalRecords);

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

  public void updateAttempt(Long quizId, Long mcqId, String studentId, Integer attemptOption) {
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

    attempt.updateAttempt(attemptOption);
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

  private Page<SimpleQuizResponse> getQuizDetail(UserContext userContext, Page<Quiz> quizzes) {
    List<QuizAttempt> attempts =
        attemptRepository.findAllByQuizIdIn(quizzes.stream().map(Quiz::getId).toList());
    List<MCQDto> mcqs =
        quizzes.isEmpty()
            ? List.of()
            : questionClient
                .retrieveMCQsByIds(
                    RetrieveMCQByIdsRequest.builder()
                        .ids(
                            attempts.stream()
                                .map(QuizAttempt::getMcqId)
                                .collect(Collectors.toList()))
                        .pageNumber(0)
                        .pageSize(quizzes.getNumberOfElements() * 60)
                        .build(),
                    userContext.getUserId(),
                    userContext.getUserEmail(),
                    userContext.getUserRoles())
                .getMcqs();
    return quizzes.map(
        quiz -> {
          List<MCQResponse> quizMcqs =
              getMcqResponses(
                  mcqs,
                  attempts.stream()
                      .filter(attempt -> attempt.getQuizId().equals(quiz.getId()))
                      .collect(Collectors.toList()));
          return SimpleQuizResponse.builder()
              .id(quiz.getId())
              .status(quiz.getStatus())
              .mcqs(quizMcqs)
              .points(calculatePoints(quizMcqs))
              .build();
        });
  }

  private QuizResponse convertQuiz(
      Integer pageNumber, Integer pageSize, Optional<Quiz> quizResponse, UserContext userContext) {
    if (quizResponse.isEmpty()) {
      throw new NotFoundException("Quiz not found");
    }
    Quiz quiz = quizResponse.get();
    Page<QuizAttempt> attempts =
        attemptRepository.findPageByQuizId(quiz.getId(), PageRequest.of(pageNumber, pageSize));

    RetrieveMCQResponse mcqs =
        questionClient.retrieveMCQsByIds(
            RetrieveMCQByIdsRequest.builder()
                .ids(attempts.stream().map(QuizAttempt::getMcqId).toList())
                .pageNumber(0)
                .pageSize(60)
                .build(),
            userContext.getUserId(),
            userContext.getUserEmail(),
            userContext.getUserRoles());

    Integer points =
        (quiz.getStatus() == COMPLETED)
            ? calculatePoints(getPageMcqResponses(mcqs.getMcqs(), attempts).toList())
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

  private int calculatePoints(List<MCQResponse> mcqs) {
    return (int)
        mcqs.stream()
            .filter(mcq -> mcq.getAttemptOption() != null)
            .filter(
                mcq ->
                    mcq.getOptions().stream()
                        .anyMatch(
                            option ->
                                option.getNo().equals(mcq.getAttemptOption())
                                    && option.getIsAnswer()))
            .count();
  }
}
