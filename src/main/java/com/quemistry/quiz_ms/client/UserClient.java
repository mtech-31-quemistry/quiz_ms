package com.quemistry.quiz_ms.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.quemistry.quiz_ms.client.model.SearchStudentRequest;
import com.quemistry.quiz_ms.client.model.SearchStudentResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "user-client", url = "${service.user.url}/v1")
public interface UserClient {
  @RequestMapping(value = "/v1/student/search", method = POST, produces = APPLICATION_JSON_VALUE)
  List<SearchStudentResponse> searchStudents(
      SearchStudentRequest searchStudentRequest,
      @RequestHeader("x-user-id") @NotBlank String tutorId,
      @RequestHeader("x-user-email") @Email String tutorEmail,
      @RequestHeader("x-user-roles") @NotBlank String userRoles);
}
