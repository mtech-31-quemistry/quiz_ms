package com.quemistry.quiz_ms.controller;

import com.quemistry.quiz_ms.controller.model.GetQuizRequest;
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

  @GetMapping
  public QuizResponse getQuiz(
      @RequestParam("id") Long id,
      @RequestHeader("x-user-id") String studentId,
      @RequestBody GetQuizRequest quizRequest) {
    log.info("GET /v1/quizzes");
    return quizService.getQuiz(id, studentId, quizRequest);
  }
}
