package com.quemistry.quiz_ms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    private List<Long> topics;
    private List<Long> skills;
    private String studentId;

    private Integer pageNumber;

    private Integer pageSize;
}
