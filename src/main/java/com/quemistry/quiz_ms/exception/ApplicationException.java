package com.quemistry.quiz_ms.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ApplicationException extends RuntimeException {
  private final HttpStatusCode code;

  public ApplicationException(String message, HttpStatusCode code) {
    super(message);
    this.code = code;
  }

  public ApplicationException(String message, HttpStatusCode code, Throwable cause) {
    super(message, cause);
    this.code = code;
  }
}
