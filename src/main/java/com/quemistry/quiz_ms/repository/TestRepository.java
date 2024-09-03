package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Test;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends JpaRepository<Test, Long> {}
