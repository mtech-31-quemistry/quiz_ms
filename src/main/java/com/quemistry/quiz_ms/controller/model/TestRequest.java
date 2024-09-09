package com.quemistry.quiz_ms.controller.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
  @NotEmpty private List<McqIndex> mcqs;
  @NotEmpty private List<String> studentIds;
  @NotBlank private String title;
}
