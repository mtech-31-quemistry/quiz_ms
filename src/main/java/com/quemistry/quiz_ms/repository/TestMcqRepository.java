package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestMcqs;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMcqRepository extends JpaRepository<TestMcqs, TestMcqs.TestMcqsId> {
  List<TestMcqs> findByTestId(Long testId);

  Optional<TestMcqs> findOneByTestIdAndMcqId(Long testId, Long mcqId);
}
