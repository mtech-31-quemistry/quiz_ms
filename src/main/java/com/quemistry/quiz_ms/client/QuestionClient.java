package com.quemistry.quiz_ms.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.quemistry.quiz_ms.client.model.RetrieveMCQByIdsRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "question-client", url = "${service.question.url}/v1/questions")
public interface QuestionClient {
  @RequestMapping(value = "/retrieve", method = POST, produces = APPLICATION_JSON_VALUE)
  RetrieveMCQResponse retrieveMCQs(
      @RequestBody RetrieveMCQRequest retrieveMCQRequest,
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-role") String userRole);

  @RequestMapping(value = "/retrieve-by-ids", method = POST, produces = APPLICATION_JSON_VALUE)
  RetrieveMCQResponse retrieveMCQsByIds(
      @RequestBody RetrieveMCQByIdsRequest retrieveMCQRequest,
      @RequestHeader("x-user-id") String studentId,
      @RequestHeader("x-user-email") String studentEmail,
      @RequestHeader("x-user-role") String userRole);
}
