package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.controller.model.TestRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
  public Long createTest(String tutorId, TestRequest testRequest) {
    return 1L;
  }
}
