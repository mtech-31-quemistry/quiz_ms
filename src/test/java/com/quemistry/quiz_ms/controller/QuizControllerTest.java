package com.quemistry.quiz_ms.controller;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.AttemptRequest;
import com.quemistry.quiz_ms.controller.model.MCQResponse;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.AttemptAlreadyExistsException;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.service.QuizService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
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
  void testHealth() throws Exception {
    Map<String, String> expectedResponse = new HashMap<>();
    expectedResponse.put("service", "auth");
    expectedResponse.put("status", "UP");

    String expectedResponseBody = objectMapper.writeValueAsString(expectedResponse);

    mockMvc
        .perform(get("/v1/quizzes/health"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseBody));
  }

  @Test
  void createQuiz() throws Exception {
    List<MCQResponse> mcqResponses = new ArrayList<>();
    mcqResponses.add(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build());

    QuizResponse quizResponse =
        QuizResponse.builder()
            .id(1L)
            .mcqs(mcqResponses)
            .pageNumber(0)
            .pageSize(1)
            .totalPages(1)
            .totalRecords(1L)
            .build();

    QuizRequest quizRequest = QuizRequest.builder().pageSize(1).totalSize(1L).build();

    String quizRequestJson = objectMapper.writeValueAsString(quizRequest);
    when(quizService.createQuiz("test-user-id", quizRequest)).thenReturn(quizResponse);

    mockMvc
        .perform(
            post("/v1/quizzes")
                .header("x-user-id", "test-user-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quizRequestJson))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).createQuiz("test-user-id", quizRequest);
  }

  @Test
  void getQuiz() throws Exception {
    List<MCQResponse> mcqResponses = new ArrayList<>();
    mcqResponses.add(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build());

    QuizResponse quizResponse =
        QuizResponse.builder()
            .id(1L)
            .mcqs(mcqResponses)
            .pageNumber(0)
            .pageSize(1)
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizService.getQuiz(1L, "test-user-id", 0, 1)).thenReturn(quizResponse);

    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).getQuiz(1L, "test-user-id", 0, 1);
  }

  @Test
  void getQuizNotFound() throws Exception {
    when(quizService.getQuiz(1L, "test-user-id", 0, 1))
        .thenThrow(new NotFoundException("Quiz not found"));

    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().json("{\"message\":\"Quiz not found\"}"));

    verify(quizService).getQuiz(1L, "test-user-id", 0, 1);
  }

  @Test
  void getInProgressQuiz() throws Exception {
    List<MCQResponse> mcqResponses = new ArrayList<>();
    mcqResponses.add(MCQResponse.builder().id(1L).attemptOption(1).attemptOn(new Date()).build());

    QuizResponse quizResponse =
        QuizResponse.builder()
            .id(1L)
            .mcqs(mcqResponses)
            .pageNumber(0)
            .pageSize(1)
            .totalPages(1)
            .totalRecords(1L)
            .build();

    when(quizService.getInProgressQuiz("test-user-id", 0, 1)).thenReturn(quizResponse);

    mockMvc
        .perform(
            get("/v1/quizzes/me/in-progress")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    verify(quizService).getInProgressQuiz("test-user-id", 0, 1);
  }

  @Test
  void updateAttemptSuccess() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "test-user-id";
    AttemptRequest attemptRequest = new AttemptRequest(1);

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNoContent());

    verify(quizService).updateAttempt(quizId, mcqId, studentId, attemptRequest.getAttemptOption());
  }

  @Test
  void updateAttemptQuizNotFound() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "test-user-id";
    AttemptRequest attemptRequest = new AttemptRequest(1);

    doThrow(new NotFoundException("Quiz not found"))
        .when(quizService)
        .updateAttempt(quizId, mcqId, studentId, attemptRequest.getAttemptOption());

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateAttemptAttemptNotFound() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "test-user-id";
    AttemptRequest attemptRequest = new AttemptRequest(1);

    doThrow(new NotFoundException("Attempt not found"))
        .when(quizService)
        .updateAttempt(quizId, mcqId, studentId, attemptRequest.getAttemptOption());

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isNotFound());
  }

  @Test
  void updateAttemptAttemptAlreadyExists() throws Exception {
    Long quizId = 1L;
    Long mcqId = 1L;
    String studentId = "test-user-id";
    AttemptRequest attemptRequest = new AttemptRequest(1);

    doThrow(new AttemptAlreadyExistsException())
        .when(quizService)
        .updateAttempt(quizId, mcqId, studentId, attemptRequest.getAttemptOption());

    mockMvc
        .perform(
            put("/v1/quizzes/{quizId}/mcqs/{mcqId}/attempt", quizId, mcqId)
                .header("x-user-id", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attemptRequest)))
        .andExpect(status().isConflict());
  }
}
