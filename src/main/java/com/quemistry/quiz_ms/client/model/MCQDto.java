package com.quemistry.quiz_ms.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MCQDto {

    private Long id;
    private String stem;
    private List<OptionDto> options;
    private List<TopicDto> topics;
    private List<SkillDto> skills;
    private String status;
    private Date publishedOn;
    private String publishedBy;
    private Date closedOn;
    private String closedBy;
    private Date createdOn;
    private String createdBy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private Integer no;
        private String text;
        private String explanation;
        private Boolean isAnswer;
    }

}
