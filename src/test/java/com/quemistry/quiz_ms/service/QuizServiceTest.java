package com.quemistry.quiz_ms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.*;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.AttemptAlreadyExistsException;
import com.quemistry.quiz_ms.exception.InProgressQuizAlreadyExistsException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.model.Attempt;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import com.quemistry.quiz_ms.repository.AttemptRepository;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class QuizServiceTest {

  @Mock private QuizRepository quizRepository;
  @Mock private AttemptRepository attemptRepository;

  @Mock private QuestionClient questionClient;

  @InjectMocks private QuizService quizService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createQuizWithFirstPage() {
    QuizRequest quizRequest =
        QuizRequest.builder()
            .topics(List.of(1L, 2L))
            .skills(List.of(1L, 2L))
            .pageSize(1)
            .totalSize(2L)
            .build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
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
    assertEquals(1L, response.getTotalRecords());

    verify(quizRepository)
        .save(
            argThat(
                quiz ->
                    quiz.getStudentId().equals("student1")
                        && quiz.getAttempts().stream().map(Attempt::getMcqId).toList().contains(1L)
                        && quiz.getStatus().equals(QuizStatus.IN_PROGRESS)));
  }

  @Test
  void createQuizFailedWithExistingInProgressQuiz() {
    QuizRequest quizRequest =
        QuizRequest.builder()
            .topics(List.of(1L, 2L))
            .skills(List.of(1L, 2L))
            .pageSize(1)
            .totalSize(2L)
            .build();

    when(quizRepository.findByStudentIdAndStatus("student1", QuizStatus.IN_PROGRESS))
        .thenReturn(Optional.of(Quiz.builder().id(1L).build()));

    assertThrows(
        InProgressQuizAlreadyExistsException.class,
        () -> quizService.createQuiz("student1", quizRequest));
  }

  @Test
  void createQuizWithTotalPagesAndRecords() {
    QuizRequest quizRequest =
        QuizRequest.builder()
            .topics(List.of(1L, 2L))
            .skills(List.of(1L, 2L))
            .pageSize(1)
            .totalSize(3L)
            .build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(
                List.of(
                    generateMCQDto(1L, "Question 1"),
                    generateMCQDto(2L, "Question 2"),
                    generateMCQDto(3L, "Question 3")))
            .totalPages(2)
            .totalRecords(3L)
            .build();

    when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class)))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(any(Quiz.class))).thenReturn(Quiz.builder().id(1L).build());

    QuizResponse response = quizService.createQuiz("student1", quizRequest);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(3, response.getTotalPages());
    assertEquals(3L, response.getTotalRecords());

    verify(quizRepository)
        .save(
            argThat(
                quiz ->
                    quiz.getStudentId().equals("student1")
                        && quiz.getAttempts().stream()
                            .map(Attempt::getMcqId)
                            .toList()
                            .containsAll(List.of(1L, 2L, 3L))
                        && quiz.getStatus().equals(QuizStatus.IN_PROGRESS)));
  }

  @Test
  void createQuizWithLimitedTotalSize() {
    QuizRequest quizRequest =
        QuizRequest.builder()
            .topics(List.of(1L, 2L))
            .skills(List.of(1L, 2L))
            .pageSize(1)
            .totalSize(1L)
            .build();

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
    assertEquals(1L, response.getTotalRecords());

    verify(quizRepository)
        .save(
            argThat(
                quiz ->
                    quiz.getStudentId().equals("student1")
                        && quiz.getAttempts().stream()
                            .map(Attempt::getMcqId)
                            .toList()
                            .containsAll(List.of(1L, 2L))
                        && quiz.getStatus().equals(QuizStatus.IN_PROGRESS)));
  }

  @Test
  void getQuizByIdAndStudentIdWithQuizNotOwnedByStudent() {
    when(quizRepository.findByIdAndStudentId(1L, "student1")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> quizService.getQuiz(1L, "student1", 0, 10));
  }

  @Test
  void getQuizByIdAndStudentIdWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).studentId("student1").build();
    quiz.setAttempts(List.of(Attempt.create(quiz, 1L), Attempt.create(quiz, 2L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class)))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(1L, "student1", 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).studentId("student1").build();
    quiz.setAttempts(List.of(Attempt.create(quiz, 1L), Attempt.create(quiz, 2L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findByStudentIdAndStatus("student1", QuizStatus.IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class)))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz("student1", 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithAttempt() {
    Date now = new Date();
    Quiz quiz = Quiz.builder().id(1L).studentId("student1").build();
    quiz.setAttempts(
        List.of(Attempt.builder().quiz(quiz).mcqId(1L).optionNo(1).attemptTime(now).build()));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findByStudentIdAndStatus("student1", QuizStatus.IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class)))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz("student1", 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(1, response.getMcqs().getFirst().getAttemptOption());
    assertEquals(now, response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithoutAttempt() {
    Quiz quiz = Quiz.builder().id(1L).studentId("student1").build();
    quiz.setAttempts(List.of(Attempt.create(quiz, 1L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findByStudentIdAndStatus("student1", QuizStatus.IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class)))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz("student1", 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void updateAttemptSuccess() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    Attempt attempt = new Attempt();
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));

    quizService.updateAttempt(quizId, mcqId, studentId, attemptOption);

    assertEquals(attemptOption, attempt.getOptionNo());
    verify(attemptRepository).save(attempt);
  }

  @Test
  void updateAttemptQuizNotFound() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(false);

    assertThrows(
        NotFoundException.class,
        () -> quizService.updateAttempt(quizId, mcqId, studentId, attemptOption));
  }

  @Test
  void updateAttemptAttemptNotFound() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> quizService.updateAttempt(quizId, mcqId, studentId, attemptOption));
  }

  @Test
  void updateAttemptAttemptAlreadyExists() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    Attempt attempt = new Attempt();
    attempt.setOptionNo(1);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));

    assertThrows(
        AttemptAlreadyExistsException.class,
        () -> quizService.updateAttempt(quizId, mcqId, studentId, attemptOption));
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
