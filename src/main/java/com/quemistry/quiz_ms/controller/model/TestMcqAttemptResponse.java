package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.model.TestAttempt;
import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestStatus;
import java.util.Date;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class TestMcqAttemptResponse extends MCQResponse {
  private Long id;
  private TestStatus testStatus;
  private String tutorId;
  private Date createdOn;
  private Date updatedOn;

  private int index;

  private List<McqStudentAttemptResponse> attempts;

  public static TestMcqAttemptResponse from(
      TestEntity test, MCQDto mcq, List<TestAttempt> attempts) {
    return TestMcqAttemptResponse.builder()
        .id(test.getId())
        .testStatus(test.getStatus())
        .tutorId(test.getTutorId())
        .createdOn(test.getCreatedOn())
        .updatedOn(test.getUpdatedOn())
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
        .attempts(attempts.stream().map(McqStudentAttemptResponse::from).toList())
        .build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class McqStudentAttemptResponse {
    private String studentId;
    private String studentName;
    private int optionNo;
    private Date attemptTime;

    public static McqStudentAttemptResponse from(TestAttempt attempt) {
      return McqStudentAttemptResponse.builder()
          .studentId(attempt.getStudentId())
          .optionNo(attempt.getOptionNo())
          .attemptTime(attempt.getAttemptTime())
          .build();
    }
  }
}
