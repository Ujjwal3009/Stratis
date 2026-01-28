package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionDTO {
    private Long attemptId;
    private Map<Long, Long> answers; // questionId -> selectedOptionId
}
