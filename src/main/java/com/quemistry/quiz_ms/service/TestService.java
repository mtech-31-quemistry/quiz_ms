package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.mapper.MCQMapper.INSTANCE;
import static com.quemistry.quiz_ms.model.TestStatus.*;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.UserClient;
import com.quemistry.quiz_ms.client.model.*;
import com.quemistry.quiz_ms.client.model.SearchStudentResponse.StudentResponse;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.*;
import com.quemistry.quiz_ms.mapper.MCQMapper;
import com.quemistry.quiz_ms.model.*;
import com.quemistry.quiz_ms.repository.TestAttemptRepository;
import com.quemistry.quiz_ms.repository.TestMcqRepository;
import com.quemistry.quiz_ms.repository.TestRepository;
import com.quemistry.quiz_ms.repository.TestStudentRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
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
  private final UserClient userClient;
  private final TestService self;
  private final MCQMapper mcqMapper = INSTANCE;

  public TestService(
      TestRepository testRepository,
      TestMcqRepository testMcqRepository,
      TestStudentRepository testStudentRepository,
      TestAttemptRepository testAttemptRepository,
      QuestionClient questionClient,
      UserClient userClient,
      TestService testService) {
    this.testRepository = testRepository;
    this.testMcqRepository = testMcqRepository;
    this.testStudentRepository = testStudentRepository;
    this.testAttemptRepository = testAttemptRepository;
    this.questionClient = questionClient;
    this.userClient = userClient;
    this.self = testService;
  }

  public Long createTest(String tutorId, TestRequest testRequest) {
    TestEntity test = TestEntity.create(tutorId, testRequest.getTitle());
    Long testId = testRepository.save(test).getId();

    saveTestMcqAndStudentAndAttempt(testRequest, testId);

    return testId;
  }

  @Transactional
  public void updateTest(Long testId, TestRequest testRequest, UserContext userContext) {
    TestEntity test = getTest(testId);
    if (!test.getStatus().equals(DRAFT)) {
      throw new TestCannotUpdateException();
    }

    test.update(testRequest);
    testRepository.save(test);

    testMcqRepository.deleteByTestId(testId);
    testStudentRepository.deleteByTestId(testId);
    testAttemptRepository.deleteByTestId(testId);

    saveTestMcqAndStudentAndAttempt(testRequest, testId);
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

  public Page<TestResponseForStudent> getTestsForStudent(
      String studentId,
      String search,
      Integer pageNumber,
      Integer pageSize,
      UserContext userContext) {
    List<TestStudent> testStudents = testStudentRepository.findByStudentId(studentId);
    List<Long> testIds = testStudents.stream().map(TestStudent::getTestId).toList();
    Page<TestEntity> pagedTests =
        search != null
            ? testRepository.findPageByIdInAndStatusIsNotAndTitleContaining(
                testIds, DRAFT, search, PageRequest.of(pageNumber, pageSize))
            : testRepository.findPageByIdInAndStatusIsNot(
                testIds, DRAFT, PageRequest.of(pageNumber, pageSize));
    List<TestStudent> testStudentList = testStudentRepository.findByStudentId(studentId);

    return pagedTests.map(
        test ->
            TestResponseForStudent.from(
                test,
                Objects.requireNonNull(
                        testStudentList.stream()
                            .filter(testStudent -> testStudent.getTestId().equals(test.getId()))
                            .findFirst()
                            .orElse(null))
                    .getPoints()));
  }

  public TestMcqDetailResponse getTestMcqDetail(Long testId, UserContext userContext) {
    var testData = getTestData(testId, userContext);
    return TestMcqDetailResponse.from(
        testData.getValue0(), testData.getValue1(), testData.getValue2(), testData.getValue3());
  }

  public TestMcqDetailResponse getMyTestMcqDetail(Long testId, UserContext userContext) {
    TestMcqDetailResponse testMcqDetail = getTestMcqDetail(testId, userContext);
    if (!testMcqDetail.getStatus().equals(COMPLETED)) {
      testMcqDetail
          .getMcqs()
          .forEach(
              mcq -> {
                mcq.setAttemptStudentsCount(null);
                mcq.setCorrectStudentsCount(null);
                mcq.getOptions().forEach(MCQDto.OptionDto::maskAnswer);
              });
      testMcqDetail.setTotalStudentsCount(null);
    }

    return testMcqDetail;
  }

  public TestStudentDetailResponse getTestStudentDetail(Long testId, UserContext userContext) {
    var testData = getTestData(testId, userContext);
    List<TestStudent> testStudents = testStudentRepository.findByTestId(testId);

    List<StudentResponse> studentResponses =
        self.searchStudent(
            testId, testStudents.stream().map(TestStudent::getStudentId).toList(), userContext);

    return TestStudentDetailResponse.from(
        testData.getValue0(),
        testData.getValue1(),
        testData.getValue2(),
        testData.getValue3(),
        testStudents,
        studentResponses);
  }

  public TestStudentAttemptResponse getTestStudentAttempts(
      Long testId, String studentId, UserContext userContext) {
    var testMcqDetail = getMcqDetail(testId, userContext);
    var attempts = testAttemptRepository.findByTestIdAndStudentId(testId, studentId);
    var student = testStudentRepository.findOneByTestIdAndStudentId(testId, studentId);

    if (student.isEmpty()) {
      throw new NotFoundException("Student not found");
    }

    StudentResponse studentResponses =
        self.searchStudent(testId, List.of(studentId), userContext).getFirst();

    return TestStudentAttemptResponse.from(
        testMcqDetail.getValue0(),
        testMcqDetail.getValue1(),
        testMcqDetail.getValue2(),
        attempts,
        student.get(),
        studentResponses);
  }

  public TestStudentAttemptResponse getMyTestStudentAttempts(Long testId, UserContext userContext) {
    TestStudentAttemptResponse testStudentAttemptResponse =
        getTestStudentAttempts(testId, userContext.getUserId(), userContext);
    if (!testStudentAttemptResponse.getStatus().equals(COMPLETED)) {
      testStudentAttemptResponse
          .getMcqs()
          .forEach(mcq -> mcq.getOptions().forEach(MCQDto.OptionDto::maskAnswer));
    }

    return testStudentAttemptResponse;
  }

  public TestMcqAttemptResponse getTestMcqAttempts(
      Long testId, Long mcqId, UserContext userContext) {
    TestEntity test = getTest(testId);
    var testMcq = testMcqRepository.findOneByTestIdAndMcqId(testId, mcqId);
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
    List<StudentResponse> studentResponses =
        self.searchStudent(
            testId, attempts.stream().map(TestAttempt::getStudentId).toList(), userContext);

    return TestMcqAttemptResponse.from(
        test, testMcq.get().getIndex(), mcq.get(), attempts, studentResponses);
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

    testStudentRepository.findByTestId(testId).parallelStream()
        .filter(testStudent -> testStudent.getPoints() == null)
        .forEach(
            testStudent ->
                updateTestStudentPoints(testId, testStudent.getStudentId(), userContext));
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

  @Cacheable(value = "test_mcqs", key = "#testId")
  public RetrieveMCQResponse getTestMcqs(Long testId, UserContext userContext, List<Long> mcqIds) {
    return questionClient.retrieveMCQsByIds(
        RetrieveMCQByIdsRequest.builder().ids(mcqIds).pageNumber(0).pageSize(60).build(),
        userContext.getUserId(),
        userContext.getUserEmail(),
        userContext.getUserRoles());
  }

  @Cacheable(value = "students", key = "#testId")
  public List<StudentResponse> searchStudent(
      Long testId, List<String> studentIds, UserContext userContext) {
    return userClient
        .searchStudents(
            SearchStudentRequest.from(studentIds),
            userContext.getUserId(),
            userContext.getUserEmail(),
            userContext.getUserRoles())
        .getPayload();
  }

  private void saveTestMcqAndStudentAndAttempt(TestRequest testRequest, Long testId) {
    testRequest
        .getMcqs()
        .forEach(
            mcq -> {
              TestMcqs testMcqs = TestMcqs.create(testId, mcq.getMcqId(), mcq.getIndex());
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
  }

  private Quartet<TestEntity, List<TestMcqs>, List<MCQResponse>, List<TestAttempt>> getTestData(
      Long testId, UserContext userContext) {
    var testMcqDetail = getMcqDetail(testId, userContext);

    List<TestAttempt> attempts = testAttemptRepository.findByTestId(testId);
    return Quartet.with(
        testMcqDetail.getValue0(), testMcqDetail.getValue1(), testMcqDetail.getValue2(), attempts);
  }

  private Triplet<TestEntity, List<TestMcqs>, List<MCQResponse>> getMcqDetail(
      Long testId, UserContext userContext) {
    TestEntity test = getTest(testId);

    List<TestMcqs> testMcqs = testMcqRepository.findByTestId(testId);
    RetrieveMCQResponse retrieveMCQResponse =
        self.getTestMcqs(testId, userContext, testMcqs.stream().map(TestMcqs::getMcqId).toList());
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
    updateTestStudentPoints(testId, userContext.getUserId(), userContext);
  }

  private void updateTestStudentPoints(Long testId, String studentId, UserContext userContext) {
    var allAttempts = testAttemptRepository.findByTestIdAndStudentId(testId, studentId);
    RetrieveMCQResponse retrieveMCQResponse =
        self.getTestMcqs(
            testId, userContext, allAttempts.stream().map(TestAttempt::getMcqId).toList());

    int points =
        (int)
            allAttempts.stream()
                .filter(
                    attemptItem ->
                        retrieveMCQResponse.getMcqs().stream()
                            .anyMatch(
                                mcqItem ->
                                    mcqItem.getId().equals(attemptItem.getMcqId())
                                        && mcqItem.getOptions().stream()
                                            .anyMatch(
                                                optionItem ->
                                                    optionItem
                                                            .getNo()
                                                            .equals(attemptItem.getOptionNo())
                                                        && optionItem.getIsAnswer())))
                .count();
    TestStudent testStudent =
        testStudentRepository
            .findOneByTestIdAndStudentId(testId, studentId)
            .orElseThrow(() -> new NotFoundException("Student not found in this test"));
    testStudent.updatePoints(points);
    testStudentRepository.save(testStudent);
  }
}
