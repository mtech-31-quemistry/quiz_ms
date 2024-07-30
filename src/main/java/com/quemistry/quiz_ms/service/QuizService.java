package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.MCQResponse;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.CreatingBlockedByExistingDataException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.mapper.MCQMapper;
import com.quemistry.quiz_ms.model.Attempt;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuizService {
  private final QuizRepository quizRepository;
  private final QuestionClient questionClient;
  private final MCQMapper mcqMapper = MCQMapper.INSTANCE;

  @Autowired
  public QuizService(QuizRepository quizRepository, QuestionClient questionClient) {
    this.quizRepository = quizRepository;
    this.questionClient = questionClient;
  }

  public QuizResponse createQuiz(String studentId, QuizRequest quizRequest) {
    Optional<Quiz> existingQuiz =
        quizRepository.findByStudentIdAndStatus(studentId, QuizStatus.IN_PROGRESS);
    if (existingQuiz.isPresent()) {
      throw new CreatingBlockedByExistingDataException("Quiz already in progress");
    }

    Quiz quiz = Quiz.create(studentId);

    RetrieveMCQResponse retrieveMCQRequests =
        questionClient.retrieveMCQs(
            RetrieveMCQRequest.builder()
                .topics(quizRequest.getTopics())
                .skills(quizRequest.getSkills())
                .pageNumber(0)
                .pageNumber(60)
                .build());

    List<Long> mcqIds = retrieveMCQRequests.getMcqs().stream().map(MCQDto::getId).toList();

    quiz.addAttempts(mcqIds);
    Long quizId = quizRepository.save(quiz).getId();

    List<MCQResponse> mcqResponses =
        retrieveMCQRequests.getMcqs().stream()
            .map(mcqMapper::toMCQResponse)
            .limit(quizRequest.getPageSize())
            .collect(Collectors.toList());

    Long totalRecords =
        Math.min(retrieveMCQRequests.getTotalRecords(), quizRequest.getTotalSize().intValue());
    Integer totalPages = (int) Math.ceil((double) totalRecords / quizRequest.getPageSize());

    return QuizResponse.builder()
        .id(quizId)
        .mcqs(mcqResponses)
        .pageNumber(0)
        .pageSize(quizRequest.getPageSize())
        .totalPages(totalPages)
        .totalRecords(totalRecords)
        .build();
  }

  public QuizResponse getQuiz(Long id, String studentId, Integer pageNumber, Integer pageSize) {
    Optional<Quiz> quiz = quizRepository.findByIdAndStudentId(id, studentId);

    return convertQuiz(pageNumber, pageSize, quiz);
  }

  public QuizResponse getInProgressQuiz(String studentId, Integer pageNumber, Integer pageSize) {
    Optional<Quiz> quiz =
        quizRepository.findByStudentIdAndStatus(studentId, QuizStatus.IN_PROGRESS);

    return convertQuiz(pageNumber, pageSize, quiz);
  }

  private QuizResponse convertQuiz(
      Integer pageNumber, Integer pageSize, Optional<Quiz> quizResponse) {
    if (quizResponse.isEmpty()) {
      throw new NotFoundException("Quiz not found");
    }
    Quiz quiz = quizResponse.get();

    RetrieveMCQResponse mcqs =
        questionClient.retrieveMCQsByIds(
            RetrieveMCQByIdsRequest.builder()
                .ids(quiz.getAttempts().stream().map(Attempt::getMcqId).toList())
                .pageNumber(pageNumber)
                .pageSize(pageSize)
                .build());

    List<MCQResponse> mcqResponses =
        mcqs.getMcqs().stream()
            .map(
                mcq -> {
                  MCQResponse response = mcqMapper.toMCQResponse(mcq);
                  Attempt attempt =
                      quiz.getAttempts().stream()
                          .filter(it -> it.getMcqId().equals(mcq.getId()))
                          .findFirst()
                          .orElse(null);
                  if (attempt != null) {
                    response.setAttemptOption(attempt.getOptionNo());
                    response.setAttemptOn(attempt.getAttemptTime());
                  }
                  return response;
                })
            .collect(Collectors.toList());

    return QuizResponse.builder()
        .id(quiz.getId())
        .mcqs(mcqResponses)
        .pageNumber(pageNumber)
        .pageSize(pageSize)
        .totalPages(mcqs.getTotalPages())
        .totalRecords(mcqs.getTotalRecords())
        .build();
  }
}
