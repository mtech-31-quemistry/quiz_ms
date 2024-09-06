package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.model.*;
import java.util.Date;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStudentAttemptResponse {
  private Long id;
  private TestStatus status;
  private String tutorId;
  private Date createdOn;
  private Date updatedOn;

  private String studentId;
  private String studentName;
  private Integer points;

  private List<StudentMcqResponse> mcqs;

  public static TestStudentAttemptResponse from(
      TestEntity test,
      List<TestMcqs> testMcqs,
      List<MCQResponse> mcqs,
      List<TestAttempt> attempts,
      TestStudent student) {
    return TestStudentAttemptResponse.builder()
        .id(test.getId())
        .status(test.getStatus())
        .tutorId(test.getTutorId())
        .createdOn(test.getCreatedOn())
        .updatedOn(test.getUpdatedOn())
        .studentId(student.getStudentId())
        // TODO: studentName should be set to the student's name
        .studentName("Student " + student.getStudentId())
        .points(student.getPoints())
        .mcqs(
            testMcqs.stream()
                .map(
                    testMcq -> {
                      MCQResponse mcq =
                          mcqs.stream()
                              .filter(m -> m.getId().equals(testMcq.getMcqId()))
                              .findFirst()
                              .orElse(null);
                      TestAttempt attempt =
                          attempts.stream()
                              .filter(a -> a.getMcqId().equals(testMcq.getMcqId()))
                              .findFirst()
                              .orElse(null);
                      return StudentMcqResponse.from(mcq, testMcq.getIndex(), attempt);
                    })
                .toList())
        .build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @SuperBuilder(toBuilder = true)
  public static class StudentMcqResponse extends MCQResponse {
    private int index;

    public static StudentMcqResponse from(MCQResponse mcq, Integer index, TestAttempt attempt) {
      StudentMcqResponseBuilder<?, ?> builder = StudentMcqResponse.builder().index(index);

      if (mcq != null) {
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
            .createdBy(mcq.getCreatedBy());
      }

      if (attempt != null) {
        builder.attemptOption(attempt.getOptionNo()).attemptOn(attempt.getAttemptTime());
      }

      return builder.build();
    }
  }
}
