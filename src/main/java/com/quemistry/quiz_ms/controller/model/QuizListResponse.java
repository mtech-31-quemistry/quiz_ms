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
public class QuizListResponse {
  private Integer pageNumber;
  private Integer pageSize;
  private Integer totalPages;
  private Long totalRecords;

  private List<SimpleQuizResponse> quizzes;
}
