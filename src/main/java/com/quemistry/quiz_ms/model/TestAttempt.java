package com.quemistry.quiz_ms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test_attempt", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TestAttempt.TestAttemptId.class)
public class TestAttempt {
  @Id private Long testId;

  @Id private Long mcqId;

  @Id private String studentId;

  private Integer optionNo;

  private Date attemptTime;

  public static TestAttempt create(Long testId, Long mcqId, String studentId) {
    return TestAttempt.builder()
        .testId(testId)
        .mcqId(mcqId)
        .studentId(studentId)
        .optionNo(null)
        .attemptTime(null)
        .build();
  }

  public void updateAttempt(Integer optionNo) {
    this.optionNo = optionNo;
    this.attemptTime = new Date();
  }

  @Data
  @NoArgsConstructor
  public static class TestAttemptId implements Serializable {
    private Long testId;
    private Long mcqId;
    private String studentId;
  }
}
