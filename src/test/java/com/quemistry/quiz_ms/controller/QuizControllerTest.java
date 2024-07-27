package com.quemistry.quiz_ms.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.service.QuizService;
import java.util.ArrayList;
import java.util.HashMap;
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

    ObjectMapper objectMapper = new ObjectMapper();
    String expectedResponseBody = objectMapper.writeValueAsString(expectedResponse);

    mockMvc
        .perform(get("/v1/quizzes/health"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseBody));
  }

  @Test
  void createQuiz() throws Exception {
    // Mock the QuizService to return a predefined QuizResponse
    QuizResponse quizResponse =
        QuizResponse.builder().id(1L).mcqs(new ArrayList<>()).pageNumber(0).pageSize(0).build();

    // Create a QuizRequest object
    QuizRequest quizRequest = new QuizRequest();

    // Convert QuizRequest to JSON
    ObjectMapper objectMapper = new ObjectMapper();
    String quizRequestJson = objectMapper.writeValueAsString(quizRequest);
    when(quizService.createQuiz("test-user-id", quizRequest)).thenReturn(quizResponse);

    // Perform a POST request to /v1/quizzes with the x-user-id header and the
    // QuizRequest JSON
    mockMvc
        .perform(
            post("/v1/quizzes")
                .header("x-user-id", "test-user-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quizRequestJson))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    // Verify that the createQuiz method in QuizService was called with the correct
    // parameters
    verify(quizService).createQuiz("test-user-id", quizRequest);
  }

  @Test
  void getQuiz() throws Exception {
    // Mock the QuizService to return a predefined QuizResponse
    QuizResponse quizResponse =
        QuizResponse.builder()
            .id(1L)
            .mcqs(new ArrayList<>())
            .pageNumber(0)
            .pageSize(1)
            .totalPages(1)
            .totalRecords(2L)
            .build();

    // Create a GetQuizRequest object

    // Convert GetQuizRequest to JSON
    ObjectMapper objectMapper = new ObjectMapper();

    when(quizService.getQuiz(1L, "test-user-id", 0, 1)).thenReturn(quizResponse);

    // Perform a GET request to /v1/quizzes with the x-user-id header and the GetQuizRequest JSON
    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    // Verify that the getQuiz method in QuizService was called with the correct parameters
    verify(quizService).getQuiz(1L, "test-user-id", 0, 1);
  }

  @Test
  void getQuizNotFound() throws Exception {

    // Convert GetQuizRequest to JSON
    ObjectMapper objectMapper = new ObjectMapper();
    when(quizService.getQuiz(1L, "test-user-id", 0, 1))
        .thenThrow(new NotFoundException("Quiz not found"));

    // Perform a GET request to /v1/quizzes with the x-user-id header and the GetQuizRequest JSON
    mockMvc
        .perform(
            get("/v1/quizzes/1")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().json("{\"message\":\"Quiz not found\"}"));

    // Verify that the getQuiz method in QuizService was called with the correct parameters
    verify(quizService).getQuiz(1L, "test-user-id", 0, 1);
  }

  @Test
  void getInProgressQuiz() throws Exception {
    // Mock the QuizService to return a predefined QuizResponse
    QuizResponse quizResponse =
        QuizResponse.builder()
            .id(1L)
            .mcqs(new ArrayList<>())
            .pageNumber(0)
            .pageSize(1)
            .totalPages(1)
            .totalRecords(2L)
            .build();

    // Create a GetQuizRequest object

    // Convert GetQuizRequest to JSON
    ObjectMapper objectMapper = new ObjectMapper();

    when(quizService.getInProgressQuiz("test-user-id", 0, 1)).thenReturn(quizResponse);

    // Perform a GET request to /v1/quizzes with the x-user-id header and the GetQuizRequest JSON
    mockMvc
        .perform(
            get("/v1/quizzes/me/in-progress")
                .header("x-user-id", "test-user-id")
                .param("pageNumber", "0")
                .param("pageSize", "1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(quizResponse)));

    // Verify that the getQuiz method in QuizService was called with the correct parameters
    verify(quizService).getInProgressQuiz("test-user-id", 0, 1);
  }
}
