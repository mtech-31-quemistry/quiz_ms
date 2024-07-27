package com.quemistry.quiz_ms.controller.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    private List<Long> topics;
    private List<Long> skills;

    private Integer pageNumber;

    private Integer pageSize;
}
