package com.quemistry.quiz_ms.controller;

import com.quemistry.quiz_ms.model.QuizResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/quizzes")
public class QuizController {
    @GetMapping("health")
    public ResponseEntity<Object> health(){
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("service", "auth");
        responseBody.put("status", "UP");
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    @PostMapping
    public QuizResponse createQuiz() {
        log.info("POST /v1/quizzes");
        return QuizResponse.builder().build();
    }
}
