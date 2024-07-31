package com.quemistry.quiz_ms.controller;

import com.quemistry.quiz_ms.controller.model.AttemptRequest;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.service.QuizService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Object> health() {
    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("service", "auth");
    responseBody.put("status", "UP");
    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
  }

  @PostMapping
  public QuizResponse createQuiz(
      @RequestHeader("x-user-id") String studentId, @RequestBody QuizRequest quizRequest) {
    log.info("POST /v1/quizzes");
    return quizService.createQuiz(studentId, quizRequest);
  }

  @GetMapping("{id}")
  public QuizResponse getQuiz(
      @PathVariable Long id,
      @RequestHeader("x-user-id") String studentId,
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize) {
    log.info("GET /v1/quizzes/{}", id);
    return quizService.getQuiz(id, studentId, pageNumber, pageSize);
  }

  @GetMapping("me/in-progress")
  public QuizResponse getInProgressQuiz(
      @RequestHeader("x-user-id") String studentId,
      @RequestParam Integer pageNumber,
      @RequestParam Integer pageSize) {
    log.info("GET /v1/me/in-progress");

    return quizService.getInProgressQuiz(studentId, pageNumber, pageSize);
  }

  @PutMapping("{id}/mcqs/{mcqId}/attempt")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateAttempt(
      @PathVariable Long id,
      @PathVariable Long mcqId,
      @RequestHeader("x-user-id") String studentId,
      @RequestBody AttemptRequest attemptRequest) {
    log.info("PUT /v1/quizzes/{}/mcqs/{}/attempt", id, mcqId);

    quizService.updateAttempt(id, mcqId, studentId, attemptRequest.getAttemptOption());
  }
}
