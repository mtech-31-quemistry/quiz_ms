package com.quemistry.quiz_ms.client.model;

import java.util.List;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchStudentRequest {
  private List<String> accountIds;
  private List<String> emails;

  public static SearchStudentRequest from(List<String> accountIds) {
    return SearchStudentRequest.builder().accountIds(accountIds).emails(List.of()).build();
  }
}
