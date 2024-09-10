package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TestCannotCompleteException extends ApplicationException {
  public TestCannotCompleteException() {
    super("Test cannot complete by current status", CONFLICT);
  }
}
