package com.upsc.ai.dto;

import com.upsc.ai.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String questionType;
    private String difficultyLevel;
    private String subject;
    private String topic;
    private String explanation;
    private List<QuestionOptionDTO> options;
    private Boolean isVerified;

    public static QuestionDTO fromEntity(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType().name());
        dto.setDifficultyLevel(question.getDifficultyLevel().name());
        dto.setExplanation(question.getExplanation());
        dto.setSubject(question.getSubject() != null ? question.getSubject().getName() : null);
        dto.setTopic(question.getTopic() != null ? question.getTopic().getName() : null);
        dto.setIsVerified(question.getIsVerified());

        if (question.getOptions() != null) {
            dto.setOptions(question.getOptions().stream()
                    .map(QuestionOptionDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionOptionDTO {
        private String text;
        private Boolean isCorrect;
        private Integer order;

        public static QuestionOptionDTO fromEntity(com.upsc.ai.entity.QuestionOption option) {
            return new QuestionOptionDTO(
                    option.getOptionText(),
                    option.getIsCorrect(),
                    option.getOptionOrder());
        }
    }
}
