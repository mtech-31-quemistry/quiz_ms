package com.quemistry.quiz_ms.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchStudentResponse {
  private Long id;

  private String firstName;

  private String lastName;

  private String email;

  private String accountId;

  private String educationLevel;
}
