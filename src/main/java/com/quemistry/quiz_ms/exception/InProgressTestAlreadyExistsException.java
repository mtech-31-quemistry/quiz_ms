package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class InProgressTestAlreadyExistsException extends ApplicationException {
  public InProgressTestAlreadyExistsException() {
    super("In progress test already exists", CONFLICT);
  }
}
