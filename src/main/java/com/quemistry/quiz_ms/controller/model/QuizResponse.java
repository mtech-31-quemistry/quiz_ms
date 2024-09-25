package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.model.QuizStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
  private Long id;
  private Page<MCQResponse> mcqs;
  private QuizStatus status;
  private Integer points;
}
