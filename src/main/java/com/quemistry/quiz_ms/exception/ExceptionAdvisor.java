package com.quemistry.quiz_ms.exception;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ExceptionAdvisor {
  private static final Logger log = LoggerFactory.getLogger(ExceptionAdvisor.class);

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ExceptionResponse> handleApplicationException(
      ApplicationException ex, WebRequest request) {
    ExceptionResponse response = new ExceptionResponse(ex.getMessage());
    return new ResponseEntity<>(response, ex.getCode());
  }

  @ResponseStatus(INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ExceptionResponse handleGlobalException(Exception ex, WebRequest request) {
    log.error("Internal server error", ex);
    return new ExceptionResponse(ex.getMessage());
  }
}
