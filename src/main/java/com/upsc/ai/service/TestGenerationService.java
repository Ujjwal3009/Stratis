package com.upsc.ai.service;

import com.upsc.ai.dto.ParsedQuestion;
import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.dto.TestRequestDTO;
import com.upsc.ai.dto.TestResponseDTO;
import com.upsc.ai.entity.*;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TestGenerationService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private GeminiAiService geminiAiService;

    @Transactional
    public TestResponseDTO generateTest(TestRequestDTO request, User user) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Subject not found"));

        Topic topic = request.getTopicId() != null ? topicRepository.findById(request.getTopicId()).orElse(null) : null;

        // 1. Fetch matching questions from DB
        // Difficulty filter: Bank questions must be at least as hard as requested
        List<Question.DifficultyLevel> targetLevels = getMatchingLevels(request.getDifficulty());

        List<Question> bankQuestions = questionRepository.findAll().stream()
                .filter(q -> q.getSubject() != null && q.getSubject().getId().equals(subject.getId()))
                .filter(q -> topic == null || (q.getTopic() != null && q.getTopic().getId().equals(topic.getId())))
                .filter(q -> q.getDifficultyLevel() != null && targetLevels.contains(q.getDifficultyLevel()))
                .collect(Collectors.toList());

        Collections.shuffle(bankQuestions);

        List<Question> selectedQuestions = bankQuestions.stream()
                .limit(request.getCount())
                .collect(Collectors.toCollection(ArrayList::new));

        // 2. Clear gaps with AI if bank is insufficient
        int gap = request.getCount() - selectedQuestions.size();
        if (gap > 0) {
            String topicName = topic != null ? topic.getName() : "General " + subject.getName();
            List<ParsedQuestion> aiParsedQuestions = geminiAiService.generateQuestions(
                    subject.getName(),
                    topicName,
                    request.getDifficulty(),
                    gap);

            for (ParsedQuestion pq : aiParsedQuestions) {
                // Map to entity using existing logic in QuestionProcessingService helper
                // (I'll need to expose the mapToEntity method or similar)
                Question aiQuestion = mapToEntity(pq, subject, topic, user);
                selectedQuestions.add(questionRepository.save(aiQuestion));
            }
        }

        // 3. Save the Test
        Test test = new Test();
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setSubject(subject);
        test.setTopic(topic);
        test.setTargetDifficulty(Question.DifficultyLevel.valueOf(request.getDifficulty()));
        test.setTotalQuestions(selectedQuestions.size());
        test.setTotalMarks(selectedQuestions.size()); // Default 1 mark per question
        test.setTestType(Test.TestType.AI_GENERATED);
        test.setDurationMinutes(request.getDurationMinutes());
        test.setCreatedBy(user);
        test.setQuestions(selectedQuestions);
        test.setCreatedAt(LocalDateTime.now());

        test = testRepository.save(test);

        return mapToResponse(test);
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

    private Question mapToEntity(ParsedQuestion pq, Subject subject, Topic topic, User user) {
        Question q = new Question();
        q.setQuestionText(pq.getQuestionText());
        q.setQuestionType(Question.QuestionType.valueOf(pq.getQuestionType()));
        q.setDifficultyLevel(Question.DifficultyLevel.valueOf(pq.getDifficultyLevel()));
        q.setExplanation(pq.getExplanation());
        q.setSubject(subject);
        q.setTopic(topic);
        q.setCreatedBy(user);
        q.setIsVerified(false);

        if (pq.getOptions() != null) {
            List<QuestionOption> options = pq.getOptions().stream().map(po -> {
                QuestionOption o = new QuestionOption();
                o.setOptionText(po.getText());
                o.setIsCorrect(po.getIsCorrect());
                o.setOptionOrder(po.getOrder());
                o.setQuestion(q);
                return o;
            }).collect(Collectors.toList());
            q.setOptions(options);
        }
        return q;
    }

    private TestResponseDTO mapToResponse(Test test) {
        TestResponseDTO dto = new TestResponseDTO();
        dto.setId(test.getId());
        dto.setTitle(test.getTitle());
        dto.setDescription(test.getDescription());
        dto.setSubject(test.getSubject().getName());
        dto.setTopic(test.getTopic() != null ? test.getTopic().getName() : null);
        dto.setDifficulty(test.getTargetDifficulty().name());
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setTotalMarks(test.getTotalMarks());
        dto.setTestType(test.getTestType().name());
        dto.setDurationMinutes(test.getDurationMinutes());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setQuestions(test.getQuestions().stream()
                .map(QuestionDTO::fromEntity)
                .collect(Collectors.toList()));
        return dto;
    }
}
