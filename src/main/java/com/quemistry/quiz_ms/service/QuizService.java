package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
import com.quemistry.quiz_ms.repository.QuizRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionClient questionClient;

    @Autowired
    public QuizService(QuizRepository quizRepository, QuestionClient questionClient) {
        this.quizRepository = quizRepository;
        this.questionClient = questionClient;
    }

    public QuizResponse createQuiz(String userId, QuizRequest quizRequest) {
        log.info("POST /v1/quizzes");

        Quiz quiz = Quiz.create(userId);

        RetrieveMCQResponse retrieveMCQRequests = questionClient.retrieveMCQs(
                RetrieveMCQRequest
                        .builder()
                        .topics(quizRequest.getTopics())
                        .skills(quizRequest.getSkills())
                        .build());

        List<Long> mcqIds = retrieveMCQRequests.getMcqs()
                .stream()
                .map(MCQDto::getId)
                .toList();

        quiz.addMcq(mcqIds);
        Long quizId = quizRepository.save(quiz).getId();

        return QuizResponse.builder()
                .id(quizId)
                .mcqs(retrieveMCQRequests.getMcqs())
                .pageNumber(0)
                .pageSize(retrieveMCQRequests.getMcqs().size())
                .build();
    }
}
