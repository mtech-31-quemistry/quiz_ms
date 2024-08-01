package com.quemistry.quiz_ms.model;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attempt", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(Attempt.AttemptId.class)
public class Attempt {
  @Id
  @Column(name = "quiz_id")
  private Long quizId;

  @Id private Long mcqId;

  @ManyToOne(fetch = LAZY)
  @JoinColumn(name = "quiz_id", insertable = false, updatable = false)
  private Quiz quiz;

  private Integer optionNo;

  private Date attemptTime;

  public static Attempt create(Quiz quiz, Long mcqId) {
    return Attempt.builder()
        .quiz(quiz)
        .quizId(quiz.getId())
        .mcqId(mcqId)
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
  public static class AttemptId implements Serializable {
    private Long quizId;
    private Long mcqId;
  }
}
