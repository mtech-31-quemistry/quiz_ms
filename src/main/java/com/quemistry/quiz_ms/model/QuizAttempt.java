package com.quemistry.quiz_ms.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_attempt", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(QuizAttempt.QuizAttemptId.class)
public class QuizAttempt {
  @Id
  @Column(name = "quiz_id")
  private Long quizId;

  @Id private Long mcqId;

  private Integer optionNo;

  private Date attemptTime;

  private Boolean isCorrect;

  public static QuizAttempt create(Long quizId, Long mcqId) {
    return QuizAttempt.builder()
        .quizId(quizId)
        .mcqId(mcqId)
        .optionNo(null)
        .attemptTime(null)
        .build();
  }

  public void updateAttempt(Integer optionNo, int correctOption) {
    this.optionNo = optionNo;
    this.isCorrect = optionNo == correctOption;
    this.attemptTime = new Date();
  }

  @Data
  @NoArgsConstructor
  public static class QuizAttemptId implements Serializable {
    private Long quizId;
    private Long mcqId;
  }
}
