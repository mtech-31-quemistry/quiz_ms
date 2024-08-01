package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class AttemptAlreadyExistsException extends ApplicationException {
  public AttemptAlreadyExistsException() {
    super("Attempt already exists", CONFLICT);
  }
}
