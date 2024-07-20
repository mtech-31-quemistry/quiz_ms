package com.quemistry.quiz_ms.client;

import com.quemistry.quiz_ms.client.model.RetrieveMCQRequest;
import com.quemistry.quiz_ms.client.model.RetrieveMCQResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "question-client", url = "${service.question.url}")
@RequestMapping(value = "/v1/questions")
public interface  QuestionClient {
    @RequestMapping(value = "/retrieve",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    RetrieveMCQResponse retrieveMCQs(RetrieveMCQRequest retrieveMCQRequest);
}
