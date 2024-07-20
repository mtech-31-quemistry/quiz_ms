package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizRequest;
import com.quemistry.quiz_ms.model.QuizResponse;
import com.quemistry.quiz_ms.model.QuizStatus;
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
    public QuizService(QuizRepository quizRepository,QuestionClient questionClient) {
        this.quizRepository = quizRepository;
        this.questionClient = questionClient;
    }

    public QuizResponse createQuiz(QuizRequest quizRequest) {
        log.info("POST /v1/quizzes");
        Quiz quiz =  Quiz.builder()
                .status(QuizStatus.IN_PROGRESS)
                .studentId(quizRequest.getStudentId())
                .build();

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

        quiz.setMcqIds(mcqIds);
        Quiz savedQuiz = quizRepository.save(quiz);

        return QuizResponse.builder()
                .id(savedQuiz.getId())
                .mcqs(retrieveMCQRequests.getMcqs())
                .pageNumber(0)
                .pageSize(retrieveMCQRequests.getMcqs().size())
                .build();
    }
}
