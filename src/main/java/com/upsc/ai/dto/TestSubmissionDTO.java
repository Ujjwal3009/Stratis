package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionDTO {
    private Long attemptId;
    private List<AnswerDTO> answers;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDTO {
        private Long questionId;
        private Long selectedOptionId;
        private Integer timeSpentSeconds;
        private Integer selectionChangeCount = 0;
        private Integer hoverCount = 0;
        private Long firstSelectedOptionId;
        private List<Long> eliminatedOptionIds;
    }
}
