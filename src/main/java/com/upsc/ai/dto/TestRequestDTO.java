package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRequestDTO {
    private String title;
    private String description;
    private Long subjectId;
    private Long topicId;
    private String difficulty; // EASY, MEDIUM, HARD
    private Integer count;
    private Integer durationMinutes;
}
