package com.quemistry.quiz_ms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.*;
import com.quemistry.quiz_ms.controller.model.GetQuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QuizServiceTest {

  @Mock private QuizRepository quizRepository;

  @Mock private QuestionClient questionClient;

  @InjectMocks private QuizService quizService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createQuizWithFirstPage() {
    QuizRequest quizRequest =
        QuizRequest.builder().topics(List.of(1L, 2L)).skills(List.of(1L, 2L)).pageSize(1).build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class)))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(any(Quiz.class))).thenReturn(Quiz.builder().id(1L).build());

    QuizResponse response = quizService.createQuiz("student1", quizRequest);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());

    verify(quizRepository)
        .save(
            argThat(
                quiz ->
                    quiz.getStudentId().equals("student1")
                        && quiz.getMcqIds().containsAll(List.of(1L, 2L))
                        && quiz.getStatus().equals(QuizStatus.IN_PROGRESS)));
  }

  @Test
  void getQuizByIdAndStudentIdWithQuizNotOwnedByStudent() {
    when(quizRepository.findByIdAndStudentId(1L, "student1")).thenReturn(Optional.empty());
    GetQuizRequest getQuizRequest = GetQuizRequest.builder().pageNumber(0).pageSize(10).build();

    assertThrows(
        NotFoundException.class, () -> quizService.getQuiz(1L, "student1", getQuizRequest));
  }

  @Test
  void getQuizByIdAndStudentIdWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).studentId("student1").mcqIds(List.of(1L, 2L)).build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class)))
        .thenReturn(retrieveMCQResponse);

    GetQuizRequest getQuizRequest = GetQuizRequest.builder().pageNumber(0).pageSize(1).build();
    QuizResponse response = quizService.getQuiz(1L, "student1", getQuizRequest);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
  }

  private MCQDto generateMCQDto(Long id, String stem) {
    return MCQDto.builder()
        .id(id)
        .stem(stem)
        .options(
            List.of(
                new MCQDto.OptionDto(1, "Option 1", "Explanation 1", true),
                new MCQDto.OptionDto(2, "Option 2", "Explanation 2", false),
                new MCQDto.OptionDto(3, "Option 3", "Explanation 3", false),
                new MCQDto.OptionDto(4, "Option 4", "Explanation 4", false)))
        .topics(List.of(new TopicDto(1, "Topic 1"), new TopicDto(2, "Topic 2")))
        .skills(List.of(new SkillDto(1, "Skill 1", 1), new SkillDto(2, "Skill 2", 2)))
        .status("PUBLISHED")
        .build();
  }
}
