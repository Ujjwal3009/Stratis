package com.upsc.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_question_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuestionAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_attempt_id")
    private TestAttempt testAttempt;

    @Column(name = "selected_option")
    private String selectedOption;

    @Column(name = "first_selected_option")
    private String firstSelectedOption;

    @Column(name = "option_change_count")
    private Integer optionChangeCount = 0;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "hover_count")
    private Integer hoverCount = 0;

    @Column(name = "eliminated_option_ids", columnDefinition = "TEXT")
    private String eliminatedOptionIdsJson;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
        if (attemptedAt == null)
            attemptedAt = LocalDateTime.now();
    }
}
