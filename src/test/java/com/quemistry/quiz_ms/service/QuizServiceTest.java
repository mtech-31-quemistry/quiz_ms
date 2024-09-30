package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.model.QuizStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.*;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.controller.model.SimpleQuizResponse;
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
            .totalSize(10L)
            .build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
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

    assertEquals(1, response.getMcqs().getSize());
    assertEquals(1, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(2, response.getMcqs().getTotalPages());
    assertEquals(2L, response.getMcqs().getTotalElements());
    assertEquals(IN_PROGRESS, response.getStatus());

    verify(quizRepository).findOneByStudentIdAndStatus("student1", IN_PROGRESS);
    verify(questionClient).retrieveMCQs(any(RetrieveMCQRequest.class), any(), any(), any());
    verify(quizRepository, times(1)).save(any(Quiz.class));
    verify(attemptRepository, times(2)).save(any(QuizAttempt.class));
  }

  @Test
  void createQuizWithLimitedTotalSize() {
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
    when(questionClient.retrieveMCQs(
            argThat(
                request ->
                    request.getPageSize() == 2
                        && request.getPageNumber() == 0
                        && request.getTopics().containsAll(List.of(1L, 2L))
                        && request.getSkills().containsAll(List.of(1L, 2L))),
            eq("student1"),
            eq("email1"),
            eq("roles1")))
        .thenReturn(retrieveMCQResponse);
    when(quizRepository.save(
            argThat(
                quiz ->
                    quiz.getStudentId().equals("student1")
                        && quiz.getStatus().equals(IN_PROGRESS))))
        .thenAnswer(
            invocation -> {
              Quiz quiz = invocation.getArgument(0);
              quiz.setId(1L);
              return quiz;
            });

    QuizResponse response = quizService.createQuiz(testUserContext, quizRequest);

    assertEquals(1, response.getMcqs().getSize());
    assertEquals(1, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(2, response.getMcqs().getTotalPages());
    assertEquals(2L, response.getMcqs().getTotalElements());

    assertEquals(IN_PROGRESS, response.getStatus());
    verify(attemptRepository, times(2)).save(argThat(attempt -> attempt.getQuizId().equals(1L)));
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
    long quizId = 1L;
    long mcqId1 = 2L;
    long mcqId2 = 3L;
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(
                List.of(generateMCQDto(mcqId1, "Question 1"), generateMCQDto(mcqId2, "Question 2")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(quizId, "student1")).thenReturn(Optional.of(quiz));
    when(attemptRepository.findPageByQuizId(quizId, PageRequest.of(0, 10)))
        .thenReturn(
            new PageImpl<>(
                List.of(QuizAttempt.create(quizId, mcqId1), QuizAttempt.create(quizId, mcqId2)),
                PageRequest.of(0, 10),
                2));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().containsAll(List.of(mcqId1, mcqId2))),
            any(),
            any(),
            any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(quizId, testUserContext, 0, 10);

    assertEquals(quizId, response.getId());

    assertEquals(10, response.getMcqs().getSize());
    assertEquals(2, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(1, response.getMcqs().getTotalPages());
    assertEquals(2L, response.getMcqs().getTotalElements());

    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOn());
  }

  @Test
  void getQuizByIdAndStudentIdReturnCompletedQuizWithCorrectAnswer() {
    long quizId = 1L;
    long mcqId1 = 2L;
    Quiz quiz = Quiz.builder().id(quizId).status(COMPLETED).studentId("student1").build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(mcqId1, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(quizId, "student1")).thenReturn(Optional.of(quiz));
    QuizAttempt attempt = QuizAttempt.create(quizId, mcqId1);
    attempt.setOptionNo(1);
    when(attemptRepository.findPageByQuizId(quizId, PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(attempt), PageRequest.of(0, 10), 1));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(mcqId1)), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(quizId, testUserContext, 0, 10);

    assertEquals(1L, response.getId());

    assertEquals(10, response.getMcqs().getSize());
    assertEquals(1, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(1, response.getMcqs().getTotalPages());
    assertEquals(1L, response.getMcqs().getTotalElements());

    assertEquals(COMPLETED, response.getStatus());
    assertEquals(1, response.getMcqs().getContent().getFirst().getAttemptOption());
    assertEquals(1, response.getPoints());
  }

  @Test
  void getQuizByIdAndStudentIdReturnCompletedQuizWithIncorrectAnswer() {
    Quiz quiz = Quiz.builder().id(1L).status(COMPLETED).studentId("student1").build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByIdAndStudentId(1L, "student1")).thenReturn(Optional.of(quiz));
    QuizAttempt attempt = QuizAttempt.create(1L, 1L);
    attempt.setOptionNo(2);
    when(attemptRepository.findPageByQuizId(1L, PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(attempt), PageRequest.of(0, 10), 1));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getQuiz(1L, testUserContext, 0, 10);

    assertEquals(0, response.getPoints());
  }

  @Test
  void getInProgressQuizWithFirstPage() {
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1"), generateMCQDto(2L, "Question 2")))
            .totalPages(1)
            .totalRecords(2L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(attemptRepository.findPageByQuizId(1L, PageRequest.of(0, 10)))
        .thenReturn(
            new PageImpl<>(
                List.of(QuizAttempt.create(1L, 1L), QuizAttempt.create(1L, 2L)),
                PageRequest.of(0, 10),
                2));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 10);

    assertEquals(1L, response.getId());

    assertEquals(10, response.getMcqs().getSize());
    assertEquals(2, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(1, response.getMcqs().getTotalPages());
    assertEquals(2L, response.getMcqs().getTotalElements());

    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithAttempt() {
    Date now = new Date();
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();

    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    QuizAttempt attempt = QuizAttempt.create(1L, 1L);
    attempt.setOptionNo(1);
    attempt.setAttemptTime(now);
    when(attemptRepository.findPageByQuizId(1L, PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(attempt), PageRequest.of(0, 10), 1));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 10);

    assertEquals(1L, response.getId());

    assertEquals(10, response.getMcqs().getSize());
    assertEquals(1, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(1, response.getMcqs().getTotalPages());
    assertEquals(1L, response.getMcqs().getTotalElements());

    assertEquals(IN_PROGRESS, response.getStatus());
    assertEquals(1, response.getMcqs().getContent().getFirst().getAttemptOption());
    assertEquals(now, response.getMcqs().getContent().getFirst().getAttemptOn());
  }

  @Test
  void getInProgressQuizWithFirstPageWithoutAttempt() {
    Quiz quiz = Quiz.builder().id(1L).status(IN_PROGRESS).studentId("student1").build();
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findOneByStudentIdAndStatus("student1", IN_PROGRESS))
        .thenReturn(Optional.of(quiz));
    when(attemptRepository.findPageByQuizId(1L, PageRequest.of(0, 10)))
        .thenReturn(new PageImpl<>(List.of(QuizAttempt.create(1L, 1L)), PageRequest.of(0, 10), 1));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    QuizResponse response = quizService.getInProgressQuiz(testUserContext, 0, 10);

    assertEquals(1L, response.getId());
    assertEquals(10, response.getMcqs().getSize());
    assertEquals(1, response.getMcqs().getContent().size());
    assertEquals(0, response.getMcqs().getNumber());
    assertEquals(1, response.getMcqs().getTotalPages());
    assertEquals(1L, response.getMcqs().getTotalElements());

    assertEquals(IN_PROGRESS, response.getStatus());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOption());
    assertNull(response.getMcqs().getContent().getFirst().getAttemptOn());
  }

  @Test
  void getCompletedQuizWithFirstPage() {
    Page<Quiz> quizzes =
        new PageImpl<>(
            List.of(Quiz.builder().id(1L).status(COMPLETED).studentId("student1").build()),
            PageRequest.of(0, 10),
            1);
    RetrieveMCQResponse retrieveMCQResponse =
        RetrieveMCQResponse.builder()
            .mcqs(List.of(generateMCQDto(1L, "Question 1")))
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizRepository.findPageByStudentIdAndStatus("student1", COMPLETED, PageRequest.of(0, 1)))
        .thenReturn(quizzes);
    QuizAttempt attempt = QuizAttempt.create(1L, 1L);
    attempt.setOptionNo(1);
    when(attemptRepository.findAllByQuizIdIn(argThat(argument -> argument.contains(1L))))
        .thenReturn(List.of(attempt));
    when(questionClient.retrieveMCQsByIds(any(RetrieveMCQByIdsRequest.class), any(), any(), any()))
        .thenReturn(retrieveMCQResponse);

    Page<SimpleQuizResponse> response = quizService.getCompletedQuiz(testUserContext, 0, 1);

    assertEquals(10, response.getSize());
    assertEquals(1, response.getContent().size());
    assertEquals(0, response.getNumber());
    assertEquals(1, response.getTotalPages());
    assertEquals(1L, response.getTotalElements());
    assertEquals(COMPLETED, response.getContent().getFirst().getStatus());
    assertEquals(1, response.getContent().getFirst().getPoints());
  }

  @Test
  void getCompletedQuizWithoutData() {
    Page<Quiz> quizzes = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(quizRepository.findPageByStudentIdAndStatus("student1", COMPLETED, PageRequest.of(0, 10)))
        .thenReturn(quizzes);

    Page<SimpleQuizResponse> response = quizService.getCompletedQuiz(testUserContext, 0, 10);

    assertEquals(10, response.getSize());
    assertEquals(0, response.getContent().size());
    assertEquals(0, response.getNumber());
    assertEquals(0, response.getTotalPages());
    assertEquals(0L, response.getTotalElements());

    verify(questionClient, never()).retrieveMCQsByIds(any(), any(), any(), any());
  }

  @Test
  void updateAttemptSuccess() {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "student1";
    Integer attemptOption = 1;
    Quiz quiz = Quiz.builder().id(quizId).status(IN_PROGRESS).studentId("student1").build();
    QuizAttempt attempt = QuizAttempt.builder().optionNo(null).quizId(quizId).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.of(quiz));
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
    QuizAttempt attempt = QuizAttempt.builder().optionNo(null).quizId(quizId).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.of(quiz));
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
    QuizAttempt attempt = QuizAttempt.builder().optionNo(null).quizId(quizId).mcqId(mcqId).build();

    when(quizRepository.existsByIdAndStudentId(quizId, studentId)).thenReturn(true);
    when(quizRepository.findOneByIdAndStudentId(quizId, studentId)).thenReturn(Optional.of(quiz));
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
