package com.quemistry.quiz_ms.service;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.controller.model.TestRequest;
import com.quemistry.quiz_ms.exception.InProgressTestAlreadyExistsException;
import com.quemistry.quiz_ms.model.*;
import com.quemistry.quiz_ms.repository.TestAttemptRepository;
import com.quemistry.quiz_ms.repository.TestMcqRepository;
import com.quemistry.quiz_ms.repository.TestRepository;
import com.quemistry.quiz_ms.repository.TestStudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
  private final TestRepository testRepository;
  private final TestMcqRepository testMcqRepository;
  private final TestStudentRepository testStudentRepository;
  private final TestAttemptRepository testAttemptRepository;
  private final QuestionClient questionClient;

  public TestService(
      TestRepository testRepository,
      TestMcqRepository testMcqRepository,
      TestStudentRepository testStudentRepository,
      TestAttemptRepository testAttemptRepository,
      QuestionClient questionClient) {
    this.testRepository = testRepository;
    this.testMcqRepository = testMcqRepository;
    this.testStudentRepository = testStudentRepository;
    this.testAttemptRepository = testAttemptRepository;
    this.questionClient = questionClient;
  }

  public Long createTest(String tutorId, TestRequest testRequest) {
    log.info("Creating test for tutor {} with request {}", tutorId, testRequest);

    if (testRepository.existsByTutorIdAndStatus(tutorId, TestStatus.IN_PROGRESS)) {
      throw new InProgressTestAlreadyExistsException();
    }

    TestEntity test = TestEntity.create(tutorId);
    Long testId = testRepository.save(test).getId();

    testRequest
        .getMcqs()
        .forEach(
            mcqIndex -> {
              TestMcqs testMcqs = TestMcqs.create(testId, mcqIndex.getMcqId(), mcqIndex.getIndex());
              testMcqRepository.save(testMcqs);
            });

    testRequest
        .getStudentIds()
        .forEach(
            studentId -> {
              TestStudent testStudent = TestStudent.create(testId, studentId);
              testStudentRepository.save(testStudent);

              testRequest
                  .getMcqs()
                  .forEach(
                      mcqIndex -> {
                        TestAttempt testAttempt =
                            TestAttempt.create(testId, mcqIndex.getMcqId(), studentId);
                        testAttemptRepository.save(testAttempt);
                      });
            });

    return testId;
  }
}
