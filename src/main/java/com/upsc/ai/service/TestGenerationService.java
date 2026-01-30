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

    @Autowired
    private PdfDocumentRepository pdfDocumentRepository;

    @Autowired
    private PdfChunkService pdfChunkService;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private TestAttemptRepository attemptRepository;

    @Autowired
    private InventoryRefillService inventoryRefillService;

    @Autowired
    private QuestionService questionService;

    @Transactional
    public TestResponseDTO generateTest(TestRequestDTO request, User user) {
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Subject not found"));

        Topic topic = request.getTopicId() != null ? topicRepository.findById(request.getTopicId()).orElse(null) : null;
        List<Question.DifficultyLevel> targetLevels = getMatchingLevels(request.getDifficulty());

        List<Question> selectedQuestions = new ArrayList<>();
        int required = request.getCount();

        // 1. Fetch matching PYQ questions (Unseen)
        selectedQuestions.addAll(questionRepository.findUnseenQuestions(
                user.getId(),
                subject.getId(),
                topic != null ? topic.getId() : null,
                targetLevels,
                "PYQ",
                org.springframework.data.domain.PageRequest.of(0, required)));

        // 2. Fallback to AI-generated questions in DB (Unseen)
        if (selectedQuestions.size() < required) {
            int needed = required - selectedQuestions.size();
            selectedQuestions.addAll(questionRepository.findUnseenQuestions(
                    user.getId(),
                    subject.getId(),
                    topic != null ? topic.getId() : null,
                    targetLevels,
                    "AI",
                    org.springframework.data.domain.PageRequest.of(0, needed)));
        }

        // 3. Fallback to Real-time AI if still insufficient
        if (selectedQuestions.size() < required) {
            int gap = required - selectedQuestions.size();
            List<Question> newAiQuestions = generateRealTimeQuestions(request, user, subject, topic, gap);
            selectedQuestions.addAll(newAiQuestions);
        }

        // Shuffle the result
        Collections.shuffle(selectedQuestions);

        // 4. Trigger Async Inventory Refill if needed (checking if we had to use AI or
        // running low)
        inventoryRefillService.triggerRefill(subject, topic, request.getDifficulty());

        // 5. Save the Test
        Test test = new Test();
        test.setTitle(request.getTitle());
        test.setDescription(request.getDescription());
        test.setSubject(subject);
        test.setTopic(topic);
        test.setTargetDifficulty(Question.DifficultyLevel.valueOf(request.getDifficulty()));
        test.setTotalQuestions(selectedQuestions.size());
        test.setTotalMarks(selectedQuestions.size()); // Default 1 mark per question
        test.setTestType(Test.TestType.AI_GENERATED);

        Integer duration = request.getDurationMinutes();
        test.setDurationMinutes(duration != null ? duration : 60);

        test.setCreatedBy(user);
        test.setQuestions(selectedQuestions);
        test.setCreatedAt(LocalDateTime.now());

        test = testRepository.save(test);

        return mapToResponse(test);
    }

    private List<Question> generateRealTimeQuestions(TestRequestDTO request, User user, Subject subject, Topic topic,
            int count) {
        String context = null;
        Long contextPdfId = request.getPdfId();

        if (contextPdfId == null) {
            contextPdfId = pdfDocumentRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PdfDocument.DocumentStatus.PROCESSED)
                    .filter(p -> p.getUploadedBy() != null && p.getUploadedBy().getId().equals(user.getId()))
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .map(PdfDocument::getId)
                    .findFirst()
                    .orElse(null);
        }

        if (contextPdfId != null) {
            PdfDocument pdf = pdfDocumentRepository.findById(contextPdfId).orElse(null);
            if (pdf != null) {
                List<PdfChunk> chunks = pdfChunkService.getChunksByPdf(pdf);
                if (!chunks.isEmpty()) {
                    context = chunks.stream()
                            .map(PdfChunk::getChunkText)
                            .collect(Collectors.joining("\n---\n"));
                    int limit = 30000;
                    if (context.length() > limit) {
                        context = context.substring(0, limit) + "... [TRUNCATED FOR PROMPT]";
                    }
                }
            }
        }

        String topicName = topic != null ? topic.getName() : "General " + subject.getName();
        List<ParsedQuestion> aiParsedQuestions = geminiAiService.generateQuestions(
                subject.getName(),
                topicName,
                request.getDifficulty(),
                count,
                context,
                user);

        List<Question> newQuestions = new ArrayList<>();
        for (ParsedQuestion pq : aiParsedQuestions) {
            newQuestions.add(questionService.createQuestionFromAi(pq, subject, topic, user));
        }
        return newQuestions;
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

    @Transactional
    public TestResponseDTO generateRemedialTest(Long attemptId, User user) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException("Attempt not found"));

        List<UserAnswer> wrongAnswers = userAnswerRepository.findByAttempt_Id(attemptId).stream()
                .filter(a -> a.getIsCorrect() != null && !a.getIsCorrect())
                .collect(Collectors.toList());

        if (wrongAnswers.isEmpty()) {
            throw new BusinessException(
                    "No weak topics identified - you got a perfect score or haven't finished the test!");
        }

        // Collect topics from wrong answers
        List<Topic> weakTopics = wrongAnswers.stream()
                .map(a -> a.getQuestion().getTopic())
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (weakTopics.isEmpty()) {
            // If questions have no specific topics, fallback to subject-level remedial test
            Subject subject = attempt.getTest().getSubject();
            TestRequestDTO request = new TestRequestDTO();
            request.setSubjectId(subject.getId());
            request.setCount(10);
            request.setDurationMinutes(15);
            request.setDifficulty(attempt.getTest().getTargetDifficulty().name());
            request.setTitle("Remedial: " + subject.getName());
            return generateTest(request, user);
        }

        // Generate a combined test from multiple weak topics
        Test remedialTest = new Test();
        remedialTest.setTitle("Remedial Test - Target: Weak Topics");
        remedialTest.setCreatedBy(user);
        remedialTest.setSubject(attempt.getTest().getSubject());
        remedialTest.setTargetDifficulty(attempt.getTest().getTargetDifficulty());
        remedialTest.setDurationMinutes(15);
        remedialTest.setTestType(Test.TestType.AI_GENERATED);

        List<Question> remedialQuestions = new ArrayList<>();
        int questionsPerTopic = Math.max(1, 10 / weakTopics.size());

        List<Question.DifficultyLevel> levels = getMatchingLevels(
                attempt.getTest().getTargetDifficulty().name());

        for (Topic topic : weakTopics) {
            // 1. Fetch matching PYQ questions (Unseen)
            List<Question> topicQuestions = questionRepository.findUnseenQuestions(
                    user.getId(),
                    attempt.getTest().getSubject().getId(),
                    topic.getId(),
                    levels,
                    "PYQ",
                    org.springframework.data.domain.PageRequest.of(0, questionsPerTopic));

            remedialQuestions.addAll(topicQuestions);

            // 2. Fallback to AI Bank
            if (topicQuestions.size() < questionsPerTopic) {
                int gap = questionsPerTopic - topicQuestions.size();
                List<Question> aiBankQuestions = questionRepository.findUnseenQuestions(
                        user.getId(),
                        attempt.getTest().getSubject().getId(),
                        topic.getId(),
                        levels,
                        "AI",
                        org.springframework.data.domain.PageRequest.of(0, gap));
                remedialQuestions.addAll(aiBankQuestions);
            }
        }

        // 3. Fallback: If still not enough, fill with random questions from weak topics
        // (even if seen, if strictly needed)
        // OR trigger inventory refill. For now, we prefer distinct questions.
        // If really low, just serve what we have or add random from subject.

        Collections.shuffle(remedialQuestions);
        remedialQuestions = remedialQuestions.stream().limit(10).collect(Collectors.toList());

        remedialTest.setQuestions(remedialQuestions);
        remedialTest.setTotalQuestions(remedialTest.getQuestions().size());
        remedialTest.setTotalMarks(remedialTest.getTotalQuestions()); // Default marking

        Test savedTest = testRepository.save(remedialTest);
        return mapToResponse(savedTest);
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
