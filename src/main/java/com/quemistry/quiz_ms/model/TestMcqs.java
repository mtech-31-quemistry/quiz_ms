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
@Table(name = "test_mcq", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TestMcqs.TestMcqsId.class)
public class TestMcqs {
  @Id private Long testId;

  @Id private Long mcqId;

  private Integer index;

  public static TestMcqs create(Long testId, Long mcqId, Integer index) {
    return TestMcqs.builder().testId(testId).mcqId(mcqId).index(index).build();
  }

  @Data
  @NoArgsConstructor
  public static class TestMcqsId {
    private Long testId;
    private Long mcqId;
  }
}
