package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.model.QuizStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.*;
import com.quemistry.quiz_ms.controller.model.QuizListResponse;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.AttemptAlreadyExistsException;
import com.quemistry.quiz_ms.exception.InProgressQuizAlreadyExistsException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizAttempt;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.repository.QuizAttemptRepository;
import com.quemistry.quiz_ms.repository.QuizRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class QuizServiceTest {

  @Mock private QuizRepository quizRepository;
  @Mock private QuizAttemptRepository attemptRepository;

  @Mock private QuestionClient questionClient;

  @InjectMocks private QuizService quizService;

  private final UserContext testUserContext = new UserContext("student1", "email1", "roles1");

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
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.empty());
    when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(any(Quiz.class)))
        .thenAnswer(
            invocation -> {
              Quiz quiz = invocation.getArgument(0);
              if (quiz.getId() == null) {
                quiz.setId(1L); // Simulate the generation of an ID
              }
              return quiz;
            });

    QuizResponse response = quizService.createQuiz(testUserContext, quizRequest);

    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(2, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());

    verify(quizRepository).findOneByStudentIdAndStatus("student1", IN_PROGRESS);
    verify(questionClient).retrieveMCQs(any(RetrieveMCQRequest.class), any(), any(), any());
    verify(quizRepository, times(2)).save(any(Quiz.class));
  }

  @Test
  void createQuizWithTotalPagesAndRecords() {
    QuizRequest quizRequest =
        QuizRequest.builder()
            .topics(List.of(1L, 2L))
            .skills(List.of(1L, 2L))
            .pageSize(1)
            .totalSize(2L)
            .build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
            .totalPages(2)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.empty());
    when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

    QuizResponse response = quizService.createQuiz(testUserContext, quizRequest);

    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(2, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
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
            .totalPages(2)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.empty());
    when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> invocation.getArgument(0));

    QuizResponse response = quizService.createQuiz(testUserContext, quizRequest);

    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
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

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(Quiz.builder().id(1L).build()));

    assertThrows(
        InProgressQuizAlreadyExistsException.class,
        () -> quizService.createQuiz(testUserContext, quizRequest));
  }

  @Test
  void getQuizByIdAndStudentIdWithQuizNotOwnedByStudent() {
    when(quizRepository.findOneByIdAndStudentId(1L, "student1")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> quizService.getQuiz(1L, testUserContext, 0, 10));
  }

  @Test
  void getQuizByIdAndStudentIdWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();
    quiz.setAttempts(List.of(QuizAttempt.create(quiz, 1L), QuizAttempt.create(quiz, 2L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(1L, testUserContext, 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(2, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getQuizByIdAndStudentIdReturnCompletedQuizWithCorrectAnswer() {
    Quiz quiz = Quiz.builder().id(1L).status(COMPLETED).studentId("student1").build();
    quiz.setAttempts(List.of(QuizAttempt.builder().quiz(quiz).mcqId(1L).optionNo(1).build()));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(1L, testUserContext, 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(COMPLETED, response.getStatus());
    assertEquals(1, response.getMcqs().getFirst().getAttemptOption());
    assertEquals(1, response.getPoints());
  }

  @Test
  void getQuizByIdAndStudentIdReturnCompletedQuizWithIncorrectAnswer() {
    Quiz quiz = Quiz.builder().id(1L).status(COMPLETED).studentId("student1").build();
    quiz.setAttempts(List.of(QuizAttempt.builder().quiz(quiz).mcqId(1L).optionNo(2).build()));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(1L, testUserContext, 0, 1);

    assertEquals(0, response.getPoints());
  }

  @Test
  void getInProgressQuizWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();
    quiz.setAttempts(List.of(QuizAttempt.create(quiz, 1L), QuizAttempt.create(quiz, 2L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(2, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(2L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithAttempt() {
    Date now = new Date();
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();
    quiz.setAttempts(
        List.of(QuizAttempt.builder().quiz(quiz).mcqId(1L).optionNo(1).attemptTime(now).build()));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
    assertEquals(1, response.getMcqs().getFirst().getAttemptOption());
    assertEquals(now, response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithoutAttempt() {
    Quiz quiz =
        Quiz.builder().id(1L).status(IN_PROGRESS).attempts(List.of()).studentId("student1").build();
    quiz.setAttempts(List.of(QuizAttempt.create(quiz, 1L)));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 1);

    assertEquals(1L, response.getId());
    assertEquals(1, response.getMcqs().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getFirst().getAttemptOn());
  }

  @Test
  void getCompletedQuizWithFirstPage() {

    Page<Quiz> quizzes =
        new PageImpl<>(
            List.of(
                Quiz.builder()
                    .id(1L)
                    .status(COMPLETED)
                    .attempts(List.of(QuizAttempt.builder().mcqId(1L).optionNo(1).build()))
                    .studentId("student1")
                    .build()));
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findPageByStudentIdAndStatus("student1", COMPLETED, PageRequest.of(0, 1)))
        .thenReturn(quizzes);
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizListResponse response = quizService.getCompletedQuiz(testUserContext, 0, 1);

    assertEquals(1, response.getQuizzes().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalRecords());
    assertEquals(COMPLETED, response.getQuizzes().getFirst().getStatus());
    assertEquals(1, response.getQuizzes().getFirst().getPoints());
  }

  @Test
  void getCompletedQuizWithoutData() {
    Page<Quiz> quizzes = new PageImpl<>(List.of());
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder().mcqs(List.of()).totalPages(1).totalRecords(1L).build();

    when(quizRepository.findPageByStudentIdAndStatus("student1", COMPLETED, PageRequest.of(0, 1)))
        .thenReturn(quizzes);

    QuizListResponse response = quizService.getCompletedQuiz(testUserContext, 0, 1);

    assertEquals(0, response.getQuizzes().size());
    assertEquals(0, response.getPageNumber());
    assertEquals(1, response.getPageSize());
    assertEquals(1, response.getTotalPages());
    assertEquals(0L, response.getTotalRecords());

    verify(questionClient, never()).retrieveMCQsByIds(any(), any(), any(), any());
  }

  @Test
  void updateAttemptSuccess() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();
    QuizAttempt attempt =
        QuizAttempt.builder().optionNo(null).quizId(quizId).quiz(quiz).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));

    quizService.updateAttempt(quizId, mcqId, studentId, attemptOption);

    assertEquals(attemptOption, attempt.getOptionNo());
    verify(attemptRepository).save(attempt);
  }

  @Test
  void updateAttemptSuccessWithNotAnsweredQuestion() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();
    QuizAttempt attempt =
        QuizAttempt.builder().optionNo(null).quizId(quizId).quiz(quiz).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));
    when(attemptRepository.existsByQuizIdAndOptionNoIsNull(quizId)).thenReturn(true);

    quizService.updateAttempt(quizId, mcqId, studentId, attemptOption);

    assertEquals(attemptOption, attempt.getOptionNo());
    verify(attemptRepository).save(attempt);
    verify(quizRepository, never()).save(quiz);
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
    QuizAttempt attempt = new QuizAttempt();
    attempt.setOptionNo(1);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));

    assertThrows(
        AttemptAlreadyExistsException.class,
        () -> quizService.updateAttempt(quizId, mcqId, studentId, attemptOption));
  }

  @Test
  void updateAttemptCompleteQuiz() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();
    QuizAttempt attempt =
        QuizAttempt.builder().optionNo(null).quizId(quizId).quiz(quiz).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(attemptRepository.findByQuizIdAndMcqId(quizId, mcqId)).thenReturn(Optional.of(attempt));
    when(attemptRepository.existsByQuizIdAndOptionNoIsNull(quizId)).thenReturn(false);

    quizService.updateAttempt(quizId, mcqId, studentId, attemptOption);

    assertEquals(attemptOption, attempt.getOptionNo());
    verify(attemptRepository).save(attempt);
    verify(quizRepository).save(quiz);
  }

  @Test
  void abandonQuizSuccess() {
    Long quizId = 1L;
    String studentId = "student1";
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();

    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.of(quiz));

    quizService.abandonQuiz(quizId, studentId);

    assertEquals(quiz.getStatus(), ABANDONED);
    verify(quizRepository).save(quiz);
  }

  @Test
  void abandonQuizQuizNotFound() {
    Long quizId = 1L;
    String studentId = "student1";

    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> quizService.abandonQuiz(quizId, studentId));
  }

  @Test
  void abandonQuizQuizNotInProgress() {
    Long quizId = 1L;
    String studentId = "student1";
    Quiz quiz = Quiz.builder().id(quizId).status(COMPLETED).studentId("student1").build();

    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.of(quiz));

    assertThrows(NotFoundException.class, () -> quizService.abandonQuiz(quizId, studentId));
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
