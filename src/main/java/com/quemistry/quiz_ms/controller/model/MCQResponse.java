package com.quemistry.quiz_ms.controller.model;

import com.quemistry.quiz_ms.client.model.MCQDto;
import com.quemistry.quiz_ms.client.model.SkillDto;
import com.quemistry.quiz_ms.client.model.TopicDto;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCQResponse {

  private Long id;
  private String stem;
  private List<MCQDto.OptionDto> options;
  private List<TopicDto> topics;
  private List<SkillDto> skills;
  private String status;
  private Date publishedOn;
  private String publishedBy;
  private Date closedOn;
  private String closedBy;
  private Date createdOn;
  private String createdBy;

  private Integer attemptOption;
  private Date attemptOn;
}
