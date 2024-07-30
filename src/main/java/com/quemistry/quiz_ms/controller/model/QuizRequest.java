package com.quemistry.quiz_ms.controller.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
  private List<Long> topics;
  private List<Long> skills;
  private Integer pageSize;
  private Long totalSize;
}
