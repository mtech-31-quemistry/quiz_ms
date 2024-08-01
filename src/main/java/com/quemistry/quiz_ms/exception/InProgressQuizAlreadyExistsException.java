package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.CONFLICT;

public class InProgressQuizAlreadyExistsException extends ApplicationException {
  public InProgressQuizAlreadyExistsException() {
    super("In progress quiz already exists", CONFLICT);
  }
}
