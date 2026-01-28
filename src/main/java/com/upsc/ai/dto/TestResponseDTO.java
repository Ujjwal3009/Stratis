package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private String topic;
    private String difficulty;
    private Integer totalQuestions;
    private Integer totalMarks;
    private String testType;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;
}
