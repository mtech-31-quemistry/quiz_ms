package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestAttemptRepository
    extends JpaRepository<TestAttempt, TestAttempt.TestAttemptId> {}
