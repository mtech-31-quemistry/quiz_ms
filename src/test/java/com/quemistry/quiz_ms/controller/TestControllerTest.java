package com.quemistry.quiz_ms.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.service.TestService;
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

  @Test
  void getTestsForTutor() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForTutor("test-user-id", 0, 10)).thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/tutor")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForTutor(testUserContext.getUserId(), 0, 10);
  }

  @Test
  void getTestsForStudent() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForStudent("test-user-id", 0, 10)).thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/student")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForStudent(testUserContext.getUserId(), 0, 10);
  }

  @Test
  void getTestMcqDetail() throws Exception {
    TestMcqDetailResponse response = new TestMcqDetailResponse();
    when(testService.getTestMcqDetail(1L, testUserContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/1/mcqs")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestMcqDetail(1L, testUserContext);
  }

  @Test
  void getTestStudentDetail() throws Exception {
    TestStudentDetailResponse response = new TestStudentDetailResponse();
    when(testService.getTestStudentDetail(1L, testUserContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/1/students")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestStudentDetail(1L, testUserContext);
  }

  @Test
  void getTestStudentAttempts() throws Exception {
    TestStudentAttemptResponse response = new TestStudentAttemptResponse();
    when(testService.getTestStudentAttempts(1L, "student1", testUserContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/1/students/student1/attempts")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestStudentAttempts(1L, "student1", testUserContext);
  }

  @Test
  void getMyTestStudentAttempts() throws Exception {
    TestStudentAttemptResponse response = new TestStudentAttemptResponse();
    when(testService.getTestStudentAttempts(1L, "test-user-id", testUserContext))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/1/students/me/attempts")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestStudentAttempts(1L, "test-user-id", testUserContext);
  }

  @Test
  void getTestMcqAttempts() throws Exception {
    TestMcqAttemptResponse response = new TestMcqAttemptResponse();
    when(testService.getTestMcqAttempts(1L, 1L, testUserContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/1/mcq/1/attempts")
                .header("x-user-id", testUserContext.getUserId())
                .header("x-user-email", testUserContext.getUserEmail())
                .header("x-user-roles", testUserContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestMcqAttempts(1L, 1L, testUserContext);
  }
}
