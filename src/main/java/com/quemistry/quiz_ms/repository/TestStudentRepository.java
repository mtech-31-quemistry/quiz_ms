package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestStudentRepository
    extends JpaRepository<TestStudent, TestStudent.TestStudentId> {}
