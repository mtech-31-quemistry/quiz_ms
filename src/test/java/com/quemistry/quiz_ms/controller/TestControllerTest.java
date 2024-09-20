package com.quemistry.quiz_ms.controller;

import static com.quemistry.quiz_ms.fixture.TestFixture.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.ExceptionAdvisor;
import com.quemistry.quiz_ms.model.TestEntity;
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
            .mcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)))
            .studentIds(List.of(STUDENT_ID))
            .title(TEST_TITLE)
            .build();

    when(testService.createTest(TUTOR_ID, testRequest)).thenReturn(TEST_ID);

    mockMvc
        .perform(
            post("/v1/tests")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/v1/tests/" + TEST_ID));

    verify(testService).createTest(tutorContext.getUserId(), testRequest);
  }

  @Test
  void updateTest() throws Exception {
    TestRequest testRequest =
        TestRequest.builder()
            .mcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)))
            .studentIds(List.of(STUDENT_ID))
            .title(TEST_TITLE)
            .build();

    mockMvc
        .perform(
            put("/v1/tests/" + TEST_ID)
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRequest)))
        .andExpect(status().isNoContent());

    verify(testService).updateTest(TEST_ID, testRequest, tutorContext);
  }

  @Test
  void getTestsForTutor() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForTutor(STUDENT_ID, null, 0, 10, tutorContext)).thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/tutor")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForTutor(tutorContext.getUserId(), null, 0, 10, tutorContext);
  }

  @Test
  void getTestsForTutorWithSearchConditions() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForTutor(STUDENT_ID, TEST_TITLE, 0, 10, tutorContext))
        .thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/tutor")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("search", TEST_TITLE)
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForTutor(tutorContext.getUserId(), TEST_TITLE, 0, 10, tutorContext);
  }

  @Test
  void getTestsByStudent() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForStudent(STUDENT_ID, null, 0, 10, tutorContext))
        .thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/student/" + STUDENT_ID)
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForStudent(STUDENT_ID, null, 0, 10, tutorContext);
  }

  @Test
  void getTestsForStudent() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForStudent(STUDENT_ID, null, 0, 10, studentContext))
        .thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/student")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService).getTestsForStudent(studentContext.getUserId(), null, 0, 10, studentContext);
  }

  @Test
  void getTestsForStudentWithSearchConditions() throws Exception {
    Page<TestEntity> testPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(testService.getTestsForStudent(STUDENT_ID, TEST_TITLE, 0, 10, studentContext))
        .thenReturn(testPage);

    mockMvc
        .perform(
            get("/v1/tests/student")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .param("search", TEST_TITLE)
                .param("pageNumber", "0")
                .param("pageSize", "10"))
        .andExpect(status().isOk());

    verify(testService)
        .getTestsForStudent(studentContext.getUserId(), TEST_TITLE, 0, 10, studentContext);
  }

  @Test
  void getTestMcqDetail() throws Exception {
    TestMcqDetailResponse response = new TestMcqDetailResponse();
    when(testService.getTestMcqDetail(TEST_ID, tutorContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/mcqs")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestMcqDetail(TEST_ID, tutorContext);
  }

  @Test
  void getMyTestMcqDetail() throws Exception {
    TestMcqDetailResponse response = new TestMcqDetailResponse();
    when(testService.getMyTestMcqDetail(TEST_ID, tutorContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/mcqs/me")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getMyTestMcqDetail(TEST_ID, tutorContext);
  }

  @Test
  void getTestStudentDetail() throws Exception {
    TestStudentDetailResponse response = new TestStudentDetailResponse();
    when(testService.getTestStudentDetail(TEST_ID, tutorContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/students")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestStudentDetail(TEST_ID, tutorContext);
  }

  @Test
  void getTestStudentAttempts() throws Exception {
    TestStudentAttemptResponse response = new TestStudentAttemptResponse();
    when(testService.getTestStudentAttempts(TEST_ID, STUDENT_ID, tutorContext))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/students/" + STUDENT_ID + "/attempts")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestStudentAttempts(TEST_ID, STUDENT_ID, tutorContext);
  }

  @Test
  void getMyTestStudentAttempts() throws Exception {
    TestStudentAttemptResponse response = new TestStudentAttemptResponse();
    when(testService.getTestStudentAttempts(TEST_ID, STUDENT_ID, studentContext))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/students/me/attempts")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getMyTestStudentAttempts(TEST_ID, studentContext);
  }

  @Test
  void getTestMcqAttempts() throws Exception {
    TestMcqAttemptResponse response = new TestMcqAttemptResponse();
    when(testService.getTestMcqAttempts(TEST_ID, MCQ_ID, tutorContext)).thenReturn(response);

    mockMvc
        .perform(
            get("/v1/tests/" + TEST_ID + "/mcq/" + MCQ_ID + "/attempts")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isOk());

    verify(testService).getTestMcqAttempts(TEST_ID, MCQ_ID, tutorContext);
  }

  @Test
  void startTest() throws Exception {
    mockMvc
        .perform(
            patch("/v1/tests/" + TEST_ID + "/start")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isNoContent());

    verify(testService).startTest(TEST_ID, tutorContext);
  }

  @Test
  void completeTest() throws Exception {
    mockMvc
        .perform(
            patch("/v1/tests/" + TEST_ID + "/complete")
                .header("x-user-id", tutorContext.getUserId())
                .header("x-user-email", tutorContext.getUserEmail())
                .header("x-user-roles", tutorContext.getUserRoles()))
        .andExpect(status().isNoContent());

    verify(testService).completeTest(TEST_ID, tutorContext);
  }

  @Test
  void updateTestStudentAttempts() throws Exception {
    int attemptOption = 1;
    TestAttemptRequest request = TestAttemptRequest.builder().attemptOption(attemptOption).build();
    mockMvc
        .perform(
            put("/v1/tests/" + TEST_ID + "/students/me/mcq/" + MCQ_ID + "/attempts")
                .header("x-user-id", studentContext.getUserId())
                .header("x-user-email", studentContext.getUserEmail())
                .header("x-user-roles", studentContext.getUserRoles())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNoContent());

    verify(testService).updateTestStudentAttempts(TEST_ID, MCQ_ID, attemptOption, studentContext);
  }
}
