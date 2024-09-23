package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestMcqs;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface TestMcqRepository extends JpaRepository<TestMcqs, TestMcqs.TestMcqsId> {
  List<TestMcqs> findByTestId(Long testId);

  Optional<TestMcqs> findOneByTestIdAndMcqId(Long testId, Long mcqId);

  @Modifying
  @Transactional
  void deleteByTestId(Long testId);
}
