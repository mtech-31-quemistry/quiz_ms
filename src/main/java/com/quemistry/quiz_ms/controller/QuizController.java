package com.quemistry.quiz_ms.controller;

import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.model.UserContext;
import com.quemistry.quiz_ms.service.QuizService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/quizzes")
public class QuizController {
  private final QuizService quizService;

  public QuizController(QuizService quizService) {
    this.quizService = quizService;
  }

  @PostMapping
  public QuizResponse createQuiz(
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestBody QuizRequest quizRequest) {
    log.info("POST /v1/quizzes");
    return quizService.createQuiz(new UserContext(studentId, studentEmail, roles), quizRequest);
  }

  @GetMapping("{id}")
  public QuizResponse getQuiz(
      @PathVariable Long id,
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestParam @PositiveOrZero Integer pageNumber,
      @RequestParam @PositiveOrZero @Max(60) Integer pageSize) {
    log.info("GET /v1/quizzes/{}", id);
    return quizService.getQuiz(
        id, new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @GetMapping("me/in-progress")
  public QuizResponse getInProgressQuiz(
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestParam @PositiveOrZero Integer pageNumber,
      @RequestParam @PositiveOrZero @Max(60) Integer pageSize) {
    log.info("GET /v1/quizzes/me/in-progress");

    return quizService.getInProgressQuiz(
        new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @GetMapping("me/completed")
  public Page<SimpleQuizResponse> getCompletedQuiz(
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestParam @PositiveOrZero Integer pageNumber,
      @RequestParam @PositiveOrZero @Max(60) Integer pageSize) {
    log.info("GET /v1/quizzes/me/completed");

    return quizService.getCompletedQuiz(
        new UserContext(studentId, studentEmail, roles), pageNumber, pageSize);
  }

  @PutMapping("{id}/mcqs/{mcqId}/attempt")
  @ResponseStatus(NO_CONTENT)
  public void updateAttempt(
      @PathVariable Long id,
      @PathVariable Long mcqId,
      @RequestHeader("x-user-id") @NotBlank String studentId,
      @RequestHeader("x-user-email") @Email String studentEmail,
      @RequestHeader("x-user-roles") @NotBlank String roles,
      @RequestBody QuizAttemptRequest attemptRequest) {
    log.info("PUT /v1/quizzes/{}/mcqs/{}/attempt", id, mcqId);

    quizService.updateAttempt(
        id,
        mcqId,
        studentId,
        attemptRequest.getAttemptOption(),
        new UserContext(studentId, studentEmail, roles));
  }

  @PatchMapping("{id}/abandon")
  @ResponseStatus(NO_CONTENT)
  public void abandonQuiz(
      @PathVariable Long id, @RequestHeader("x-user-id") @NotBlank String studentId) {
    log.info("PATCH /v1/quizzes/{}/abandon", id);
    quizService.abandonQuiz(id, studentId);
  }
}
