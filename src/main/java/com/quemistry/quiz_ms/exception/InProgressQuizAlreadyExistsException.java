package com.quemistry.quiz_ms.exception;

import org.springframework.http.HttpStatus;

public class InProgressQuizAlreadyExistsException extends ApplicationException {
  public InProgressQuizAlreadyExistsException() {
    super("In progress quiz already exists", HttpStatus.CONFLICT);
  }
}
