package com.quemistry.quiz_ms.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class WPADControllerTest {

  @Test
  void handleWpadRequest() {
    WPADController wpadController = new WPADController();
    ResponseEntity<Void> response = wpadController.handleWpadRequest();
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }
}
