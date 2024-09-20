package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TestCannotUpdateException extends ApplicationException {
  public TestCannotUpdateException() {
    super("Test cannot update by current status", CONFLICT);
  }
}
