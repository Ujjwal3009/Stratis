package com.upsc.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_difficulty", nullable = false)
    private Question.DifficultyLevel targetDifficulty;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 30;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToMany
    @JoinTable(name = "test_questions_mapping", joinColumns = @JoinColumn(name = "test_id"), inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions;

    @Enumerated(EnumType.STRING)
    @Column(name = "test_type", nullable = false)
    private TestType testType = TestType.PRACTICE;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (totalMarks == null) {
            totalMarks = questions != null ? questions.size() : 0;
        }
    }

    public enum TestType {
        MOCK, PRACTICE, PREVIOUS_YEAR, AI_GENERATED
    }
}
