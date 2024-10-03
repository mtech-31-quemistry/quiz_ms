package com.quemistry.quiz_ms.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class HealthControllerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;

  @InjectMocks private HealthController healthController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(healthController)
            .setControllerAdvice(new ExceptionAdvisor())
            .build();
  }

  @Test
  void testHealth() throws Exception {
    Map<String, String> expectedResponse = new HashMap<>();
    expectedResponse.put("service", "quiz");
    expectedResponse.put("status", "UP");

    String expectedResponseBody = objectMapper.writeValueAsString(expectedResponse);

    mockMvc
        .perform(get("/v1/quizzes/health"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseBody));
  }
}
