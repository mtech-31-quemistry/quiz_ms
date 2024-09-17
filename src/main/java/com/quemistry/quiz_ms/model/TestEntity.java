package com.quemistry.quiz_ms.model;

import static com.quemistry.quiz_ms.model.TestStatus.*;
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
public class TestEntity {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private TestStatus status;

  private String tutorId;

  private String title;

  private Date createdOn;

  private String createdBy;

  private Date startedOn;

  private String startedBy;

  private Date completedOn;

  private String completedBy;

  private Date updatedOn;

  public static TestEntity create(String tutorId, String title) {
    Date now = new Date();
    return TestEntity.builder()
        .status(DRAFT)
        .tutorId(tutorId)
        .title(title)
        .createdOn(now)
        .createdBy(tutorId)
        .updatedOn(now)
        .build();
  }

  public void start(String tutorId) {
    this.status = IN_PROGRESS;
    this.startedBy = tutorId;
    this.startedOn = new Date();
    this.updatedOn = new Date();
  }

  public void complete(String tutorId) {
    this.status = COMPLETED;
    this.completedBy = tutorId;
    this.completedOn = new Date();
    this.updatedOn = new Date();
  }
}
