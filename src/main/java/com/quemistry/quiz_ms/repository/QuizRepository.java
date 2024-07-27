package com.quemistry.quiz_ms.repository;

import com.quemistry.quiz_ms.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
//    List<Long> findMCQIdsByQuizId(Long quizId);
}
