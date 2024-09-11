package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.mapper.MCQMapper.INSTANCE;
import static com.quemistry.quiz_ms.model.TestStatus.DRAFT;
import static com.quemistry.quiz_ms.model.TestStatus.IN_PROGRESS;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.*;
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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@CacheConfig(cacheNames = "test")
@Scope(proxyMode = TARGET_CLASS)
public class TestService {
  private final TestRepository testRepository;
  private final TestMcqRepository testMcqRepository;
  private final TestStudentRepository testStudentRepository;
  private final TestAttemptRepository testAttemptRepository;
  private final QuestionClient questionClient;
  private final TestService self;
  private final MCQMapper mcqMapper = INSTANCE;

  public TestService(
      TestRepository testRepository,
      TestMcqRepository testMcqRepository,
      TestStudentRepository testStudentRepository,
      TestAttemptRepository testAttemptRepository,
      QuestionClient questionClient,
      TestService testService) {
    this.testRepository = testRepository;
    this.testMcqRepository = testMcqRepository;
    this.testStudentRepository = testStudentRepository;
    this.testAttemptRepository = testAttemptRepository;
    this.questionClient = questionClient;
    this.self = testService;
  }

  public Long createTest(String tutorId, TestRequest testRequest) {
    if (testRepository.existsByTutorIdAndStatus(tutorId, DRAFT)) {
      throw new InProgressTestAlreadyExistsException();
    }

    TestEntity test = TestEntity.create(tutorId, testRequest.getTitle());
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

  public Page<TestEntity> getTestsForTutor(
      String tutorId,
      String search,
      Integer pageNumber,
      Integer pageSize,
      UserContext userContext) {
    if (search != null) {
      return testRepository.findPageByTutorIdAndTitleContainingOrderByIdDesc(
          tutorId, search, PageRequest.of(pageNumber, pageSize));
    }
    return testRepository.findPageByTutorIdOrderByIdDesc(
        tutorId, PageRequest.of(pageNumber, pageSize));
  }

  public Page<TestEntity> getTestsForStudent(
      String studentId,
      String search,
      Integer pageNumber,
      Integer pageSize,
      UserContext userContext) {
    List<TestStudent> testStudents = testStudentRepository.findByStudentId(studentId);
    List<Long> testIds = testStudents.stream().map(TestStudent::getTestId).toList();
    if (search != null) {
      return testRepository.findPageByIdInAndStatusIsNotAndTitleContaining(
          testIds, DRAFT, search, PageRequest.of(pageNumber, pageSize));
    }
    return testRepository.findPageByIdInAndStatusIsNot(
        testIds, DRAFT, PageRequest.of(pageNumber, pageSize));
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
                    .ids(List.of(testMcq.get().getMcqId()))
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

    return TestMcqAttemptResponse.from(test, testMcq.get().getIndex(), mcq.get(), attempts);
  }

  public void startTest(Long testId, UserContext userContext) {
    TestEntity test = getTest(testId);
    if (!test.getStatus().equals(DRAFT)) {
      throw new TestCannotStartException();
    }
    test.start(userContext.getUserId());
    testRepository.save(test);
  }

  public void completeTest(Long testId, UserContext userContext) {
    TestEntity test = getTest(testId);
    if (!test.getStatus().equals(IN_PROGRESS)) {
      throw new TestCannotCompleteException();
    }
    test.complete(userContext.getUserId());
    testRepository.save(test);

    // TODO: Update student points

  }

  public void updateTestStudentAttempts(
      Long testId, Long mcqId, int attemptOption, UserContext userContext) {
    TestEntity test = getTest(testId);
    if (!test.getStatus().equals(IN_PROGRESS)) {
      throw new TestCannotUpdateAttemptException();
    }

    TestStudent testStudent =
        testStudentRepository
            .findOneByTestIdAndStudentId(testId, userContext.getUserId())
            .orElseThrow(() -> new NotFoundException("Student not found in this test"));
    if (testStudent.getPoints() != null) {
      throw new TestCannotUpdateAttemptException();
    }

    TestAttempt attempt =
        testAttemptRepository
            .findOneByTestIdAndMcqIdAndStudentId(testId, mcqId, userContext.getUserId())
            .orElseThrow(() -> new NotFoundException("Attempt not found"));
    attempt.updateAttempt(attemptOption);
    testAttemptRepository.save(attempt);
  }

  public void summitStudentTest(Long testId, UserContext userContext) {
    TestEntity test = getTest(testId);
    if (!test.getStatus().equals(IN_PROGRESS)) {
      throw new TestCannotSummitException();
    }

    updateTestStudentPoints(testId, userContext);
  }

  @Cacheable(value = "mcqs", key = "#testId")
  public RetrieveMCQResponse retrieveMCQResponse(
      Long testId, UserContext userContext, List<Long> mcqIds) {
    return questionClient.retrieveMCQsByIds(
        RetrieveMCQByIdsRequest.builder().ids(mcqIds).pageNumber(0).pageSize(60).build(),
        userContext.getUserId(),
        userContext.getUserEmail(),
        userContext.getUserRoles());
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
        self.retrieveMCQResponse(
            testId, userContext, testMcqs.stream().map(TestMcqs::getMcqId).toList());
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

  private void updateTestStudentPoints(Long testId, UserContext userContext) {
    List<TestAttempt> allAttempts =
        testAttemptRepository.findByTestIdAndStudentId(testId, userContext.getUserId());
    RetrieveMCQResponse retrieveMCQResponse =
        self.retrieveMCQResponse(
            testId, userContext, allAttempts.stream().map(TestAttempt::getMcqId).toList());

    int points =
        (int)
            allAttempts.stream()
                .filter(
                    attemptItem ->
                        retrieveMCQResponse.getMcqs().stream()
                            .filter(mcq -> mcq.getId().equals(attemptItem.getMcqId()))
                            .findFirst()
                            .map(
                                mcqDto ->
                                    mcqDto.getOptions().stream()
                                        .filter(MCQDto.OptionDto::getIsAnswer)
                                        .findFirst()
                                        .filter(
                                            optionDto ->
                                                optionDto.getNo().equals(attemptItem.getOptionNo()))
                                        .isPresent())
                            .orElse(false))
                .count();
    TestStudent testStudent =
        testStudentRepository
            .findOneByTestIdAndStudentId(testId, userContext.getUserId())
            .orElseThrow(() -> new NotFoundException("Student not found in this test"));
    testStudent.updatePoints(points);
    testStudentRepository.save(testStudent);
  }
}
