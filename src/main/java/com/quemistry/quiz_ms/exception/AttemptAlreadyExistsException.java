package com.quemistry.quiz_ms.exception;

import org.springframework.http.HttpStatus;

public class AttemptAlreadyExistsException extends ApplicationException {
  public AttemptAlreadyExistsException() {
    super("Attempt already exists", HttpStatus.CONFLICT);
  }
}
