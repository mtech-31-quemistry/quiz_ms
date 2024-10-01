package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.QuizAttempt;
import com.quemistry.quiz_ms.model.QuizAttempt.QuizAttemptId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, QuizAttemptId> {
  Optional<QuizAttempt> findByQuizIdAndMcqId(Long quizId, Long mcqId);

  List<QuizAttempt> findAllByQuizIdIn(List<Long> quizIds);

  boolean existsByQuizIdAndOptionNoIsNull(Long quizId);

  Page<QuizAttempt> findPageByQuizId(Long id, Pageable page);

  List<QuizAttempt> findAllByQuizId(Long quizId);
}
