package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.mapper.MCQMapper.INSTANCE;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.InProgressTestAlreadyExistsException;
import com.quemistry.quiz_ms.exception.NotFoundException;
import com.quemistry.quiz_ms.mapper.MCQMapper;
import com.quemistry.quiz_ms.model.*;
import com.quemistry.quiz_ms.repository.TestAttemptRepository;
import com.quemistry.quiz_ms.repository.TestMcqRepository;
import com.quemistry.quiz_ms.repository.TestRepository;
import com.quemistry.quiz_ms.repository.TestStudentRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
  private final TestRepository testRepository;
  private final TestMcqRepository testMcqRepository;
  private final TestStudentRepository testStudentRepository;
  private final TestAttemptRepository testAttemptRepository;
  private final QuestionClient questionClient;
  private final MCQMapper mcqMapper = INSTANCE;

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

  public Page<TestEntity> getTestsForTutor(String tutorId, Integer pageNumber, Integer pageSize) {
    return testRepository.findPageByTutorIdOOrderByIdDesc(
        tutorId, PageRequest.of(pageNumber, pageSize));
  }

  public Page<TestEntity> getTestsForStudent(
      String studentId, Integer pageNumber, Integer pageSize) {
    Page<TestStudent> testStudentPage =
        testStudentRepository.findPageByStudentIdOrderByTestIdDesc(
            studentId, PageRequest.of(pageNumber, pageSize));
    List<Long> testIds = testStudentPage.getContent().stream().map(TestStudent::getTestId).toList();
    List<TestEntity> testEntities = testRepository.findAllById(testIds);
    return testStudentPage.map(
        testStudent ->
            testEntities.stream()
                .filter(testEntity -> testEntity.getId().equals(testStudent.getTestId()))
                .findFirst()
                .orElse(null));
  }

  public TestMcqDetailResponse getTestMcqDetail(Long testId, UserContext userContext) {
    Quartet<TestEntity, List<TestMcqs>, List<MCQResponse>, List<TestAttempt>> testData =
        getTestData(testId, userContext);
    return TestMcqDetailResponse.from(
        testData.getValue0(), testData.getValue1(), testData.getValue2(), testData.getValue3());
  }

  public TestStudentDetailResponse getTestStudentDetail(Long testId, UserContext userContext) {
    Quartet<TestEntity, List<TestMcqs>, List<MCQResponse>, List<TestAttempt>> testData =
        getTestData(testId, userContext);

    List<TestStudent> testStudents = testStudentRepository.findByTestId(testId);

    return TestStudentDetailResponse.from(
        testData.getValue0(),
        testData.getValue1(),
        testData.getValue2(),
        testData.getValue3(),
        testStudents);
  }

  public TestStudentAttemptResponse getTestStudentAttempts(
      Long testId, String studentId, UserContext userContext) {
    Triplet<TestEntity, List<TestMcqs>, List<MCQResponse>> testMcqDetail =
        getMcqDetail(testId, userContext);

    List<TestAttempt> attempts = testAttemptRepository.findByTestIdAndStudentId(testId, studentId);
    Optional<TestStudent> student =
        testStudentRepository.findOneByTestIdAndStudentId(testId, studentId);
    if (student.isEmpty()) {
      throw new NotFoundException("Student not found");
    }
    return TestStudentAttemptResponse.from(
        testMcqDetail.getValue0(),
        testMcqDetail.getValue1(),
        testMcqDetail.getValue2(),
        attempts,
        student.get());
  }

  public TestMcqAttemptResponse getTestMcqAttempts(
      Long testId, Long mcqId, UserContext userContext) {
    TestEntity test = getTest(testId);
    Optional<TestMcqs> testMcq = testMcqRepository.findOneByTestIdAndMcqId(testId, mcqId);
    if (testMcq.isEmpty()) {
      throw new NotFoundException("MCQ not found");
    }

    Optional<MCQDto> mcq =
        questionClient
            .retrieveMCQsByIds(
                RetrieveMCQByIdsRequest.builder()
                    .ids(List.of(testMcq.get().getTestId()))
                    .pageNumber(0)
                    .pageSize(10)
                    .build(),
                userContext.getUserId(),
                userContext.getUserEmail(),
                userContext.getUserRoles())
            .getMcqs()
            .stream()
            .findFirst();

    if (mcq.isEmpty()) {
      throw new NotFoundException("MCQ not found in question service");
    }

    List<TestAttempt> attempts = testAttemptRepository.findByTestIdAndMcqId(testId, mcqId);

    return TestMcqAttemptResponse.from(test, mcq.get(), attempts);
  }

  private Quartet<TestEntity, List<TestMcqs>, List<MCQResponse>, List<TestAttempt>> getTestData(
      Long testId, UserContext userContext) {
    Triplet<TestEntity, List<TestMcqs>, List<MCQResponse>> testMcqDetail =
        getMcqDetail(testId, userContext);

    List<TestAttempt> attempts = testAttemptRepository.findByTestId(testId);
    return Quartet.with(
        testMcqDetail.getValue0(), testMcqDetail.getValue1(), testMcqDetail.getValue2(), attempts);
  }

  private Triplet<TestEntity, List<TestMcqs>, List<MCQResponse>> getMcqDetail(
      Long testId, UserContext userContext) {
    TestEntity test = getTest(testId);

    List<TestMcqs> testMcqs = testMcqRepository.findByTestId(testId);
    RetrieveMCQResponse retrieveMCQResponse =
        questionClient.retrieveMCQsByIds(
            RetrieveMCQByIdsRequest.builder()
                .ids(testMcqs.stream().map(TestMcqs::getMcqId).toList())
                .pageNumber(0)
                .pageSize(180)
                .build(),
            userContext.getUserId(),
            userContext.getUserEmail(),
            userContext.getUserRoles());
    List<MCQResponse> mcqs =
        retrieveMCQResponse.getMcqs().stream().map(mcqMapper::toMCQResponse).toList();

    return Triplet.with(test, testMcqs, mcqs);
  }

  private TestEntity getTest(Long testId) {
    Optional<TestEntity> OptionalTestEntity = testRepository.findById(testId);
    if (OptionalTestEntity.isEmpty()) {
      throw new NotFoundException("Test not found");
    }
    return OptionalTestEntity.get();
  }
}
