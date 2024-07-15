package com.quemistry.quiz_ms.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDto {

    private Integer id;
    private String name;
    private Integer topicId;
}
