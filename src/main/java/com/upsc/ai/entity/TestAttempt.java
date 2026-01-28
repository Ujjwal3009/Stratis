package com.upsc.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum AttemptStatus {
        IN_PROGRESS, COMPLETED, ABANDONED
    }
}
