package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.CreatingBlockedByExistingDataException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuizService {
  private final QuizRepository quizRepository;
  private final QuestionClient questionClient;

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
                .build());

    List<Long> mcqIds = retrieveMCQRequests.getMcqs().stream().map(MCQDto::getId).toList();

    quiz.addMcq(mcqIds);
    Long quizId = quizRepository.save(quiz).getId();

    return QuizResponse.builder()
        .id(quizId)
        .mcqs(retrieveMCQRequests.getMcqs().stream().limit(quizRequest.getPageSize()).toList())
        .pageNumber(0)
        .pageSize(quizRequest.getPageSize())
        .totalPages(retrieveMCQRequests.getTotalPages())
        .totalRecords(retrieveMCQRequests.getTotalRecords())
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

  private QuizResponse convertQuiz(Integer pageNumber, Integer pageSize, Optional<Quiz> quiz) {
    if (quiz.isEmpty()) {
      throw new NotFoundException("Quiz not found");
    }

    RetrieveMCQResponse mcqs =
        questionClient.retrieveMCQsByIds(
            RetrieveMCQByIdsRequest.builder().ids(quiz.get().getMcqIds()).build());

    return QuizResponse.builder()
        .id(quiz.get().getId())
        .mcqs(mcqs.getMcqs())
        .pageNumber(pageNumber)
        .pageSize(pageSize)
        .totalPages(mcqs.getTotalPages())
        .totalRecords(mcqs.getTotalRecords())
        .build();
  }
}
