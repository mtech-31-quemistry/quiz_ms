package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Quiz;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
  Optional<Quiz> findByIdAndStudentId(Long id, String studentId);
  //    List<Long> findMCQIdsByQuizId(Long quizId);
}
