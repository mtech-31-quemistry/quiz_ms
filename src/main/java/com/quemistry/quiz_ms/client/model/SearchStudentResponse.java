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
  private List<StudentResponse> payload;

  @Builder
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String accountId;

    public static StudentResponse defaultStudent(String studentId) {
      return StudentResponse.builder()
          .accountId(studentId)
          .firstName("Unknown")
          .lastName("Unknown")
          .build();
    }

    public String getFullName() {
      return firstName + " " + lastName;
    }
  }
}
