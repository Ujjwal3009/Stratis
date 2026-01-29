package com.upsc.ai.service;

import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    public List<QuestionDTO> listQuestions(Long subjectId, String difficulty) {
        // Simple implementation for now, can be expanded with Specification
        return questionRepository.findAll().stream()
                .filter(q -> subjectId == null || (q.getSubject() != null && q.getSubject().getId().equals(subjectId)))
                .filter(q -> difficulty == null || q.getDifficultyLevel().name().equalsIgnoreCase(difficulty))
                .map(QuestionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public QuestionDTO getQuestion(Long id) {
        return questionRepository.findById(id)
                .map(QuestionDTO::fromEntity)
                .orElseThrow(() -> new BusinessException("Question not found: " + id));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new BusinessException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
    }

    @Transactional
    public com.upsc.ai.entity.Question createQuestionFromAi(com.upsc.ai.dto.ParsedQuestion pq,
            com.upsc.ai.entity.Subject subject, com.upsc.ai.entity.Topic topic, com.upsc.ai.entity.User user) {
        String hash = (subject.getName() + (topic != null ? topic.getName() : "") + pq.getQuestionText())
                .toLowerCase().replaceAll("\\s+", " ").trim();

        return questionRepository.findByNormalizedHash(hash).orElseGet(() -> {
            com.upsc.ai.entity.Question q = new com.upsc.ai.entity.Question();
            q.setQuestionText(pq.getQuestionText());
            q.setQuestionType(com.upsc.ai.entity.Question.QuestionType.valueOf(pq.getQuestionType()));
            q.setDifficultyLevel(com.upsc.ai.entity.Question.DifficultyLevel.valueOf(pq.getDifficultyLevel()));
            q.setExplanation(pq.getExplanation());
            q.setSubject(subject);
            q.setTopic(topic);
            q.setCreatedBy(user);
            q.setIsVerified(false);
            q.setCreatedSource("AI");
            q.setNormalizedHash(hash);
            q.setIsActive(true);

            // Save parent first
            final com.upsc.ai.entity.Question savedQ = questionRepository.save(q);

            if (pq.getOptions() != null) {
                List<com.upsc.ai.entity.QuestionOption> options = pq.getOptions().stream().map(po -> {
                    com.upsc.ai.entity.QuestionOption o = new com.upsc.ai.entity.QuestionOption();
                    o.setOptionText(po.getText());
                    o.setIsCorrect(Boolean.TRUE.equals(po.getIsCorrect()));
                    o.setOptionOrder(po.getOrder());
                    o.setQuestion(savedQ);
                    return o;
                }).collect(Collectors.toList());
                savedQ.setOptions(options);
                return questionRepository.save(savedQ);
            }
            return savedQ;
        });
    }

    // Additional methods like update can be added here
}
