package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.fixture.TestFixture.*;
import static com.quemistry.quiz_ms.model.TestStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.controller.model.*;
import com.quemistry.quiz_ms.exception.InProgressTestAlreadyExistsException;
import com.quemistry.quiz_ms.exception.NotFoundException;
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

  @InjectMocks private TestService testService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateTest() {

    when(testRepository.existsByTutorIdAndStatus(TUTOR_ID, IN_PROGRESS)).thenReturn(false);
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
                        && testEntity.getStatus().equals(IN_PROGRESS)));
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
  void testCreateTestInProgressTestExists() {
    when(testRepository.existsByTutorIdAndStatus(TUTOR_ID, IN_PROGRESS)).thenReturn(true);

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));
    testRequest.setTitle(TEST_TITLE);

    assertThrows(
        InProgressTestAlreadyExistsException.class,
        () -> testService.createTest(TUTOR_ID, testRequest));

    verify(testRepository, times(0)).save(any());
    verify(testMcqRepository, times(0)).save(any());
    verify(testStudentRepository, times(0)).save(any());
    verify(testAttemptRepository, times(0)).save(any());
  }

  @Test
  void getTestsForTutorTest() {
    when(testRepository.findPageByTutorIdOrderByIdDesc(
            TUTOR_ID, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestEntity> testEntities =
        testService.getTestsForTutor(TUTOR_ID, PAGE_NUMBER, PAGE_SIZE, tutorContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestEntity test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(IN_PROGRESS, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
  }

  @Test
  void getTestsForStudentTest() {
    when(testStudentRepository.findPageByStudentIdOrderByTestIdDesc(
            STUDENT_ID, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    TestStudent.builder().testId(TEST_ID).studentId(STUDENT_ID).points(10).build()),
                PageRequest.of(PAGE_NUMBER, PAGE_SIZE),
                TOTAL_RECORDS));
    when(testRepository.findAllById(List.of(TEST_ID))).thenReturn(List.of(testEntity));

    Page<TestEntity> testEntities =
        testService.getTestsForStudent(STUDENT_ID, PAGE_NUMBER, PAGE_SIZE, studentContext);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestEntity test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(IN_PROGRESS, test.getStatus());
    assertEquals(TEST_TITLE, test.getTitle());
  }

  @Test
  void getTestMcqDetailTest() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.of(testEntity));
    when(testMcqRepository.findByTestId(TEST_ID)).thenReturn(List.of(testMcqs));
    when(questionClient.retrieveMCQsByIds(
            argThat(request -> request.getIds().contains(MCQ_ID)), any(), any(), any()))
        .thenReturn(getRetrieveMCQResponse());
    when(testAttemptRepository.findByTestId(TEST_ID)).thenReturn(List.of(testAttempt));

    TestMcqDetailResponse testMcqDetailResponse =
        testService.getTestMcqDetail(TEST_ID, tutorContext);

    assertNotNull(testMcqDetailResponse);
    assertEquals(TEST_ID, testMcqDetailResponse.getId());
    assertEquals(TUTOR_ID, testMcqDetailResponse.getTutorId());
    assertEquals(IN_PROGRESS, testMcqDetailResponse.getStatus());
    assertEquals(TEST_TITLE, testMcqDetailResponse.getTitle());

    assertEquals(1, testMcqDetailResponse.getTotalStudentsCount());

    TestMcqDetailResponse.TestMcqResponse testMcqResponse =
        testMcqDetailResponse.getMcqs().getFirst();
    assertEquals(MCQ_ID, testMcqResponse.getId());
    assertEquals(1, testMcqResponse.getAttemptStudentsCount());
    assertEquals(1, testMcqResponse.getCorrectStudentsCount());
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

    TestStudentDetailResponse testStudentDetailResponse =
        testService.getTestStudentDetail(TEST_ID, tutorContext);

    assertNotNull(testStudentDetailResponse);
    assertEquals(TEST_ID, testStudentDetailResponse.getId());
    assertEquals(IN_PROGRESS, testStudentDetailResponse.getStatus());
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

    TestStudentAttemptResponse testStudentAttemptResponse =
        testService.getTestStudentAttempts(TEST_ID, STUDENT_ID, tutorContext);

    assertNotNull(testStudentAttemptResponse);
    assertEquals(TEST_ID, testStudentAttemptResponse.getId());
    assertEquals(IN_PROGRESS, testStudentAttemptResponse.getStatus());
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
    assertEquals(IN_PROGRESS, testMcqAttemptResponse.getTestStatus());
    assertEquals(TEST_TITLE, testMcqAttemptResponse.getTitle());
    assertEquals(TUTOR_ID, testMcqAttemptResponse.getTutorId());
    assertEquals(MCQ_INDEX, testMcqAttemptResponse.getIndex());

    assertEquals(STUDENT_ID, testMcqAttemptResponse.getAttempts().getFirst().getStudentId());
    assertEquals(CURRENT_OPTION_NO, testMcqAttemptResponse.getAttempts().getFirst().getOptionNo());
  }
}
