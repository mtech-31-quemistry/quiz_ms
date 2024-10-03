package com.quemistry.quiz_ms.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/health")
public class HealthController {
  @GetMapping
  public Map<String, String> health() {
    Map<String, String> responseBody = new HashMap<>();
    responseBody.put("service", "quiz");
    responseBody.put("status", "UP");
    return responseBody;
  }
}
