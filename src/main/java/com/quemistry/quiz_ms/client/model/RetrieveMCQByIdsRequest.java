package com.quemistry.quiz_ms.client.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrieveMCQByIdsRequest {
  private List<Long> ids;
  private Integer pageNumber;
  private Integer pageSize;
}
