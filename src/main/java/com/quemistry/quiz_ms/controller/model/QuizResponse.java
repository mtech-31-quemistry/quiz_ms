package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
  private Long id;

  private List<MCQDto> mcqs;

  private Integer pageNumber;
  private Integer pageSize;
  private Integer totalPages;

  private Long totalRecords;
}
