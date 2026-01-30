package com.upsc.ai.service;

import com.upsc.ai.dto.ParsedQuestion;
import com.upsc.ai.entity.Question;
import com.upsc.ai.entity.Subject;
import com.upsc.ai.entity.Topic;
import com.upsc.ai.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.entity.User;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryRefillService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private GeminiAiService geminiAiService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Async
    public void triggerRefill(Subject subject, Topic topic, String difficultyLevel) {
        try {
            List<Question.DifficultyLevel> levels = getMatchingLevels(difficultyLevel);

            // 1. Check current inventory count
            long availableCount = questionRepository.countAvailableQuestions(
                    subject.getId(),
                    topic != null ? topic.getId() : null,
                    levels);

            int targetCount = 30; // Threshold
            if (availableCount < targetCount) {
                int needed = targetCount - (int) availableCount;
                int batchSize = Math.min(needed, 20); // Cap batch size

                System.out.println("Inventory Refill: Generating " + batchSize + " questions for " + subject.getName());

                String topicName = topic != null ? topic.getName() : "General " + subject.getName();

                User systemUser = userRepository.findAll().stream().findFirst().orElse(null);

                // 2. Generate new questions via AI
                List<ParsedQuestion> generated = geminiAiService.generateQuestions(
                        subject.getName(),
                        topicName,
                        difficultyLevel,
                        batchSize,
                        null,
                        systemUser);

                // 3. Save with created_source = "AI" and is_verified = false
                for (ParsedQuestion pq : generated) {
                    try {
                        questionService.createQuestionFromAi(pq, subject, topic, null);
                    } catch (Exception e) {
                        System.err.println("Inventory Refill: Error saving question: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Inventory Refill: Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Question.DifficultyLevel> getMatchingLevels(String requested) {
        List<Question.DifficultyLevel> levels = new ArrayList<>();
        Question.DifficultyLevel requestedLevel = Question.DifficultyLevel.valueOf(requested);
        levels.add(requestedLevel);
        if (requestedLevel == Question.DifficultyLevel.EASY) {
            levels.add(Question.DifficultyLevel.MEDIUM);
            levels.add(Question.DifficultyLevel.HARD);
        } else if (requestedLevel == Question.DifficultyLevel.MEDIUM) {
            levels.add(Question.DifficultyLevel.HARD);
        }
        return levels;
    }
}
