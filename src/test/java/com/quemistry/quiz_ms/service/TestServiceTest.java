package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.fixture.TestFixture.*;
import static com.quemistry.quiz_ms.model.TestStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.jsonzou.jmockdata.JMockData;
import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.controller.model.McqIndex;
import com.quemistry.quiz_ms.controller.model.TestMcqDetailResponse;
import com.quemistry.quiz_ms.controller.model.TestRequest;
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

  private UserContext userContext;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    userContext = new UserContext("user1", "user1@example.com", "ROLE_USER");
  }

  @Test
  void testCreateTest() {

    when(testRepository.existsByTutorIdAndStatus(TUTOR_ID, IN_PROGRESS)).thenReturn(false);
    when(testRepository.save(any())).thenReturn(TestEntity.builder().id(TEST_ID).build());

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));

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

    TestRequest testRequest = JMockData.mock(TestRequest.class);
    testRequest.setMcqs(List.of(new McqIndex(MCQ_ID, MCQ_INDEX)));
    testRequest.setStudentIds(List.of(STUDENT_ID));

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
    when(testRepository.findPageByTutorIdOOrderByIdDesc(
            TUTOR_ID, PageRequest.of(PAGE_NUMBER, PAGE_SIZE)))
        .thenReturn(
            new PageImpl<>(
                List.of(testEntity), PageRequest.of(PAGE_NUMBER, PAGE_SIZE), TOTAL_RECORDS));

    Page<TestEntity> testEntities = testService.getTestsForTutor(TUTOR_ID, PAGE_NUMBER, PAGE_SIZE);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());

    TestEntity test = testEntities.getContent().getFirst();
    assertEquals(TEST_ID, test.getId());
    assertEquals(TUTOR_ID, test.getTutorId());
    assertEquals(IN_PROGRESS, test.getStatus());
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
        testService.getTestsForStudent(STUDENT_ID, PAGE_NUMBER, PAGE_SIZE);

    assertNotNull(testEntities);
    assertEquals(TOTAL_RECORDS, testEntities.getTotalElements());
    assertEquals(TEST_ID, testEntities.getContent().getFirst().getId());
    assertEquals(TUTOR_ID, testEntities.getContent().getFirst().getTutorId());
    assertEquals(IN_PROGRESS, testEntities.getContent().getFirst().getStatus());
    assertEquals(PAGE_NUMBER, testEntities.getPageable().getPageNumber());
    assertEquals(PAGE_SIZE, testEntities.getPageable().getPageSize());
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
        testService.getTestMcqDetail(TEST_ID, userContext);

    assertNotNull(testMcqDetailResponse);
    assertEquals(TEST_ID, testMcqDetailResponse.getId());
    assertEquals(TUTOR_ID, testMcqDetailResponse.getTutorId());
    assertEquals(IN_PROGRESS, testMcqDetailResponse.getStatus());
    assertEquals(MCQ_ID, testMcqDetailResponse.getMcqs().getFirst().getId());

    assertEquals(1, testMcqDetailResponse.getTotalStudentsCount());
    assertEquals(1, testMcqDetailResponse.getMcqs().getFirst().getAttemptStudentsCount());
    assertEquals(1, testMcqDetailResponse.getMcqs().getFirst().getCorrectStudentsCount());
  }

  @Test
  void getTestMcqDetailTestTestNotFound() {
    when(testRepository.findById(TEST_ID)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> testService.getTestMcqDetail(TEST_ID, userContext));

    verify(testMcqRepository, times(0)).findByTestId(TEST_ID);
    verify(questionClient, times(0)).retrieveMCQsByIds(any(), any(), any(), any());
    verify(testAttemptRepository, times(0)).findByTestId(TEST_ID);
  }
}
