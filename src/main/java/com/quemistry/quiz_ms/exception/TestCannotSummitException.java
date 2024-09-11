package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class TestCannotSummitException extends ApplicationException {
  public TestCannotSummitException() {
    super("Test cannot summit by current status", CONFLICT);
  }
}
