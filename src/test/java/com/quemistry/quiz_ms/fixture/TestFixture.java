package com.quemistry.quiz_ms.fixture;

import static com.quemistry.quiz_ms.model.TestStatus.DRAFT;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.client.model.SearchStudentResponse;
import com.quemistry.quiz_ms.client.model.SearchStudentResponse.StudentResponse;
import com.quemistry.quiz_ms.model.*;
import java.util.List;

public class TestFixture {
  public static final Long TEST_ID = 1L;
  public static final Long MCQ_ID = 2L;
  public static final int MCQ_INDEX = 1;
  public static final Integer CURRENT_OPTION_NO = 1;

  public static final String STUDENT_ID = "student Id";
  public static final String STUDENT_FIRST_NAME = "Ce";
  public static final String STUDENT_LAST_NAME = "Shi Ren";
  public static final String STUDENT_NAME = "Ce Shi Ren";
  public static final String TUTOR_ID = "tutor Id";
  public static final String TEST_TITLE = "test title";

  public static final int PAGE_NUMBER = 0;
  public static final int PAGE_SIZE = 10;
  public static final Long TOTAL_RECORDS = 1L;

  public static final int STUDENT_POINTS = 1;

  public static final UserContext studentContext =
      new UserContext(STUDENT_ID, "student@test.com", "student");
  public static final StudentResponse STUDENT_RESPONSE =
      StudentResponse.builder()
          .id(1L)
          .firstName(STUDENT_FIRST_NAME)
          .lastName(STUDENT_LAST_NAME)
          .email("student@test.com")
          .accountId(STUDENT_ID)
          .build();

  public static final SearchStudentResponse SEARCH_STUDENT_RESPONSE =
      SearchStudentResponse.builder()
          .statusCode("200")
          .statusMessage("Success")
          .serviceName("quiz-service")
          .payload(List.of(STUDENT_RESPONSE))
          .build();

  public static final String TUTOR_EMAIL = "tutor@test.com";
  public static final String TUTOR_ROLE = "tutor";
  public static final UserContext tutorContext = new UserContext(TUTOR_ID, TUTOR_EMAIL, TUTOR_ROLE);

  public static final TestEntity testEntity =
      TestEntity.builder().id(TEST_ID).tutorId(TUTOR_ID).status(DRAFT).title(TEST_TITLE).build();

  public static final TestMcqs testMcqs =
      TestMcqs.builder().mcqId(MCQ_ID).testId(TEST_ID).index(MCQ_INDEX).build();

  public static final TestAttempt testAttempt =
      TestAttempt.builder()
          .studentId(STUDENT_ID)
          .testId(TEST_ID)
          .mcqId(MCQ_ID)
          .optionNo(CURRENT_OPTION_NO)
          .build();

  public static final TestStudent testStudent =
      TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(STUDENT_POINTS).build();

  public static RetrieveMCQResponse getRetrieveMCQResponse() {
    RetrieveMCQResponse retrieveMCQResponse = new RetrieveMCQResponse();
    retrieveMCQResponse.setMcqs(List.of(getMcqResponse()));
    retrieveMCQResponse.setTotalPages(1);
    retrieveMCQResponse.setPageSize(PAGE_SIZE);
    retrieveMCQResponse.setPageNumber(PAGE_NUMBER);
    retrieveMCQResponse.setTotalRecords(TOTAL_RECORDS);
    return retrieveMCQResponse;
  }

  private static MCQDto getMcqResponse() {
    MCQDto mcqResponse =
        JMockData.mock(
            MCQDto.class,
            new MockConfig().subConfig("id").longRange(MCQ_ID, MCQ_ID).globalConfig());
    mcqResponse.setOptions(
        List.of(
            getOptionDto(CURRENT_OPTION_NO, TRUE),
            getOptionDto(2, FALSE),
            getOptionDto(3, FALSE),
            getOptionDto(4, FALSE)));

    return mcqResponse;
  }

  private static MCQDto.OptionDto getOptionDto(Integer no, Boolean isAnswer) {
    MCQDto.OptionDto option = JMockData.mock(MCQDto.OptionDto.class);
    option.setNo(no);
    option.setIsAnswer(isAnswer);
    return option;
  }
}
