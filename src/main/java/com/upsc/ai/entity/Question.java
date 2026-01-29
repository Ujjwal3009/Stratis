package com.upsc.ai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_pdf_id")
    private PdfDocument sourcePdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    // New Question Bank Fields
    @Column(name = "cognitive_level")
    private String cognitiveLevel;

    // stored as JSON (String) in DB
    @Column(name = "options", columnDefinition = "TEXT")
    private String optionsJson;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "explanation_json", columnDefinition = "TEXT")
    private String explanationJson;

    @Column(name = "exam_year")
    private Integer year;

    @Column(name = "paper")
    private String paper;

    @Column(name = "passage", columnDefinition = "TEXT")
    private String passage;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "normalized_hash")
    private String normalizedHash;

    @Column(name = "created_source")
    private String createdSource;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private List<QuestionOption> options;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum QuestionType {
        MCQ, SUBJECTIVE, TRUE_FALSE
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }

    public void generateHash() {
        if (this.questionText == null)
            return;
        try {
            String combined = this.questionText.trim().toLowerCase().replaceAll("\\s+", " ");
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            this.normalizedHash = hexString.toString();
        } catch (Exception e) {
            // fallback
            this.normalizedHash = String.valueOf(this.questionText.hashCode());
        }
    }
}
