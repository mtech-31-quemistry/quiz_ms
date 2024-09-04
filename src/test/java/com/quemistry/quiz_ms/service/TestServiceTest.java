package com.quemistry.quiz_ms.service;

import static com.quemistry.quiz_ms.model.TestStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.jsonzou.jmockdata.JMockData;
import com.quemistry.quiz_ms.client.QuestionClient;
import com.quemistry.quiz_ms.controller.model.McqIndex;
import com.quemistry.quiz_ms.controller.model.TestRequest;
import com.quemistry.quiz_ms.exception.InProgressTestAlreadyExistsException;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestStudent;
import com.quemistry.quiz_ms.model.UserContext;
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
    String tutorId = "tutor 1";
    String studentId = "student 1";
    Long testId = 1L;
    Long mcqId = 2L;
    int mcqIndex = 1;
    when(testRepository.existsByTutorIdAndStatus(tutorId, IN_PROGRESS)).thenReturn(false);
    when(testRepository.save(any())).thenReturn(TestEntity.builder().id(testId).build());

    TestRequest testRequest = new TestRequest();
    testRequest.setMcqs(List.of(new McqIndex(mcqId, mcqIndex)));
    testRequest.setStudentIds(List.of(studentId));

    Long actualTestId = testService.createTest(tutorId, testRequest);

    assertNotNull(actualTestId);
    assertEquals(testId, actualTestId);
    verify(testRepository, times(1))
        .save(
            argThat(
                testEntity ->
                    testEntity.getTutorId().equals(tutorId)
                        && testEntity.getStatus().equals(IN_PROGRESS)));
    verify(testMcqRepository, times(1))
        .save(
            argThat(
                testMcqs ->
                    testMcqs.getTestId().equals(testId)
                        && testMcqs.getMcqId().equals(mcqId)
                        && testMcqs.getIndex().equals(mcqIndex)));
    verify(testStudentRepository, times(1))
        .save(
            argThat(
                testStudent ->
                    testStudent.getTestId().equals(testId)
                        && testStudent.getStudentId().equals(studentId)
                        && testStudent.getPoints() == null));
    verify(testAttemptRepository, times(1))
        .save(
            argThat(
                testAttempt ->
                    testAttempt.getTestId().equals(testId)
                        && testAttempt.getMcqId().equals(mcqId)
                        && testAttempt.getStudentId().equals(studentId)
                        && testAttempt.getOptionNo() == null));
  }

  @Test
  void testCreateTestInProgressTestExists() {
    String tutorId = "tutor 1";
    when(testRepository.existsByTutorIdAndStatus(tutorId, IN_PROGRESS)).thenReturn(true);

    TestRequest testRequest = JMockData.mock(TestRequest.class);
    testRequest.setMcqs(List.of(new McqIndex(1L, 1)));
    testRequest.setStudentIds(List.of("student 1"));

    assertThrows(
        InProgressTestAlreadyExistsException.class,
        () -> testService.createTest(tutorId, testRequest));

    verify(testRepository, times(0)).save(any());
    verify(testMcqRepository, times(0)).save(any());
    verify(testStudentRepository, times(0)).save(any());
    verify(testAttemptRepository, times(0)).save(any());
  }

  @Test
  void getTestsForTutorTest() {
    String tutorId = "tutor 1";
    Long testId = 1L;
    int pageNumber = 0;
    int pageSize = 10;
    int totalTests = 1;
    TestEntity testEntity = new TestEntity();
    testEntity.setId(testId);
    testEntity.setStatus(IN_PROGRESS);
    testEntity.setTutorId(tutorId);
    when(testRepository.findPageByTutorIdOOrderByIdDesc(
            tutorId, PageRequest.of(pageNumber, pageSize)))
        .thenReturn(
            new PageImpl<>(List.of(testEntity), PageRequest.of(pageNumber, pageSize), totalTests));

    Page<TestEntity> testEntities = testService.getTestsForTutor(tutorId, pageNumber, pageSize);

    assertNotNull(testEntities);
    assertEquals(totalTests, testEntities.getTotalElements());
    assertEquals(testId, testEntities.getContent().getFirst().getId());
    assertEquals(tutorId, testEntities.getContent().getFirst().getTutorId());
    assertEquals(IN_PROGRESS, testEntities.getContent().getFirst().getStatus());
    assertEquals(pageNumber, testEntities.getPageable().getPageNumber());
    assertEquals(pageSize, testEntities.getPageable().getPageSize());
  }

  @Test
  void getTestsForStudentTest() {
    String studentId = "student 1";
    Long testId = 1L;
    int pageNumber = 0;
    int pageSize = 10;
    int totalTests = 1;
    TestEntity testEntity = new TestEntity();
    testEntity.setId(testId);
    testEntity.setStatus(IN_PROGRESS);
    testEntity.setTutorId("tutor 1");
    when(testStudentRepository.findPageByStudentIdOrderByTestIdDesc(
            studentId, PageRequest.of(pageNumber, pageSize)))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    TestStudent.builder().testId(testId).studentId(studentId).points(10).build()),
                PageRequest.of(pageNumber, pageSize),
                totalTests));
    when(testRepository.findAllById(List.of(testId))).thenReturn(List.of(testEntity));

    Page<TestEntity> testEntities = testService.getTestsForStudent(studentId, pageNumber, pageSize);

    assertNotNull(testEntities);
    assertEquals(totalTests, testEntities.getTotalElements());
    assertEquals(testId, testEntities.getContent().getFirst().getId());
    assertEquals("tutor 1", testEntities.getContent().getFirst().getTutorId());
    assertEquals(IN_PROGRESS, testEntities.getContent().getFirst().getStatus());
    assertEquals(pageNumber, testEntities.getPageable().getPageNumber());
    assertEquals(pageSize, testEntities.getPageable().getPageSize());
  }

  @Test
  void getTestMcqDetailTest() {
    Long testId = 1L;
    String tutorId = "tutor 1";
    TestEntity testEntity = new TestEntity();
    testEntity.setId(testId);
    testEntity.setStatus(IN_PROGRESS);
    testEntity.setTutorId(tutorId);
    when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));

    //    TestMcqDetailResponse testMcqDetailResponse = testService.getTestMcqDetail(testId,
    // userContext);
    //
    //    assertNotNull(testMcqDetailResponse);
    //    assertEquals(testId, testMcqDetailResponse.getId());
    //    assertEquals(tutorId, testMcqDetailResponse.getTutorId());
    //    assertEquals(IN_PROGRESS, testMcqDetailResponse.getStatus());
  }

  @Test
  void getTestMcqDetailTestTestNotFound() {
    Long testId = 1L;
    TestEntity testEntity = new TestEntity();
    testEntity.setId(testId);
    testEntity.setStatus(IN_PROGRESS);
    testEntity.setTutorId("tutor 1");
    when(testRepository.findById(testId)).thenReturn(Optional.of(testEntity));
  }
}
