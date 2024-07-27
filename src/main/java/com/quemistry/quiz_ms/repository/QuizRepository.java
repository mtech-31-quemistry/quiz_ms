package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
  Optional<Quiz> findByIdAndStudentId(Long id, String studentId);

  Optional<Quiz> findByStudentIdAndStatus(String studentId, QuizStatus status);
}
