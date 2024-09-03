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
public class TestRequest {
  private List<McqIndex> mcqs;
  private List<String> studentIds;
}
