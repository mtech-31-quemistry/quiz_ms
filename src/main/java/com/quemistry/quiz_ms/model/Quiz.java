package com.quemistry.quiz_ms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz", schema = "qms_quiz")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private QuizStatus status;

    private String studentId;

    @ElementCollection
    @CollectionTable(name = "quiz_mcq", schema = "qms_quiz", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "mcq_id")
    private List<Long> mcqIds;

    static public Quiz create(String studentId) {
        return Quiz.builder()
                .status(QuizStatus.IN_PROGRESS)
                .studentId(studentId)
                .mcqIds(new ArrayList<>())
                .build();
    }

    public void addMcq(List<Long> mcqIds) {
        this.mcqIds.addAll(mcqIds);

    }

    public void complete() {
        this.status = QuizStatus.COMPLETED;
    }

    public void abandon() {
        this.status = QuizStatus.ABANDONED;
    }
}
