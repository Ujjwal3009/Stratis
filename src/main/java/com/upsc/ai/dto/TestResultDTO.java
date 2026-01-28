package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {
    private Long attemptId;
    private Long testId;
    private BigDecimal score;
    private Integer totalQuestions;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<QuestionResultDTO> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultDTO {
        private Long questionId;
        private Long selectedOptionId;
        private Long correctOptionId;
        private Boolean isCorrect;
        private String explanation;
    }
}
