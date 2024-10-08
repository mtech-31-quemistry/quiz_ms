package com.quemistry.quiz_ms.controller;

import static com.quemistry.quiz_ms.fixture.TestFixture.studentContext;
import static com.quemistry.quiz_ms.fixture.TestFixture.tutorContext;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.AttemptAlreadyExistsException;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.model.QuizStatus;
import com.quemistry.quiz_ms.service.QuizService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class QuizControllerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @Mock private QuizService quizService;

  @InjectMocks private QuizController quizController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(quizController)
            .setControllerAdvice(new ExceptionAdvisor())
            .build();
  }

  @Test
  void createQuiz() throws Exception {
    Page<MCQResponse> mcqResponses =
        new PageImpl<>(
            List.of(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build()),
            PageRequest.of(0, 1),
            1);

    QuizResponse quizResponse = QuizResponse.builder().id(1L).mcqs(mcqResponses).build();

    QuizRequest quizRequest = QuizRequest.builder().pageSize(1).totalSize(1L).build();

    String quizRequestJson = objectMapper.writeValueAsString(quizRequest);
    when(quizService.createQuiz(studentContext, quizRequest)).thenReturn(quizResponse);

    mockMvc
        .perform(
            post("/v1/quizzes")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(quizRequestJson))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).createQuiz(studentContext, quizRequest);
  }

  @Test
  void getQuiz() throws Exception {
    Page<MCQResponse> mcqResponses =
        new PageImpl<>(
            List.of(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build()),
            PageRequest.of(0, 1),
            1);

    QuizResponse quizResponse = QuizResponse.builder().id(1L).mcqs(mcqResponses).build();

    when(quizService.getQuiz(1L, tutorContext, 0, 1)).thenReturn(quizResponse);

    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).getQuiz(1L, tutorContext, 0, 1);
  }

  @Test
  void getQuizNotFound() throws Exception {
    when(quizService.getQuiz(1L, tutorContext, 0, 1))
        .thenThrow(new NotFoundException("Quiz not found"));

    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().json("{\"message\":\"Quiz not found\"}"));

    verify(quizService).getQuiz(1L, tutorContext, 0, 1);
  }

  @Test
  void getInProgressQuiz() throws Exception {
    Page<MCQResponse> mcqResponses =
        new PageImpl<>(
            List.of(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build()),
            PageRequest.of(0, 1),
            1);

    QuizResponse quizResponse = QuizResponse.builder().id(1L).mcqs(mcqResponses).build();

    when(quizService.getInProgressQuiz(tutorContext, 0, 1)).thenReturn(quizResponse);

    mockMvc
        .perform(
            get("/v1/quizzes/me/in-progress")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).getInProgressQuiz(tutorContext, 0, 1);
  }

  @Test
  void getCompletedQuiz() throws Exception {
    SimpleQuizResponse simpleQuizResponse =
        SimpleQuizResponse.builder().id(1L).status(QuizStatus.COMPLETED).points(1).build();
    PageImpl<SimpleQuizResponse> quizResponses =
        new PageImpl<>(List.of(simpleQuizResponse), PageRequest.of(0, 1), 1);
    when(quizService.getCompletedQuiz(studentContext, 0, 1)).thenReturn(quizResponses);

    mockMvc
        .perform(
            get("/v1/quizzes/me/completed")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponses)));

    verify(quizService).getCompletedQuiz(studentContext, 0, 1);
  }

  @Test
  void updateAttemptSuccess() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    QuizAttemptRequest attemptRequest = new QuizAttemptRequest(1);

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNoContent());

    verify(quizService)
        .updateAttempt(
            quizId,
            mcqId,
            studentContext.getUserId(),
            attemptRequest.getAttemptOption(),
            studentContext);
  }

  @Test
  void updateAttemptQuizNotFound() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    QuizAttemptRequest attemptRequest = new QuizAttemptRequest(1);

    doThrow(new NotFoundException("Quiz not found"))
        .when(quizService)
        .updateAttempt(
            quizId,
            mcqId,
            studentContext.getUserId(),
            attemptRequest.getAttemptOption(),
            studentContext);

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateAttemptAttemptNotFound() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    QuizAttemptRequest attemptRequest = new QuizAttemptRequest(1);

    doThrow(new NotFoundException("Attempt not found"))
        .when(quizService)
        .updateAttempt(
            quizId,
            mcqId,
            studentContext.getUserId(),
            attemptRequest.getAttemptOption(),
            studentContext);

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateAttemptAttemptAlreadyExists() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    QuizAttemptRequest attemptRequest = new QuizAttemptRequest(1);

    doThrow(new AttemptAlreadyExistsException())
        .when(quizService)
        .updateAttempt(
            quizId,
            mcqId,
            studentContext.getUserId(),
            attemptRequest.getAttemptOption(),
            studentContext);

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  void abandonQuiz() throws Exception {
    Long quizId = 1L;
    String studentId = "test-user-id";

    mockMvc
        .perform(
            patch("/v1/quizzes/{quizId}/abandon", quizId)
                .header("x-user-id", studentId)
                .contentType(APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(quizService).abandonQuiz(quizId, studentId);
  }
}
