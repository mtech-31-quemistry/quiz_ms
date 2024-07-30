package com.quemistry.quiz_ms.model;

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

  @ManyToOne(fetch = FetchType.LAZY)
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

  @Data
  @NoArgsConstructor
  public static class AttemptId implements Serializable {
    private Long quizId;
    private Long mcqId;
  }
}
