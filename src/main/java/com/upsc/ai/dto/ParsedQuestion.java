package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedQuestion {
    private String questionText;
    private String questionType; // MCQ, SUBJECTIVE, TRUE_FALSE
    private String difficultyLevel; // EASY, MEDIUM, HARD
    private String subject;
    private String topic;
    private String explanation;
    private List<ParsedOption> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParsedOption {
        private String text;
        private Boolean isCorrect;
        private Integer order;
    }
}
