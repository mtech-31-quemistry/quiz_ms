package com.quemistry.quiz_ms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_student", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TestStudent.TestStudentId.class)
public class TestStudent {
  @Id private Long testId;

  @Id private String studentId;

  private Integer points;

  public static TestStudent create(Long testId, String studentId) {
    return TestStudent.builder().testId(testId).studentId(studentId).points(null).build();
  }

  public void updatePoints(Integer points) {
    this.points = points;
  }

  @Data
  @NoArgsConstructor
  public static class TestStudentId {
    private Long testId;
    private String studentId;
  }
}
