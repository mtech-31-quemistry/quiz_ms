package com.quemistry.quiz_ms.client.model;

import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchStudentRequest {
  private String email;
}
