package com.quemistry.quiz_ms.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.McqIndex;
import com.quemistry.quiz_ms.controller.model.TestRequest;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.service.TestService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class TestControllerTest {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private MockMvc mockMvc;
  private final UserContext testUserContext =
      new UserContext("test-user-id", "test-user-email", "test-user-roles");

  @Mock private TestService testService;

  @InjectMocks private TestController testController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc =
        MockMvcBuilders.standaloneSetup(testController)
            .setControllerAdvice(new ExceptionAdvisor())
            .build();
  }

  @Test
  void createTest() throws Exception {
    TestRequest testRequest =
        TestRequest.builder()
            .mcqs(List.of(new McqIndex(1L, 1)))
            .studentIds(List.of("student1", "student2"))
            .build();

    Long testId = 1L;
    when(testService.createTest("test-user-id", testRequest)).thenReturn(testId);

    mockMvc
        .perform(
            post("/v1/tests")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/v1/tests/" + testId));

    verify(testService).createTest("test-user-id", testRequest);
  }
}
