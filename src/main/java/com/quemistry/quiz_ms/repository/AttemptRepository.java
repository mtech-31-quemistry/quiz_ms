package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Attempt;
import com.quemistry.quiz_ms.model.Attempt.AttemptId;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, AttemptId> {
  Optional<Attempt> findByQuizIdAndMcqId(Long quizId, Long mcqId);

  boolean existsByQuizIdAndOptionNoIsNull(Long quizId);
}
