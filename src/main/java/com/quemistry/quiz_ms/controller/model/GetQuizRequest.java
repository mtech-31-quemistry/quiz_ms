package com.quemistry.quiz_ms.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetQuizRequest {
  private Integer pageNumber;
  private Integer pageSize;
}
