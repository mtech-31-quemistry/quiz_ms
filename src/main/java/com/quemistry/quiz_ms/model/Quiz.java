package com.quemistry.quiz_ms.model;

import static com.quemistry.quiz_ms.model.QuizStatus.*;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private QuizStatus status;

  private String studentId;

  private Date createdOn;

  private Date updatedOn;

  public static Quiz create(String studentId) {
    Date now = new Date();
    return Quiz.builder()
        .status(IN_PROGRESS)
        .studentId(studentId)
        .createdOn(now)
        .updatedOn(now)
        .build();
  }

  public void complete() {
    this.status = COMPLETED;
    this.updatedOn = new Date();
  }

  public void abandon() {
    this.status = ABANDONED;
    this.updatedOn = new Date();
  }
}
