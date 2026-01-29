package com.upsc.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_test_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTestMetrics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_attempt_id", nullable = false, unique = true)
    private TestAttempt testAttempt;

    @Column(name = "accuracy_percentage")
    private BigDecimal accuracyPercentage;

    @Column(name = "attempt_ratio")
    private BigDecimal attemptRatio;

    @Column(name = "negative_marks")
    private BigDecimal negativeMarks;

    @Column(name = "first_instinct_accuracy")
    private BigDecimal firstInstinctAccuracy;

    @Column(name = "elimination_efficiency")
    private BigDecimal eliminationEfficiency;

    @Column(name = "impulsive_error_count")
    private Integer impulsiveErrorCount;

    @Column(name = "overthinking_error_count")
    private Integer overthinkingErrorCount;

    @Column(name = "guess_probability")
    private BigDecimal guessProbability;

    @Column(name = "cognitive_breakdown", columnDefinition = "TEXT")
    private String cognitiveBreakdownJson;

    @Column(name = "risk_appetite_score")
    private BigDecimal riskAppetiteScore;

    @Column(name = "fatigue_curve", columnDefinition = "TEXT")
    private String fatigueCurveJson;

    @Column(name = "confidence_index")
    private BigDecimal confidenceIndex;

    @Column(name = "consistency_index")
    private BigDecimal consistencyIndex;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
