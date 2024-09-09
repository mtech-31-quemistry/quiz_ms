package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.model.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestStudentDetailResponse {
  private Long id;
  private TestStatus status;
  private String title;
  private String tutorId;
  private Date createdOn;
  private Date updatedOn;

  private int totalMcqCount;

  private List<TestStudentResponse> students;

  public static TestStudentDetailResponse from(
      TestEntity test,
      List<TestMcqs> testMcqs,
      List<MCQResponse> mcqs,
      List<TestAttempt> attempts,
      List<TestStudent> students) {
    return TestStudentDetailResponse.builder()
        .id(test.getId())
        .status(test.getStatus())
        .title(test.getTitle())
        .tutorId(test.getTutorId())
        .createdOn(test.getCreatedOn())
        .updatedOn(test.getUpdatedOn())
        .totalMcqCount(testMcqs.size())
        .students(
            students.stream()
                .map(
                    student ->
                        TestStudentResponse.from(
                            student,
                            attempts.stream()
                                .filter(
                                    attempt ->
                                        attempt.getStudentId().equals(student.getStudentId()))
                                .collect(Collectors.toList()),
                            mcqs))
                .collect(Collectors.toList()))
        .build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class TestStudentResponse {
    private String studentId;
    private String studentName;
    private Integer points;

    private int attemptMcqCount;
    private int correctMcqCount;

    public static TestStudentResponse from(
        TestStudent student, List<TestAttempt> attempts, List<MCQResponse> mcqs) {
      return TestStudentResponse.builder()
          .studentId(student.getStudentId())
          // TODO: studentName should be set to the student's name
          .studentName("Student " + student.getStudentId())
          .points(student.getPoints())
          .attemptMcqCount(attempts.size())
          .correctMcqCount(
              (int)
                  mcqs.stream()
                      .filter(
                          mcq -> {
                            MCQDto.OptionDto correctOptions =
                                mcq.getOptions().stream()
                                    .filter(MCQDto.OptionDto::getIsAnswer)
                                    .findFirst()
                                    .orElse(null);

                            return correctOptions != null
                                && attempts.stream()
                                    .anyMatch(
                                        attempt ->
                                            attempt.getMcqId().equals(mcq.getId())
                                                && correctOptions
                                                    .getNo()
                                                    .equals(attempt.getOptionNo()));
                          })
                      .count())
          .build();
    }
  }
}
