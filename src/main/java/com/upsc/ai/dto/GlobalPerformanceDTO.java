package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPerformanceDTO {
    private Map<String, Double> subjectAccuracy;
    private Map<String, Double> topicAccuracy;
    private List<StrengthWeaknessDTO> strengths;
    private List<StrengthWeaknessDTO> weaknesses;
    private BehaviouralTrendsDTO behaviouralTrends;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrengthWeaknessDTO {
        private String name; // Subject or Topic name
        private Double accuracy;
        private String type; // "SUBJECT" or "TOPIC"
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BehaviouralTrendsDTO {
        private Double avgFirstInstinctAccuracy;
        private Double avgEliminationEfficiency;
        private Double totalNegativeMarks;
        private Integer totalImpulsiveErrors;
        private Integer totalOverthinkingErrors;
        private Double avgConfidenceIndex;
    }
}
