package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.controller.model.QuizRequest;
import com.quemistry.quiz_ms.controller.model.QuizResponse;
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
        // Create a QuizRequest object with sample data
        QuizRequest quizRequest = QuizRequest.builder()
                .topics(List.of(1L, 2L))
                .skills(List.of(3L, 4L))
                .pageNumber(0)
                .pageSize(10)
                .build();

        // Create a RetrieveMCQResponse object with sample data
        RetrieveMCQResponse retrieveMCQResponse = RetrieveMCQResponse.builder()
                .mcqs(List.of(
                        MCQDto.builder().id(1L).build(),
                        MCQDto.builder().id(2L).build()
                ))
                .build();

        // Mock the questionClient.retrieveMCQs method to return the RetrieveMCQResponse
        when(questionClient.retrieveMCQs(any(RetrieveMCQRequest.class))).thenReturn(retrieveMCQResponse);

        // Create a Quiz object with sample data
        Quiz quiz = Quiz.builder()
                .id(1L)
                .status(QuizStatus.IN_PROGRESS)
                .studentId("test-user-id")
                .mcqIds(List.of(1L, 2L))
                .build();

        // Mock the quizRepository.save method to return the Quiz object
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        // Call the createQuiz method in QuizService
        QuizResponse quizResponse = quizService.createQuiz("test-user-id", quizRequest);

        // Verify the response
        assertEquals(1L, quizResponse.getId());
        assertEquals(2, quizResponse.getMcqs().size());
        assertEquals(0, quizResponse.getPageNumber());
        assertEquals(2, quizResponse.getPageSize());

        // Verify that the quizRepository.save method was called with the correct parameters
        verify(quizRepository).save(any(Quiz.class));

        // Verify that the questionClient.retrieveMCQs method was called with the correct parameters
        verify(questionClient).retrieveMCQs(any(RetrieveMCQRequest.class));
    }
}