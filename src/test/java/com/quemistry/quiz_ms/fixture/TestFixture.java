package com.quemistry.quiz_ms.fixture;

import static com.quemistry.quiz_ms.model.TestStatus.IN_PROGRESS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.github.jsonzou.jmockdata.JMockData;
import com.github.jsonzou.jmockdata.MockConfig;
import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import com.quemistry.quiz_ms.model.TestAttempt;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestMcqs;
import com.quemistry.quiz_ms.model.TestStudent;
import java.util.List;

public class TestFixture {
  public static final Long TEST_ID = 1L;
  public static final Long MCQ_ID = 2L;
  public static final int MCQ_INDEX = 1;
  public static final Integer CURRENT_OPTION_NO = 1;

  public static final String STUDENT_ID = "student Id";
  public static final String TUTOR_ID = "tutor Id";

  public static final int PAGE_NUMBER = 0;
  public static final int PAGE_SIZE = 10;
  public static final Long TOTAL_RECORDS = 1L;

  public static final TestEntity testEntity =
      TestEntity.builder().id(TEST_ID).tutorId(TUTOR_ID).status(IN_PROGRESS).build();

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
      TestStudent.builder().studentId(STUDENT_ID).testId(TEST_ID).points(1).build();

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
