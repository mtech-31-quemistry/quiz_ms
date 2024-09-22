package com.quemistry.quiz_ms.client.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchStudentResponse {
  private String statusCode;
  private String statusMessage;
  private String serviceName;
  private List<ErrorDto> errors;
  private Object payload;
}
