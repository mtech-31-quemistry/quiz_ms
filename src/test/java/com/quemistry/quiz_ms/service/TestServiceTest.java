package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.fixture.TestFixture.*;
import static com.quemistry.quiz_ms.model.TestStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.*;
import com.quemistry.quiz_ms.model.*;
import com.quemistry.quiz_ms.repository.TestAttemptRepository;
import com.quemistry.quiz_ms.repository.TestMcqRepository;
import com.quemistry.quiz_ms.repository.TestRepository;
import com.quemistry.quiz_ms.repository.TestStudentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class TestServiceTest {
  @Mock private TestRepository testRepository;

  @Mock private TestMcqRepository testMcqRepository;

  @Mock private TestStudentRepository testStudentRepository;

  @Mock private TestAttemptRepository testAttemptRepository;

  @Mock private QuestionClient questionClient;

  @Mock private TestService self;

  @InjectMocks private TestService testService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateTest() {

    when(testRepository.existsByTutorIdAndStatus(TUTOR_ID, DRAFT)).thenReturn(false);
    when(testRepository.save(any())).thenReturn(TestEntity.builder().id(TEST_ID).build());

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));
    testRequest.setTitle(TEST_TITLE);

    Long actualTestId = testService.createTest(TUTOR_ID, testRequest);

    assertNotNull(actualTestId);
    assertEquals(TEST_ID, actualTestId);
    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getTutorId().equals(TUTOR_ID)
                        && testEntity.getStatus().equals(DRAFT)
                        && testEntity.getTitle().equals(TEST_TITLE)
                        && testEntity.getCreatedBy().equals(TUTOR_ID)));
    verify(testMcqRepository, times(1))
        .save(
            argThat(
                testMcqs ->
                    testMcqs.getTestId().equals(TEST_ID)
                        && testMcqs.getMcqId().equals(MCQ_ID)
                        && testMcqs.getIndex().equals(MCQ_INDEX)));
    verify(testStudentRepository, times(1))
        .save(
            argThat(
                testStudent ->
                    testStudent.getTestId().equals(TEST_ID)
                        && testStudent.getStudentId().equals(STUDENT_ID)
                        && testStudent.getPoints() == null));
    verify(testAttemptRepository, times(1))
        .save(
            argThat(
                testAttempt ->
                    testAttempt.getTestId().equals(TEST_ID)
                        && testAttempt.getMcqId().equals(MCQ_ID)
                        && testAttempt.getStudentId().equals(STUDENT_ID)
                        && testAttempt.getOptionNo() == null));
  }

  @Test
  void updateTestTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(DRAFT).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));
    testRequest.setTitle(TEST_TITLE);

    testService.updateTest(TEST_ID, testRequest, tutorContext);

    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getId().equals(TEST_ID)
                        && testEntity.getStatus().equals(DRAFT)
                        && testEntity.getTitle().equals(TEST_TITLE)));
    verify(testMcqRepository, times(1))
        .save(
            argThat(
                testMcqs ->
                    testMcqs.getTestId().equals(TEST_ID)
                        && testMcqs.getMcqId().equals(MCQ_ID)
                        && testMcqs.getIndex().equals(MCQ_INDEX)));
    verify(testStudentRepository, times(1))
        .save(
            argThat(
                testStudent ->
                    testStudent.getTestId().equals(TEST_ID)
                        && testStudent.getStudentId().equals(STUDENT_ID)
                        && testStudent.getPoints() == null));
    verify(testAttemptRepository, times(1))
        .save(
            argThat(
                testAttempt ->
                    testAttempt.getTestId().equals(TEST_ID)
                        && testAttempt.getMcqId().equals(MCQ_ID)
                        && testAttempt.getStudentId().equals(STUDENT_ID)
                        && testAttempt.getOptionNo() == null));
  }

  @Test
  void updateTestTestTestNotFound() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));
    testRequest.setTitle(TEST_TITLE);

    assertThrows(
        NotFoundException.class,
        () -> testService.updateTest(TEST_ID, testRequest, tutorContext),
        "Test not found");
  }

  @Test
  void getTestsForTutorTest() {
    when(testRepository.findPageByTutorIdOrderByIdDesc(
            TUTOR_ID, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestEntity> testEntities =
        testService.getTestsForTutor(TUTOR_ID, null, PAGE_NUMBER, PAGE_SIZE, tutorContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestEntity test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(DRAFT, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
  }

  @Test
  void getTestsForTutorTestWithSearchConditions() {
    when(testRepository.findPageByTutorIdAndTitleContainingOrderByIdDesc(
            TUTOR_ID, TEST_TITLE, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestEntity> testEntities =
        testService.getTestsForTutor(TUTOR_ID, TEST_TITLE, PAGE_NUMBER, PAGE_SIZE, tutorContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestEntity test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(DRAFT, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
  }

  @Test
  void getTestsForStudentTest() {
    when(testStudentRepository.findByStudentId(STUDENT_ID))
        .thenReturn(
            List.of(
                TestStudent.builder().testId(TEST_ID).studentId(STUDENT_ID).points(10).build()));
    when(testRepository.findPageByIdInAndStatusIsNot(
            List.of(TEST_ID), DRAFT, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestResponseForStudent> testEntities =
        testService.getTestsForStudent(STUDENT_ID, null, PAGE_NUMBER, PAGE_SIZE, studentContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestResponseForStudent test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(DRAFT, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
    assertEquals(10, test.getPoints());
  }

  @Test
  void getTestsForStudentTestWithSearchConditions() {
    when(testStudentRepository.findByStudentId(STUDENT_ID))
        .thenReturn(
            List.of(
                TestStudent.builder().testId(TEST_ID).studentId(STUDENT_ID).points(null).build()));
    when(testRepository.findPageByIdInAndStatusIsNotAndTitleContaining(
            List.of(TEST_ID), DRAFT, TEST_TITLE, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestResponseForStudent> testEntities =
        testService.getTestsForStudent(
            STUDENT_ID, TEST_TITLE, PAGE_NUMBER, PAGE_SIZE, studentContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestResponseForStudent test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(DRAFT, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
    assertNull(test.getPoints());
  }

  @Test
  void getTestMcqDetailTest() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestId(TEST_ID)).thenReturn(List.of(testAttempt));
    when(self.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestMcqDetailResponse testMcqDetailResponse =
        testService.getTestMcqDetail(TEST_ID, tutorContext);

    assertNotNull(testMcqDetailResponse);
    assertEquals(TEST_ID, testMcqDetailResponse.getId());
    assertEquals(TUTOR_ID, testMcqDetailResponse.getTutorId());
    assertEquals(DRAFT, testMcqDetailResponse.getStatus());
    assertEquals(TEST_TITLE, testMcqDetailResponse.getTitle());

    assertEquals(1, testMcqDetailResponse.getTotalStudentsCount());

    TestMcqDetailResponse.TestMcqResponse testMcqResponse =
        testMcqDetailResponse.getMcqs().getFirst();
    assertEquals(MCQ_ID, testMcqResponse.getId());
    assertEquals(1, testMcqResponse.getAttemptStudentsCount());
    assertEquals(1, testMcqResponse.getCorrectStudentsCount());
  }

  @Test
  void getMyTestMcqDetailReturnsFullOptionWhenTestIsCompleted() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(COMPLETED).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestId(TEST_ID)).thenReturn(List.of(testAttempt));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestMcqDetailResponse testMcqDetailResponse =
        testService.getMyTestMcqDetail(TEST_ID, studentContext);

    TestMcqDetailResponse.TestMcqResponse testMcqResponse =
        testMcqDetailResponse.getMcqs().getFirst();
    assertEquals(4, testMcqResponse.getOptions().size());
    testMcqResponse
        .getOptions()
        .forEach(
            option -> {
              assertNotNull(option.getExplanation());
              assertNotNull(option.getIsAnswer());
            });
  }

  @Test
  void getMyTestMcqDetailReturnsPartialOptionWhenTestIsInProgress() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestId(TEST_ID)).thenReturn(List.of(testAttempt));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestMcqDetailResponse testMcqDetailResponse =
        testService.getMyTestMcqDetail(TEST_ID, studentContext);

    TestMcqDetailResponse.TestMcqResponse testMcqResponse =
        testMcqDetailResponse.getMcqs().getFirst();
    assertEquals(4, testMcqResponse.getOptions().size());
    testMcqResponse
        .getOptions()
        .forEach(
            option -> {
              assertNull(option.getExplanation());
              assertNull(option.getIsAnswer());
            });
  }

  @Test
  void getTestMcqDetailTestTestNotFound() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class, () -> testService.getTestMcqDetail(TEST_ID, tutorContext));

    verify(testMcqRepository, times(0)).findByTestId(TEST_ID);
    verify(questionClient, times(0)).retrieveMCQsByIds(any(), any(), any(), any());
    verify(testAttemptRepository, times(0)).findByTestId(TEST_ID);
  }

  @Test
  void getTestStudentDetailTest() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testStudentRepository.findByTestId(TEST_ID)).thenReturn(List.of(testStudent));
    when(testAttemptRepository.findByTestId(TEST_ID)).thenReturn(List.of(testAttempt));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(self.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestStudentDetailResponse testStudentDetailResponse =
        testService.getTestStudentDetail(TEST_ID, tutorContext);

    assertNotNull(testStudentDetailResponse);
    assertEquals(TEST_ID, testStudentDetailResponse.getId());
    assertEquals(DRAFT, testStudentDetailResponse.getStatus());
    assertEquals(TEST_TITLE, testStudentDetailResponse.getTitle());
    assertEquals(TUTOR_ID, testStudentDetailResponse.getTutorId());

    assertEquals(1, testStudentDetailResponse.getTotalMcqCount());

    TestStudentDetailResponse.TestStudentResponse testStudentResponse =
        testStudentDetailResponse.getStudents().getFirst();
    assertEquals(STUDENT_ID, testStudentResponse.getStudentId());
    assertEquals(STUDENT_POINTS, testStudentResponse.getPoints());
    assertEquals(1, testStudentResponse.getAttemptMcqCount());
    assertEquals(1, testStudentResponse.getCorrectMcqCount());
  }

  @Test
  void getTestStudentAttemptsTest() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));
    when(self.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestStudentAttemptResponse testStudentAttemptResponse =
        testService.getTestStudentAttempts(TEST_ID, STUDENT_ID, tutorContext);

    assertNotNull(testStudentAttemptResponse);
    assertEquals(TEST_ID, testStudentAttemptResponse.getId());
    assertEquals(DRAFT, testStudentAttemptResponse.getStatus());
    assertEquals(TEST_TITLE, testStudentAttemptResponse.getTitle());
    assertEquals(TUTOR_ID, testStudentAttemptResponse.getTutorId());
    assertEquals(STUDENT_ID, testStudentAttemptResponse.getStudentId());

    assertEquals(STUDENT_POINTS, testStudentAttemptResponse.getPoints());

    TestStudentAttemptResponse.StudentMcqResponse studentMcqResponse =
        testStudentAttemptResponse.getMcqs().getFirst();
    assertEquals(MCQ_ID, studentMcqResponse.getId());
    assertEquals(MCQ_INDEX, studentMcqResponse.getIndex());
    assertEquals(CURRENT_OPTION_NO, studentMcqResponse.getAttemptOption());
  }

  @Test
  void getMyTestStudentAttemptsReturnsFullOptionWhenTestIsCompleted() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(COMPLETED).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestStudentAttemptResponse testStudentAttemptResponse =
        testService.getMyTestStudentAttempts(TEST_ID, studentContext);

    TestStudentAttemptResponse.StudentMcqResponse studentMcqResponse =
        testStudentAttemptResponse.getMcqs().getFirst();
    assertEquals(4, studentMcqResponse.getOptions().size());
    studentMcqResponse
        .getOptions()
        .forEach(
            option -> {
              assertNotNull(option.getExplanation());
              assertNotNull(option.getIsAnswer());
            });
  }

  @Test
  void getMyTestStudentAttemptsReturnsPartialOptionWhenTestIsInProgress() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    TestStudentAttemptResponse testStudentAttemptResponse =
        testService.getMyTestStudentAttempts(TEST_ID, studentContext);

    TestStudentAttemptResponse.StudentMcqResponse studentMcqResponse =
        testStudentAttemptResponse.getMcqs().getFirst();
    assertEquals(4, studentMcqResponse.getOptions().size());
    studentMcqResponse
        .getOptions()
        .forEach(
            option -> {
              assertNull(option.getExplanation());
              assertNull(option.getIsAnswer());
            });
  }

  @Test
  void getTestStudentAttemptsTestStudentNotFound() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.empty());
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(self.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    assertThrows(
        NotFoundException.class,
        () -> testService.getTestStudentAttempts(TEST_ID, STUDENT_ID, tutorContext),
        "Student not found");
  }

  @Test
  void TestMcqAttemptResponseTest() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findOneByTestIdAndMcqId(TEST_ID, MCQ_ID))
        .thenReturn(Optional.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestIdAndMcqId(TEST_ID, MCQ_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));

    TestMcqAttemptResponse testMcqAttemptResponse =
        testService.getTestMcqAttempts(TEST_ID, MCQ_ID, tutorContext);

    assertNotNull(testMcqAttemptResponse);
    assertEquals(TEST_ID, testMcqAttemptResponse.getId());
    assertEquals(DRAFT, testMcqAttemptResponse.getTestStatus());
    assertEquals(TEST_TITLE, testMcqAttemptResponse.getTitle());
    assertEquals(TUTOR_ID, testMcqAttemptResponse.getTutorId());
    assertEquals(MCQ_INDEX, testMcqAttemptResponse.getIndex());

    assertEquals(STUDENT_ID, testMcqAttemptResponse.getAttempts().getFirst().getStudentId());
    assertEquals(CURRENT_OPTION_NO, testMcqAttemptResponse.getAttempts().getFirst().getOptionNo());
  }

  @Test
  void TestMcqAttemptResponseTestMcqNotFound() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findOneByTestIdAndMcqId(TEST_ID, MCQ_ID)).thenReturn(Optional.empty());
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestIdAndMcqId(TEST_ID, MCQ_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));

    assertThrows(
        NotFoundException.class,
        () -> testService.getTestMcqAttempts(TEST_ID, MCQ_ID, tutorContext),
        "MCQ not found");
  }

  @Test
  void TestMcqAttemptResponseTestMcqNotFoundFromQuestionMS() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findOneByTestIdAndMcqId(TEST_ID, MCQ_ID))
        .thenReturn(Optional.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(RetrieveMCQResponse.builder().mcqs(List.of()).build());
    when(testAttemptRepository.findByTestIdAndMcqId(TEST_ID, MCQ_ID))
        .thenReturn(List.of(testAttempt));
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));

    assertThrows(
        NotFoundException.class,
        () -> testService.getTestMcqAttempts(TEST_ID, MCQ_ID, tutorContext),
        "MCQ not found in question service");
  }

  @Test
  void startTestTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(DRAFT).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    testService.startTest(TEST_ID, tutorContext);

    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getId().equals(TEST_ID)
                        && testEntity.getStartedBy().equals(TUTOR_ID)
                        && testEntity.getStartedOn().equals(testEntity.getUpdatedOn())
                        && testEntity.getStatus().equals(IN_PROGRESS)));
  }

  @Test
  void startTestTestWithIncorrectStatus() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    assertThrows(
        TestCannotStartException.class, () -> testService.startTest(TEST_ID, tutorContext));
  }

  @Test
  void completeTestTestAndSummitStudentTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    TestStudent notSummitedStudent =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findByTestId(TEST_ID)).thenReturn(List.of(notSummitedStudent));
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));
    when(self.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));

    testService.completeTest(TEST_ID, tutorContext);

    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getId().equals(TEST_ID)
                        && testEntity.getCompletedBy().equals(TUTOR_ID)
                        && testEntity.getCompletedOn().equals(testEntity.getUpdatedOn())
                        && testEntity.getStatus().equals(COMPLETED)));
    verify(testStudentRepository, times(1))
        .save(
            argThat(
                testStudent ->
                    testStudent.getTestId().equals(TEST_ID)
                        && testStudent.getStudentId().equals(STUDENT_ID)
                        && testStudent.getPoints().equals(1)));
  }

  @Test
  void completeTestTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testStudentRepository.findByTestId(TEST_ID)).thenReturn(List.of(testStudent));

    testService.completeTest(TEST_ID, tutorContext);

    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getId().equals(TEST_ID)
                        && testEntity.getCompletedBy().equals(TUTOR_ID)
                        && testEntity.getCompletedOn().equals(testEntity.getUpdatedOn())
                        && testEntity.getStatus().equals(COMPLETED)));
    verify(testStudentRepository, times(0)).save(any());
  }

  @Test
  void completeTestTestWithIncorrectStatus() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(DRAFT).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    assertThrows(
        TestCannotCompleteException.class, () -> testService.completeTest(TEST_ID, tutorContext));
  }

  @Test
  void updateTestStudentAttemptsTest() {
    when(testAttemptRepository.findOneByTestIdAndMcqIdAndStudentId(TEST_ID, MCQ_ID, STUDENT_ID))
        .thenReturn(Optional.of(testAttempt));
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    TestStudent testStudentData =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudentData));

    testService.updateTestStudentAttempts(TEST_ID, MCQ_ID, CURRENT_OPTION_NO, studentContext);

    verify(testAttemptRepository, times(1))
        .save(
            argThat(
                testAttempt ->
                    testAttempt.getTestId().equals(TEST_ID)
                        && testAttempt.getMcqId().equals(MCQ_ID)
                        && testAttempt.getStudentId().equals(STUDENT_ID)
                        && testAttempt.getOptionNo().equals(CURRENT_OPTION_NO)));
  }

  @Test
  void updateTestStudentAttemptsTestFailedWhenTestIsDraft() {
    when(testAttemptRepository.findOneByTestIdAndMcqIdAndStudentId(TEST_ID, MCQ_ID, STUDENT_ID))
        .thenReturn(Optional.of(testAttempt));
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));

    assertThrows(
        TestCannotUpdateAttemptException.class,
        () ->
            testService.updateTestStudentAttempts(
                TEST_ID, MCQ_ID, CURRENT_OPTION_NO, studentContext));
  }

  @Test
  void updateTestStudentAttemptsTestFailedWhenTestAlreadyGotPoints() {
    when(testAttemptRepository.findOneByTestIdAndMcqIdAndStudentId(TEST_ID, MCQ_ID, STUDENT_ID))
        .thenReturn(Optional.of(testAttempt));
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    TestStudent testStudentData =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudentData));

    assertThrows(
        TestCannotUpdateAttemptException.class,
        () ->
            testService.updateTestStudentAttempts(
                TEST_ID, MCQ_ID, CURRENT_OPTION_NO, studentContext));
  }

  @Test
  void summitStudentTestTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    TestStudent testStudentData =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudentData));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    testService.summitStudentTest(TEST_ID, studentContext);

    verify(testStudentRepository, times(1))
        .save(
            argThat(
                data ->
                    data.getTestId().equals(TEST_ID)
                        && data.getStudentId().equals(STUDENT_ID)
                        && data.getPoints().equals(1)));
  }

  @Test
  void summitStudentTestTestFailedWhenTestIsDraft() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));

    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());

    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudent));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    assertThrows(
        TestCannotSummitException.class,
        () -> testService.summitStudentTest(TEST_ID, studentContext));
  }

  @Test
  void summitStudentTestTestFailedWhenStudentNotFoundInTest() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));

    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(testAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.empty());
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    assertThrows(
        NotFoundException.class,
        () -> testService.summitStudentTest(TEST_ID, studentContext),
        "Student not found in test");
  }

  @Test
  void summitStudentTestTestGet0PointWhenNoCorrectAnswer() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    TestAttempt inCorrectTestAttempt =
        TestAttempt.builder()
            .studentId(STUDENT_ID)
            .testId(TEST_ID)
            .mcqId(MCQ_ID)
            .optionNo(2)
            .build();
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(inCorrectTestAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    TestStudent testStudentData =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudentData));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    testService.summitStudentTest(TEST_ID, studentContext);

    verify(testStudentRepository, times(1))
        .save(
            argThat(
                data ->
                    data.getTestId().equals(TEST_ID)
                        && data.getStudentId().equals(STUDENT_ID)
                        && data.getPoints().equals(0)));
  }

  @Test
  void summitStudentTestTestGet0PointWhenNoAnswer() {
    TestEntity test = TestEntity.builder().id(TEST_ID).status(IN_PROGRESS).build();
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(test));
    TestAttempt noAnswerTestAttempt =
        TestAttempt.builder()
            .studentId(STUDENT_ID)
            .testId(TEST_ID)
            .mcqId(MCQ_ID)
            .optionNo(null)
            .build();
    when(testAttemptRepository.findByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(List.of(noAnswerTestAttempt));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    TestStudent testStudentData =
        TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(null).build();
    when(testStudentRepository.findOneByTestIdAndStudentId(TEST_ID, STUDENT_ID))
        .thenReturn(Optional.of(testStudentData));
    when(self.getTestMcqs(TEST_ID, studentContext, List.of(MCQ_ID)))
        .thenReturn(getRetrieveMCQResponse());

    testService.summitStudentTest(TEST_ID, studentContext);

    verify(testStudentRepository, times(1))
        .save(
            argThat(
                data ->
                    data.getTestId().equals(TEST_ID)
                        && data.getStudentId().equals(STUDENT_ID)
                        && data.getPoints().equals(0)));
  }

  @Test
  void getTestMcqsTest() {
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());

    RetrieveMCQResponse retrieveMCQResponse =
        testService.getTestMcqs(TEST_ID, tutorContext, List.of(MCQ_ID));

    assertNotNull(retrieveMCQResponse);
    assertEquals(1, retrieveMCQResponse.getTotalRecords());
    assertEquals(PAGE_NUMBER, retrieveMCQResponse.getPageNumber());
    assertEquals(PAGE_SIZE, retrieveMCQResponse.getPageSize());
    assertEquals(1, retrieveMCQResponse.getTotalPages());

    assertEquals(MCQ_ID, retrieveMCQResponse.getMcqs().getFirst().getId());
  }
}
