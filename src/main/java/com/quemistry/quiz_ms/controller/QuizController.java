package com.quemistry.quiz_ms.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.quemistry.quiz_ms.controller.model.AttemptRequest;
import com.quemistry.quiz_ms.controller.model.QuizListResponse;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.service.QuizService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/quizzes")
public class QuizController {
  private final QuizService quizService;

  public QuizController(QuizService quizService) {
    this.quizService = quizService;
  }

  @GetMapping("health")
  public Map<String, String> health() {
    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("service", "quiz");
    responseBody.put("status", "UP");
    return responseBody;
  }

  @PostMapping
  public QuizResponse createQuiz(
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-roles") String roles,
      @RequestBody QuizRequest quizRequest) {
    log.info("POST /v1/quizzes");
    return quizService.createQuiz(new UserContext(studentId, studentEmail, roles), quizRequest);
  }

  @GetMapping("{id}")
  public QuizResponse getQuiz(
      @PathVariable Long id,
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-roles") String roles,
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize) {
    log.info("GET /v1/quizzes/{}", id);
    return quizService.getQuiz(
        id, new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @GetMapping("me/in-progress")
  public QuizResponse getInProgressQuiz(
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-roles") String roles,
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize) {
    log.info("GET /v1/me/in-progress");

    return quizService.getInProgressQuiz(
        new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @GetMapping("me/completed")
  public QuizListResponse getCompletedQuiz(
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-roles") String roles,
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize) {
    log.info("GET /v1/me/completed");

    return quizService.getCompletedQuiz(
        new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @PutMapping("{id}/mcqs/{mcqId}/attempt")
  @ResponseStatus(NO_CONTENT)
  public void updateAttempt(
      @PathVariable Long id,
      @PathVariable Long mcqId,
      @RequestHeader("x-user-id") String studentId,
      @RequestBody AttemptRequest attemptRequest) {
    log.info("PUT /v1/quizzes/{}/mcqs/{}/attempt", id, mcqId);

    quizService.updateAttempt(id, mcqId, studentId, attemptRequest.getAttemptOption());
  }

  @PatchMapping("{id}/abandon")
  @ResponseStatus(NO_CONTENT)
  public void abandonQuiz(@PathVariable Long id, @RequestHeader("x-user-id") String studentId) {
    log.info("PATCH /v1/quizzes/{}/abandon", id);
    quizService.abandonQuiz(id, studentId);
  }
}
