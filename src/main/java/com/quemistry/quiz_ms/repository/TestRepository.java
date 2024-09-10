package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestEntity;
import com.quemistry.quiz_ms.model.TestStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<TestEntity, Long> {
  boolean existsByTutorIdAndStatus(String tutorId, TestStatus status);

  Page<TestEntity> findPageByTutorIdOrderByIdDesc(String tutorId, Pageable page);

  Page<TestEntity> findPageByTutorIdAndTitleContainingOrderByIdDesc(
      String tutorId, String search, Pageable page);

  Page<TestEntity> findPageByIdInAndStatusIsNot(
      List<Long> testIds, TestStatus status, Pageable page);

  Page<TestEntity> findPageByIdInAndStatusIsNotAndTitleContaining(
      List<Long> testIds, TestStatus status, String search, Pageable page);
}
