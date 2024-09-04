package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
  boolean existsByTutorIdAndStatus(String tutorId, TestStatus status);
}
