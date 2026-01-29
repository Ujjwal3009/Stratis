package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAnalysisDTO {
    private Long attemptId;
    private Long testId;
    private BigDecimal overallScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer wrongCount;
    private Integer unansweredCount;
    private Double accuracyPercentage;
    private Long totalTimeSpentSeconds;

    // Topic-wise breakdown
    private List<TopicAnalysisDTO> topicPerformances = new java.util.ArrayList<>();

    // Mistake categorization
    private Map<String, Integer> mistakeTypeCounts = new java.util.HashMap<>(); // e.g., "SILLY_MISTAKE" -> 3

    // AI Insights
    private String aiDiagnosticSummary;
    private String synthesizedStudyNotes;
    private List<StrengthWeaknessDTO> strengthWeaknessPairs = new java.util.ArrayList<>();

    // Behavioural Metrics (Rule-Based Cards)
    private BehaviouralMetricsDTO behaviouralMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviouralMetricsDTO {
        private Double accuracyPercentage;
        private Double attemptRatio;
        private Double negativeMarks;
        private Double firstInstinctAccuracy;
        private Double eliminationEfficiency;
        private Integer impulsiveErrorCount;
        private Integer overthinkingErrorCount;
        private Double guessProbability;
        private Map<String, Object> cognitiveBreakdown;
        private Map<String, Object> fatigueCurve;
        private Double riskAppetiteScore;
        private Double confidenceIndex;
        private Double consistencyIndex;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrengthWeaknessDTO {
        private String point;
        private String strategy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicAnalysisDTO {
        private String topicName;
        private Integer correct;
        private Integer total;
        private Double accuracy;
        private Long avgTimeSpentSeconds;
        private String status; // "MASTERED", "NEED_PRACTICE", "WEAK"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MistakeDetailDTO {
        private Long questionId;
        private String mistakeType;
        private String reason;
    }
}
