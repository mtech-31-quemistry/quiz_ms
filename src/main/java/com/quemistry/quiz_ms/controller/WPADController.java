package com.quemistry.quiz_ms.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WPADController {

  @GetMapping("/wpad.dat")
  public ResponseEntity<Void> handleWpadRequest() {
    return new ResponseEntity<>(NOT_FOUND);
  }
}
