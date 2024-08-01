package com.quemistry.quiz_ms.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class WPADControllerTest {

  @Test
  void handleWpadRequest() {
    WPADController wpadController = new WPADController();
    ResponseEntity<Void> response = wpadController.handleWpadRequest();
    assertEquals(NOT_FOUND, response.getStatusCode());
  }
}
