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

    // Additional methods like update can be added here
}
