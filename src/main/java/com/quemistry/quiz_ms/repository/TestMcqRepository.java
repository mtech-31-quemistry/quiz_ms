package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.TestMcqs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestMcqRepository extends JpaRepository<TestMcqs, TestMcqs.TestMcqsId> {}
