package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestStudent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestStudentRepository
    extends JpaRepository<TestStudent, TestStudent.TestStudentId> {
  Page<TestStudent> findPageByStudentIdOrderByTestIdDesc(String studentId, PageRequest pageable);

  List<TestStudent> findByTestId(Long testId);

  Optional<TestStudent> findOneByTestIdAndStudentId(Long testId, String studentId);
}
