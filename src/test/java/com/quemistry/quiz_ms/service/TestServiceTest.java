package com.quemistry.quiz_ms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.controller.model.TestRequest;
import com.quemistry.quiz_ms.exception.InProgressTestAlreadyExistsException;
import com.quemistry.quiz_ms.model.*;
import com.quemistry.quiz_ms.repository.*;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class TestServiceTest {

  @Mock private TestRepository testRepository;

  @Mock private TestMcqRepository testMcqRepository;

  @Mock private TestStudentRepository testStudentRepository;

  @Mock private TestAttemptRepository testAttemptRepository;

  @Mock private QuestionClient questionClient;

  @InjectMocks private TestService testService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createTest_success() {
    String tutorId = "tutor1";
    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(Collections.emptyList());
    testRequest.setStudentIds(Collections.emptyList());

    when(testRepository.existsByTutorIdAndStatus(tutorId, TestStatus.IN_PROGRESS))
        .thenReturn(false);
    when(testRepository.save(any(TestEntity.class)))
        .thenAnswer(
            invocation -> {
              TestEntity test = invocation.getArgument(0);
              test.setId(1L);
              return test;
            });

    Long testId = testService.createTest(tutorId, testRequest);

    assertNotNull(testId);
    assertEquals(1L, testId);

    verify(testRepository, times(1)).existsByTutorIdAndStatus(tutorId, TestStatus.IN_PROGRESS);
    verify(testRepository, times(1)).save(any(TestEntity.class));
    verify(testMcqRepository, times(0)).save(any(TestMcqs.class));
    verify(testStudentRepository, times(0)).save(any(TestStudent.class));
    verify(testAttemptRepository, times(0)).save(any(TestAttempt.class));
  }

  @Test
  void createTest_inProgressTestAlreadyExists() {
    String tutorId = "tutor1";
    TestRequest testRequest = new TestRequest();

    when(testRepository.existsByTutorIdAndStatus(tutorId, TestStatus.IN_PROGRESS)).thenReturn(true);

    assertThrows(
        InProgressTestAlreadyExistsException.class,
        () -> {
          testService.createTest(tutorId, testRequest);
        });

    verify(testRepository, times(1)).existsByTutorIdAndStatus(tutorId, TestStatus.IN_PROGRESS);
    verify(testRepository, times(0)).save(any(TestEntity.class));
  }
}
