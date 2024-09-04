package com.quemistry.quiz_ms.controller;

import static org.springframework.http.HttpStatus.CREATED;

import com.quemistry.quiz_ms.controller.model.TestRequest;
import com.quemistry.quiz_ms.service.TestService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/v1/tests")
public class TestController {
  private final TestService testService;

  public TestController(TestService testService) {
    this.testService = testService;
  }

  @PostMapping
  @ResponseStatus(CREATED)
  public void createTest(
      @RequestHeader("x-user-id") String tutorId,
      @RequestHeader("x-user-email") String tutorEmail,
      @RequestHeader("x-user-roles") String roles,
      @RequestBody TestRequest testRequest,
      HttpServletResponse response) {
    log.info("POST /v1/testes");
    Long testId = testService.createTest(tutorId, testRequest);
    response.setHeader("Location", "/v1/tests/" + testId);
  }
}
