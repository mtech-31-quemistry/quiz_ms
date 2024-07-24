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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionClient questionClient;

    @InjectMocks
    private QuizService quizService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createQuiz() {
        // Prepare test data
        QuizRequest quizRequest = new QuizRequest();
        quizRequest.setStudentId("student123");
        quizRequest.setTopics(List.of(11L, 22L));
        quizRequest.setSkills(List.of(33L, 44L));

        MCQDto mcqDto = new MCQDto();
        mcqDto.setId(1L);
        RetrieveMCQResponse retrieveMCQResponse = new RetrieveMCQResponse();
        retrieveMCQResponse.setMcqs(List.of(mcqDto));

        Quiz quiz = new Quiz();
        quiz.setId(1L);
        quiz.setStatus(QuizStatus.IN_PROGRESS);
        quiz.setStudentId("student123");
        quiz.setMcqIds(List.of(1L));

        // Mock external calls
        when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class))).thenReturn(retrieveMCQResponse);
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        // Call the method under test
        QuizResponse quizResponse = quizService.createQuiz(quizRequest);

        // Verify results
        assertEquals(1L, quizResponse.getId());
        assertEquals(1, quizResponse.getMcqs().size());
        assertEquals(1L, quizResponse.getMcqs().getFirst().getId());

        // Verify interactions
        verify(questionClient).retrieveMCQs(any(RetrieveMCQRequest.class));
        verify(quizRepository).save(any(Quiz.class));
    }
}