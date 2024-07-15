package com.quemistry.quiz_ms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponse {
    private List<MCQDto> mcqs;

    private Integer pageNumber;

    private Integer pageSize;

}
