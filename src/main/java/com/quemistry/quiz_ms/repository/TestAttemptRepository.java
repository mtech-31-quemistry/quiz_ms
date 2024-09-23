package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestAttempt;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface TestAttemptRepository
    extends JpaRepository<TestAttempt, TestAttempt.TestAttemptId> {
  List<TestAttempt> findByTestId(Long testId);

  List<TestAttempt> findByTestIdAndStudentId(Long testId, String studentId);

  List<TestAttempt> findByTestIdAndMcqId(Long testId, Long mcqId);

  Optional<TestAttempt> findOneByTestIdAndMcqIdAndStudentId(Long testId, Long mcqId, String userId);

  @Modifying
  @Transactional
  void deleteByTestId(Long testId);
}
