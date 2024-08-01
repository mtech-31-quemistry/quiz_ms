package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundException extends ApplicationException {
  public NotFoundException(String message) {
    super(message, NOT_FOUND);
  }
}
