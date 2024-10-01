package com.quemistry.quiz_ms.client.model;

import static com.quemistry.quiz_ms.client.model.RetrieveMCQRequest.QuestionStatus.PUBLISHED;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetrieveMCQRequest {
  private List<Long> topics;
  private List<Long> skills;
  private List<Long> excludeIds;

  private Integer pageNumber;
  private Integer pageSize;

  @Builder.Default private List<QuestionStatus> statuses = List.of(PUBLISHED);

  enum QuestionStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
  }
}
