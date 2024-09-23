package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestStudent;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface TestStudentRepository
    extends JpaRepository<TestStudent, TestStudent.TestStudentId> {
  List<TestStudent> findByTestId(Long testId);

  Optional<TestStudent> findOneByTestIdAndStudentId(Long testId, String studentId);

  List<TestStudent> findByStudentId(String studentId);

  @Modifying
  @Transactional
  void deleteByTestId(Long testId);
}
