package com.quemistry.quiz_ms.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

class ExceptionAdvisorTest {

  private ExceptionAdvisor exceptionAdvisor;
  private WebRequest webRequest;

  @BeforeEach
  void setUp() {
    exceptionAdvisor = new ExceptionAdvisor();
    webRequest = mock(WebRequest.class);
  }

  @Test
  void handleApplicationException_ReturnsResponseEntityWithCorrectStatusAndMessage() {
    ApplicationException ex = new ApplicationException("Application error", HttpStatus.BAD_REQUEST);

    ResponseEntity<ExceptionResponse> response =
        exceptionAdvisor.handleApplicationException(ex, webRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(Objects.requireNonNull(response.getBody()).getMessage())
        .isEqualTo("Application error");
  }

  @Test
  void handleGlobalException_ReturnsExceptionResponseWithCorrectMessage() {
    Exception ex = new Exception("Global error");

    ExceptionResponse response = exceptionAdvisor.handleGlobalException(ex, webRequest);

    assertThat(response.getMessage()).isEqualTo("Global error");
  }
}
