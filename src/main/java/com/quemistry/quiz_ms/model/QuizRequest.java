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
    private Optional<List<Long>> topics;
    private Optional<List<Long>> skills;

    private Integer pageNumber;

    private Integer pageSize;
}
