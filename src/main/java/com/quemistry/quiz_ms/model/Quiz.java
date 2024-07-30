package com.quemistry.quiz_ms.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private QuizStatus status;

  private String studentId;

  private Date createdOn;

  private Date updatedOn;

  @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attempt> attempts;

  public static Quiz create(String studentId) {
    Date now = new Date();
    return Quiz.builder()
        .status(QuizStatus.IN_PROGRESS)
        .studentId(studentId)
        .attempts(new ArrayList<>())
        .createdOn(now)
        .updatedOn(now)
        .build();
  }

  public void addAttempts(List<Long> mcqIds) {
    mcqIds.forEach(mcqId -> this.attempts.add(Attempt.create(this, mcqId)));
  }

  public void complete() {
    this.status = QuizStatus.COMPLETED;
    this.updatedOn = new Date();
  }

  public void abandon() {
    this.status = QuizStatus.ABANDONED;
    this.updatedOn = new Date();
  }
}
