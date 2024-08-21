package com.quemistry.quiz_ms.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserContext {
  private String userId;
  private String userEmail;
  private String userRole;
}
