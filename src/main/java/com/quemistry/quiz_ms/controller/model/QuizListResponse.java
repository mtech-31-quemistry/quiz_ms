package com.quemistry.quiz_ms.controller.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class QuizListResponse {

  private List<SimpleQuizResponse> quizzes;
}
