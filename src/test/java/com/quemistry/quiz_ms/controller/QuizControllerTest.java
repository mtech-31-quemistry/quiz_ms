package com.quemistry.quiz_ms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.model.QuizRequest;
import com.quemistry.quiz_ms.model.QuizResponse;
import com.quemistry.quiz_ms.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class QuizControllerTest {
    private MockMvc mockMvc;

    @Mock
    private QuizService quizService;

    @InjectMocks
    private QuizController quizController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(quizController)
                .build();
    }

    @Test
    void testHealth() throws Exception {
        Map<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("service", "auth");
        expectedResponse.put("status", "UP");

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedResponseBody = objectMapper.writeValueAsString(expectedResponse);

        mockMvc.perform(get("/v1/quizzes/health"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseBody));
    }

    @Test
    void createQuiz() throws Exception {
        // 1. Create a QuizRequest object
        QuizRequest quizRequest = new QuizRequest();
        // Set properties on quizRequest as needed, e.g., quizRequest.setTitle("Sample Quiz");

        // 2. Convert QuizRequest object to JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonQuizRequest = objectMapper.writeValueAsString(quizRequest);

        // 3. Mock the quizService.createQuiz method
        QuizResponse mockResponse = new QuizResponse();
        // Set properties on mockResponse as needed, e.g., mockResponse.setId(1L);
        when(quizService.createQuiz(any(QuizRequest.class))).thenReturn(mockResponse);

        // 4. Perform a POST request to /v1/quizzes with the JSON payload
        mockMvc.perform(post("/v1/quizzes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonQuizRequest))
                .andExpect(status().isOk())
                // 6. Optionally, verify the response content
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

        // Optionally, verify that the service method was called
        verify(quizService).createQuiz(any(QuizRequest.class));
    }
}