package com.quemistry.quiz_ms.controller;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.service.TestService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/tests")
public class TestController {
  private final TestService testService;

  public TestController(TestService testService) {
    this.testService = testService;
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public void createTest(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestBody @Validated TestRequest testRequest,
      HttpServletResponse response) {
    log.info("POST /v1/testes");
    Long testId = testService.createTest(tutorId, testRequest);
    response.setHeader("Location", "/v1/tests/" + testId);
  }

  @GetMapping("tutor")
  public Page<TestEntity> getTestsForTutor(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestParam(required = false) String search,
      @RequestParam @PositiveOrZero Integer pageNumber,
      @RequestParam @PositiveOrZero Integer pageSize) {
    log.info("GET /v1/tests/tutor");

    return testService.getTestsForTutor(
        tutorId, search, pageNumber, pageSize, new UserContext(tutorId, studentEmail, roles));
  }

  @GetMapping("student")
  public Page<TestEntity> getTestsForStudent(
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestParam(required = false) String search,
      @RequestParam @PositiveOrZero Integer pageNumber,
      @RequestParam @PositiveOrZero @Max(60) Integer pageSize) {
    log.info("GET /v1/tests/student");
    return testService.getTestsForStudent(
        studentId, search, pageNumber, pageSize, new UserContext(studentId, studentEmail, roles));
  }

  @GetMapping("{testId}/mcqs")
  public TestMcqDetailResponse getTestMcqDetail(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @PathVariable Long testId) {
    log.info("GET /v1/tests/{}/mcq", testId);

    return testService.getTestMcqDetail(testId, new UserContext(tutorId, tutorEmail, roles));
  }

  @GetMapping("{testId}/students")
  public TestStudentDetailResponse getTestStudentDetail(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @PathVariable Long testId) {
    log.info("GET /v1/tests/{}/student", testId);

    return testService.getTestStudentDetail(testId, new UserContext(tutorId, tutorEmail, roles));
  }

  @GetMapping("{testId}/students/{studentId}/attempts")
  public TestStudentAttemptResponse getTestStudentAttempts(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @PathVariable Long testId,
      @PathVariable String studentId) {
    log.info("GET /v1/tests/{}/students/{}/attempts", testId, studentId);

    return testService.getTestStudentAttempts(
        testId, studentId, new UserContext(tutorId, tutorEmail, roles));
  }

  @GetMapping("{testId}/students/me/attempts")
  public TestStudentAttemptResponse getMyTestStudentAttempts(
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @PathVariable Long testId) {
    log.info("GET /v1/tests/{}/students/me/attempts", testId);

    return testService.getTestStudentAttempts(
        testId, studentId, new UserContext(studentId, studentEmail, roles));
  }

  @GetMapping("{testId}/mcq/{mcqId}/attempts")
  public TestMcqAttemptResponse getTestMcqAttempts(
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @PathVariable Long testId,
      @PathVariable Long mcqId) {
    log.info("GET /v1/tests/{}/mcq/{}/attempts", testId, mcqId);

    return testService.getTestMcqAttempts(
        testId, mcqId, new UserContext(tutorId, tutorEmail, roles));
  }

  @PatchMapping("{testId}/start")
  @ResponseStatus(NO_CONTENT)
  public void startTest(
      @PathVariable Long testId,
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles) {
    log.info("PATCH /v1/tests/{}/start", testId);

    testService.startTest(testId, new UserContext(tutorId, tutorEmail, roles));
  }

  @PatchMapping("{testId}/complete")
  @ResponseStatus(NO_CONTENT)
  public void completeTest(
      @PathVariable Long testId,
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles) {
    log.info("PATCH /v1/tests/{}/complete", testId);

    testService.completeTest(testId, new UserContext(tutorId, tutorEmail, roles));
  }

  @PutMapping("{testId}/students/me/mcq/{mcqId}/attempts")
  @ResponseStatus(NO_CONTENT)
  public void updateTestStudentAttempts(
      @PathVariable Long testId,
      @PathVariable Long mcqId,
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestBody TestAttemptRequest attemptRequest) {
    log.info("PUT /v1/tests/{}/students/me/attempts", testId);

    testService.updateTestStudentAttempts(
        testId,
        mcqId,
        attemptRequest.getAttemptOption(),
        new UserContext(studentId, studentEmail, roles));
  }

  @PutMapping("{testId}/students/me/summit")
  @ResponseStatus(NO_CONTENT)
  public void summitMyTest(
      @PathVariable Long testId,
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles) {
    log.info("PUT /v1/tests/{}/students/me/summit", testId);

    testService.summitStudentTest(testId, new UserContext(studentId, studentEmail, roles));
  }
}
