package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.QuizAttempt;
import com.quemistry.quiz_ms.model.QuizAttempt.QuizAttemptId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, QuizAttemptId> {
  Optional<QuizAttempt> findByQuizIdAndMcqId(Long quizId, Long mcqId);

  boolean existsByQuizIdAndOptionNoIsNull(Long quizId);
}
