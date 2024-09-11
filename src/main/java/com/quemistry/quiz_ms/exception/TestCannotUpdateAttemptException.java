package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TestCannotUpdateAttemptException extends ApplicationException {
  public TestCannotUpdateAttemptException() {
    super("Test cannot update attempt by current status", CONFLICT);
  }
}
