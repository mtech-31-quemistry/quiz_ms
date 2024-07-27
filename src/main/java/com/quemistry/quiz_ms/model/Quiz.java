package com.quemistry.quiz_ms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
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

    @Enumerated(EnumType.STRING)
    private QuizStatus status;

    private String studentId;

    private Date createdOn;

    private Date updatedOn;

    @ElementCollection
    @CollectionTable(name = "quiz_mcq", schema = "qms_quiz", joinColumns = @JoinColumn(name = "quiz_id"))
    @Column(name = "mcq_id")
    private List<Long> mcqIds;

    static public Quiz create(String studentId) {
        Date now = new Date();
        return Quiz.builder()
                .status(QuizStatus.IN_PROGRESS)
                .studentId(studentId)
                .mcqIds(new ArrayList<>())
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public void addMcq(List<Long> mcqIds) {
        this.mcqIds.addAll(mcqIds);
    }

    public void complete() {
        this.status = QuizStatus.COMPLETED;
        this.updatedOn = new Date();
    }

    public void abandon() {
        this.status = QuizStatus.ABANDONED;
        this.updatedOn = new Date();
    }
}
