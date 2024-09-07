package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.model.TestAttempt;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestMcqs;
import com.quemistry.quiz_ms.model.TestStatus;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestMcqDetailResponse {
  private Long id;
  private TestStatus status;
  private String tutorId;
  private Date createdOn;
  private Date updatedOn;
  private List<TestMcqResponse> mcqs;

  private int totalStudentsCount;

  public static TestMcqDetailResponse from(
      TestEntity test,
      List<TestMcqs> testMcqs,
      List<MCQResponse> mcqs,
      List<TestAttempt> attempts) {
    return TestMcqDetailResponse.builder()
        .id(test.getId())
        .status(test.getStatus())
        .tutorId(test.getTutorId())
        .createdOn(test.getCreatedOn())
        .updatedOn(test.getUpdatedOn())
        .totalStudentsCount(attempts.size())
        .mcqs(
            testMcqs.stream()
                .map(
                    testMcq ->
                        TestMcqResponse.from(
                            testMcq.getIndex(),
                            mcqs.stream()
                                .filter(
                                    retrieveMCQ -> retrieveMCQ.getId().equals(testMcq.getMcqId()))
                                .findFirst()
                                .orElse(null),
                            attempts.stream()
                                .filter(
                                    attempt ->
                                        attempt.getTestId().equals(test.getId())
                                            && attempt.getMcqId().equals(testMcq.getMcqId()))
                                .toList()))
                .collect(Collectors.toList()))
        .build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @SuperBuilder(toBuilder = true)
  public static class TestMcqResponse extends MCQResponse {
    private int index;
    private int attemptStudentsCount;
    private int correctStudentsCount;

    public static TestMcqResponse from(int index, MCQResponse mcq, List<TestAttempt> attempts) {
      TestMcqResponseBuilder<?, ?> builder = TestMcqResponse.builder().index(index);
      if (mcq != null) {
        int correctStudentsCount = 0;
        MCQDto.OptionDto correctOption =
            mcq.getOptions().stream()
                .filter(MCQDto.OptionDto::getIsAnswer)
                .findFirst()
                .orElse(null);
        if (correctOption != null) {
          correctStudentsCount =
              (int)
                  attempts.stream()
                      .filter(attempt -> correctOption.getNo().equals(attempt.getOptionNo()))
                      .count();
        }
        builder
            .id(mcq.getId())
            .stem(mcq.getStem())
            .options(mcq.getOptions())
            .topics(mcq.getTopics())
            .skills(mcq.getSkills())
            .status(mcq.getStatus())
            .publishedOn(mcq.getPublishedOn())
            .publishedBy(mcq.getPublishedBy())
            .closedOn(mcq.getClosedOn())
            .closedBy(mcq.getClosedBy())
            .createdOn(mcq.getCreatedOn())
            .createdBy(mcq.getCreatedBy())
            .attemptOption(mcq.getAttemptOption())
            .attemptOn(mcq.getAttemptOn())
            .attemptStudentsCount(attempts.size())
            .correctStudentsCount(correctStudentsCount);
      }
      return builder.build();
    }
  }
}
