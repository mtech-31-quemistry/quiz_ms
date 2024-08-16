package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Quiz;
import com.quemistry.quiz_ms.model.QuizStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
  Optional<Quiz> findOneByIdAndStudentId(Long id, String studentId);

  Optional<Quiz> findOneByStudentIdAndStatus(String studentId, QuizStatus status);

  Page<Quiz> findPageByStudentIdAndStatus(String studentId, QuizStatus status, Pageable pageable);

  boolean existsByIdAndStudentId(Long id, String studentId);
}
