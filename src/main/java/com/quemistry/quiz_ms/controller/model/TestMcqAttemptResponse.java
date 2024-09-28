package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.SearchStudentResponse.StudentResponse;
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
public class TestMcqAttemptResponse extends MCQDto {
  private Long id;
  private TestStatus testStatus;
  private String title;
  private String tutorId;
  private Date createdOn;
  private Date updatedOn;

  private int index;

  private List<McqStudentAttemptResponse> attempts;

  public static TestMcqAttemptResponse from(
      TestEntity test,
      int index,
      MCQDto mcq,
      List<TestAttempt> attempts,
      List<StudentResponse> studentResponses) {
    return TestMcqAttemptResponse.builder()
        .id(test.getId())
        .testStatus(test.getStatus())
        .title(test.getTitle())
        .tutorId(test.getTutorId())
        .createdOn(test.getCreatedOn())
        .createdBy(mcq.getCreatedBy())
        .updatedOn(test.getUpdatedOn())
        .index(index)
        .stem(mcq.getStem())
        .options(mcq.getOptions())
        .topics(mcq.getTopics())
        .skills(mcq.getSkills())
        .status(mcq.getStatus())
        .publishedOn(mcq.getPublishedOn())
        .publishedBy(mcq.getPublishedBy())
        .closedOn(mcq.getClosedOn())
        .closedBy(mcq.getClosedBy())
        .attempts(
            attempts.stream()
                .map(
                    (TestAttempt attempt) ->
                        McqStudentAttemptResponse.from(
                            attempt,
                            studentResponses.stream()
                                .filter(
                                    studentResponse ->
                                        studentResponse
                                            .getAccountId()
                                            .equals(attempt.getStudentId()))
                                .findFirst()
                                .orElse(StudentResponse.defaultStudent(attempt.getStudentId()))))
                .toList())
        .build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class McqStudentAttemptResponse {
    private String studentId;
    private String studentName;
    private Integer optionNo;
    private Date attemptTime;

    public static McqStudentAttemptResponse from(
        TestAttempt attempt, StudentResponse studentResponse) {
      return McqStudentAttemptResponse.builder()
          .studentId(attempt.getStudentId())
          .studentName(studentResponse.getFullName())
          .optionNo(attempt.getOptionNo())
          .attemptTime(attempt.getAttemptTime())
          .build();
    }
  }
}
