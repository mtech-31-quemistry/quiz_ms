package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TestCannotStartException extends ApplicationException {
  public TestCannotStartException() {
    super("Test cannot start by current status", CONFLICT);
  }
}
