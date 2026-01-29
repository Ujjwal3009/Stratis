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

    public static TestResponseDTO fromEntity(com.upsc.ai.entity.Test test) {
        TestResponseDTO dto = new TestResponseDTO();
        dto.setId(test.getId());
        dto.setTitle(test.getTitle());
        dto.setDescription(test.getDescription());
        dto.setSubject(test.getSubject() != null ? test.getSubject().getName() : null);
        dto.setTopic(test.getTopic() != null ? test.getTopic().getName() : null);
        dto.setDifficulty(test.getTargetDifficulty() != null ? test.getTargetDifficulty().name() : null);
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setTotalMarks(test.getTotalMarks());
        dto.setTestType(test.getTestType() != null ? test.getTestType().name() : null);
        dto.setDurationMinutes(test.getDurationMinutes());
        dto.setCreatedAt(test.getCreatedAt());
        if (test.getQuestions() != null) {
            dto.setQuestions(test.getQuestions().stream()
                    .map(QuestionDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList()));
        }
        return dto;
    }
}
