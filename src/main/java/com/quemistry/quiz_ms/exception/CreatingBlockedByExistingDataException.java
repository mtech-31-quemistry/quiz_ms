package com.quemistry.quiz_ms.exception;

import org.springframework.http.HttpStatus;

public class CreatingBlockedByExistingDataException extends ApplicationException {
  public CreatingBlockedByExistingDataException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
