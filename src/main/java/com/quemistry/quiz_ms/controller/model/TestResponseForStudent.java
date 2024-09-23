package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResponseForStudent {
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

  private Integer points;

  public static TestResponseForStudent from(TestEntity test, Integer points) {
    return TestResponseForStudent.builder()
        .id(test.getId())
        .status(test.getStatus())
        .tutorId(test.getTutorId())
        .title(test.getTitle())
        .createdOn(test.getCreatedOn())
        .createdBy(test.getCreatedBy())
        .startedOn(test.getStartedOn())
        .startedBy(test.getStartedBy())
        .completedOn(test.getCompletedOn())
        .completedBy(test.getCompletedBy())
        .updatedOn(test.getUpdatedOn())
        .points(points)
        .build();
  }
}
