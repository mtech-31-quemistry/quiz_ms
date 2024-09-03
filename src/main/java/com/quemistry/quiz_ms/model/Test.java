package com.quemistry.quiz_ms.model;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.*;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "test", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private TestStatus status;

  private String tutorId;

  private Date createdOn;

  private Date updatedOn;

  public static Test create(String tutorId) {
    Date now = new Date();
    return Test.builder()
        .status(TestStatus.IN_PROGRESS)
        .tutorId(tutorId)
        .createdOn(now)
        .updatedOn(now)
        .build();
  }

  public void complete() {
    this.status = TestStatus.COMPLETED;
    this.updatedOn = new Date();
  }
}
