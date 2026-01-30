package com.upsc.ai.service;

import com.upsc.ai.dto.TestResultDTO;
import com.upsc.ai.dto.TestSubmissionDTO;
import com.upsc.ai.entity.*;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.upsc.ai.dto.TestAnalysisDTO;
import java.util.HashMap;

@Service
public class TestService {

    @Autowired
    private TestAttemptRepository attemptRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private GeminiAiService geminiAiService;

    private final Gson gson = new Gson();

    @Autowired
    private UserQuestionAttemptRepository userQuestionAttemptRepository;

    @Autowired
    private BehaviourAnalyticsService behaviourAnalyticsService;

    @Autowired
    private UserTestMetricsRepository userTestMetricsRepository;

    @Transactional
    public TestAttempt startAttempt(Test test, User user) {
        // Check if there's already an in-progress attempt for this test and user
        return attemptRepository.findFirstByTestAndUserAndStatusOrderByStartedAtDesc(
                test, user, TestAttempt.AttemptStatus.IN_PROGRESS)
                .orElseGet(() -> {
                    TestAttempt attempt = new TestAttempt();
                    attempt.setTest(test);
                    attempt.setUser(user);
                    attempt.setStatus(TestAttempt.AttemptStatus.IN_PROGRESS);
                    attempt.setStartedAt(LocalDateTime.now());
                    attempt.setTotalMarks(test.getTotalMarks() != null ? test.getTotalMarks() : 0);
                    return attemptRepository.save(attempt);
                });
    }

    @Transactional
    public TestResultDTO submitAttempt(TestSubmissionDTO submission, Long userId) {
        TestAttempt attempt = attemptRepository.findById(submission.getAttemptId())
                .orElseThrow(() -> new BusinessException("Attempt not found"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new BusinessException("Access Denied: You do not own this test attempt");
        }

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("Attempt already submitted or abandoned");
        }

        Test test = attempt.getTest();
        BigDecimal score = BigDecimal.ZERO;
        List<TestResultDTO.QuestionResultDTO> results = new ArrayList<>();

        List<TestSubmissionDTO.AnswerDTO> userAnswers = submission.getAnswers();
        Map<Long, TestSubmissionDTO.AnswerDTO> answerMap = userAnswers != null ? userAnswers.stream()
                .collect(Collectors.toMap(TestSubmissionDTO.AnswerDTO::getQuestionId, a -> a, (a1, a2) -> a1))
                : new java.util.HashMap<>();

        for (Question question : test.getQuestions()) {
            TestSubmissionDTO.AnswerDTO answerDTO = answerMap.get(question.getId());
            Long selectedOptionId = (answerDTO != null) ? answerDTO.getSelectedOptionId() : null;
            Integer timeSpent = (answerDTO != null && answerDTO.getTimeSpentSeconds() != null)
                    ? answerDTO.getTimeSpentSeconds()
                    : 0;

            QuestionOption selectedOption = null;

            if (selectedOptionId != null) {
                selectedOption = question.getOptions().stream()
                        .filter(o -> o.getId().equals(selectedOptionId))
                        .findFirst()
                        .orElse(null);
            }

            QuestionOption correctOption = question.getOptions().stream()
                    .filter(QuestionOption::getIsCorrect)
                    .findFirst()
                    .orElse(null);

            boolean isCorrect = selectedOption != null && selectedOption.getIsCorrect();
            if (isCorrect)
                score = score.add(BigDecimal.ONE);

            // Save UserAnswer
            UserAnswer answer = new UserAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedOption(selectedOption);
            answer.setIsCorrect(isCorrect);
            answer.setTimeSpentSeconds(timeSpent);

            // Behavioral Analysis
            String answerType = "UNKNOWN";
            if (answerDTO != null) {
                int changes = answerDTO.getSelectionChangeCount() != null ? answerDTO.getSelectionChangeCount() : 0;
                int hovers = answerDTO.getHoverCount() != null ? answerDTO.getHoverCount() : 0;
                String difficulty = question.getDifficultyLevel() != null ? question.getDifficultyLevel().name()
                        : "MEDIUM";

                // Blind Guess: Very fast
                if (timeSpent < 3 || (timeSpent < 5 && !difficulty.equals("EASY"))) {
                    answerType = "BLIND_GUESS";
                }
                // Educated Guess: Changed mind OR hovered multiple options (reading) OR
                // deliberate process
                else if (changes > 0 || hovers > 2 || (timeSpent > 45)) {
                    answerType = "EDUCATED_GUESS";
                }
                // Sure: Moderate time, no changes, focused
                else if (timeSpent >= 5 && changes == 0) {
                    answerType = "SURE";
                }
            }
            answer.setAnswerType(answerType);

            userAnswerRepository.save(answer);

            // Save Granular UserQuestionAttempt
            UserQuestionAttempt uqa = new UserQuestionAttempt();
            uqa.setUser(attempt.getUser());
            uqa.setQuestion(question);
            uqa.setTestAttempt(attempt);
            uqa.setSelectedOption(selectedOption != null ? selectedOption.getOptionText() : null);
            uqa.setIsCorrect(isCorrect);
            uqa.setTimeTakenSeconds(timeSpent);
            uqa.setAttemptedAt(LocalDateTime.now());

            if (answerDTO != null) {
                uqa.setOptionChangeCount(
                        answerDTO.getSelectionChangeCount() != null ? answerDTO.getSelectionChangeCount() : 0);
                uqa.setHoverCount(answerDTO.getHoverCount() != null ? answerDTO.getHoverCount() : 0);
                if (answerDTO.getEliminatedOptionIds() != null) {
                    uqa.setEliminatedOptionIdsJson(gson.toJson(answerDTO.getEliminatedOptionIds()));
                }

                if (answerDTO.getFirstSelectedOptionId() != null) {
                    QuestionOption firstOpt = question.getOptions().stream()
                            .filter(o -> o.getId().equals(answerDTO.getFirstSelectedOptionId()))
                            .findFirst().orElse(null);
                    uqa.setFirstSelectedOption(firstOpt != null ? firstOpt.getOptionText() : null);
                }
            }

            userQuestionAttemptRepository.save(uqa);

            results.add(new TestResultDTO.QuestionResultDTO(
                    question.getId(),
                    selectedOptionId,
                    correctOption != null ? correctOption.getId() : null,
                    isCorrect,
                    question.getExplanation()));
        }

        attempt.setScore(score);
        attempt.setStatus(TestAttempt.AttemptStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Generate and Save Behavioural Metrics
        try {
            behaviourAnalyticsService.analyzeAndSaveMetrics(attempt, userAnswers, test.getQuestions());
        } catch (Exception e) {
            System.err.println("Error generating behavioural metrics: " + e.getMessage());
            // Don't fail submission if metrics fail
        }

        return new TestResultDTO(
                attempt.getId(),
                test.getId(),
                score,
                test.getTotalQuestions(),
                attempt.getStatus().name(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                results);
    }

    public List<TestAttempt> getAttemptsByUser(User user) {
        return attemptRepository.findByUserOrderByStartedAtDesc(user);
    }

    @Transactional
    public TestAnalysisDTO generateAnalysis(Long attemptId, Long userId) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException("Attempt not found"));

        if (!attempt.getUser().getId().equals(userId)) {
            throw new BusinessException("Access Denied: You do not own this test attempt");
        }

        if (attempt.getStatus() != TestAttempt.AttemptStatus.COMPLETED) {
            throw new BusinessException("Cannot analyze an in-progress or abandoned test.");
        }

        List<UserAnswer> answers = userAnswerRepository.findByAttempt_Id(attemptId);
        Test test = attempt.getTest();

        TestAnalysisDTO analysis = new TestAnalysisDTO();
        analysis.setAttemptId(attemptId);
        analysis.setTestId(test.getId());
        analysis.setOverallScore(attempt.getScore());
        analysis.setTotalQuestions(test.getTotalQuestions());

        int correct = 0;
        int wrong = 0;
        int unanswered = 0;
        long totalTime = 0;

        Map<String, List<UserAnswer>> topicMap = new HashMap<>();

        for (UserAnswer answer : answers) {
            if (answer.getIsCorrect() == null)
                unanswered++;
            else if (answer.getIsCorrect())
                correct++;
            else
                wrong++;

            if (answer.getTimeSpentSeconds() != null) {
                totalTime += answer.getTimeSpentSeconds();
            }

            String topic = answer.getQuestion().getTopic() != null ? answer.getQuestion().getTopic().getName()
                    : "General";
            topicMap.computeIfAbsent(topic, k -> new ArrayList<>()).add(answer);
        }

        analysis.setCorrectCount(correct);
        analysis.setWrongCount(wrong);
        analysis.setUnansweredCount(unanswered);
        analysis.setTotalTimeSpentSeconds(totalTime);
        analysis.setAccuracyPercentage((double) correct / test.getTotalQuestions() * 100);

        List<TestAnalysisDTO.TopicAnalysisDTO> topicPerformances = new ArrayList<>();
        for (Map.Entry<String, List<UserAnswer>> entry : topicMap.entrySet()) {
            List<UserAnswer> topicAnswers = entry.getValue();
            int tCorrect = (int) topicAnswers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect())
                    .count();
            int tTotal = topicAnswers.size();
            double tAccuracy = (double) tCorrect / tTotal * 100;
            long tAvgTime = (long) topicAnswers.stream()
                    .mapToInt(a -> a.getTimeSpentSeconds() != null ? a.getTimeSpentSeconds() : 0)
                    .average().orElse(0);

            String status = tAccuracy > 80 ? "MASTERED" : (tAccuracy > 50 ? "NEED_PRACTICE" : "WEAK");
            topicPerformances.add(new TestAnalysisDTO.TopicAnalysisDTO(entry.getKey(), tCorrect, tTotal, tAccuracy,
                    tAvgTime, status));
        }
        analysis.setTopicPerformances(topicPerformances);

        // Prepare context for Gemini
        // Prepare context for Gemini - OPTIMIZED for Cost
        StringBuilder contextBuilder = new StringBuilder();

        Optional<UserTestMetrics> metricsOpt = userTestMetricsRepository.findByTestAttemptId(attemptId);
        if (metricsOpt.isPresent()) {
            UserTestMetrics m = metricsOpt.get();

            TestAnalysisDTO.BehaviouralMetricsDTO bMetrics = new TestAnalysisDTO.BehaviouralMetricsDTO();
            bMetrics.setAccuracyPercentage(
                    m.getAccuracyPercentage() != null ? m.getAccuracyPercentage().doubleValue() : null);
            bMetrics.setAttemptRatio(m.getAttemptRatio() != null ? m.getAttemptRatio().doubleValue() : null);
            bMetrics.setNegativeMarks(m.getNegativeMarks() != null ? m.getNegativeMarks().doubleValue() : null);
            bMetrics.setFirstInstinctAccuracy(
                    m.getFirstInstinctAccuracy() != null ? m.getFirstInstinctAccuracy().doubleValue() : null);
            bMetrics.setEliminationEfficiency(
                    m.getEliminationEfficiency() != null ? m.getEliminationEfficiency().doubleValue() : null);
            bMetrics.setImpulsiveErrorCount(m.getImpulsiveErrorCount());
            bMetrics.setOverthinkingErrorCount(m.getOverthinkingErrorCount());
            bMetrics.setGuessProbability(
                    m.getGuessProbability() != null ? m.getGuessProbability().doubleValue() : null);
            bMetrics.setRiskAppetiteScore(
                    m.getRiskAppetiteScore() != null ? m.getRiskAppetiteScore().doubleValue() : null);
            bMetrics.setConfidenceIndex(m.getConfidenceIndex() != null ? m.getConfidenceIndex().doubleValue() : null);
            bMetrics.setConsistencyIndex(
                    m.getConsistencyIndex() != null ? m.getConsistencyIndex().doubleValue() : null);

            if (m.getCognitiveBreakdownJson() != null) {
                bMetrics.setCognitiveBreakdown(
                        gson.fromJson(m.getCognitiveBreakdownJson(), new TypeToken<Map<String, Object>>() {
                        }.getType()));
            }
            if (m.getFatigueCurveJson() != null) {
                bMetrics.setFatigueCurve(gson.fromJson(m.getFatigueCurveJson(), new TypeToken<Map<String, Object>>() {
                }.getType()));
            }
            analysis.setBehaviouralMetrics(bMetrics);

            contextBuilder.append("PRE-CALCULATED METRICS:\n");
            contextBuilder.append("- Accuracy: ").append(m.getAccuracyPercentage()).append("%\n");
            contextBuilder.append("- Impulsive Errors: ").append(m.getImpulsiveErrorCount()).append("\n");
            contextBuilder.append("- Overthinking Errors: ").append(m.getOverthinkingErrorCount()).append("\n");
            contextBuilder.append("- Elimination Efficiency: ").append(m.getEliminationEfficiency()).append("%\n");
            contextBuilder.append("- First Instinct Accuracy: ").append(m.getFirstInstinctAccuracy()).append("%\n");
            contextBuilder.append("- Risk Appetite: ").append(m.getRiskAppetiteScore()).append("\n");

            // Add weak topics (Accuracy < 50%)
            contextBuilder.append("- Weak Topics: ");
            String weakTopics = topicPerformances.stream()
                    .filter(t -> t.getAccuracy() < 50)
                    .map(TestAnalysisDTO.TopicAnalysisDTO::getTopicName)
                    .collect(Collectors.joining(", "));
            contextBuilder.append(weakTopics.isEmpty() ? "None" : weakTopics).append("\n");

        } else {
            // Fallback to legacy calc if metrics missing
            contextBuilder.append("User Score: ").append(correct).append("/").append(test.getTotalQuestions())
                    .append("\n");
            contextBuilder.append("Topic Performance:\n");
            topicPerformances.forEach(
                    t -> contextBuilder.append("- ").append(t.getTopicName()).append(": ").append(t.getAccuracy())
                            .append("% accuracy, ").append(t.getAvgTimeSpentSeconds()).append("s avg time\n"));
        }

        // Only include critical mistakes (top 3 worst) to save context
        contextBuilder.append("\nSample Mistakes (Top 3):\n");
        int mistakeCount = 0;
        for (UserAnswer answer : answers) {
            if (answer.getIsCorrect() != null && !answer.getIsCorrect()) {
                if (mistakeCount++ >= 3)
                    break;
                contextBuilder.append("Q: ").append(answer.getQuestion().getQuestionText()).append("\n");
                contextBuilder.append("Difficulty: ").append(answer.getQuestion().getDifficultyLevel())
                        .append("\n---\n");
            }
        }

        try {
            String aiResponse = geminiAiService.generateAnalysisInsights(contextBuilder.toString(), attempt.getUser());
            // Extract JSON from response if it has markdown
            if (aiResponse.contains("```json")) {
                aiResponse = aiResponse.substring(aiResponse.indexOf("```json") + 7, aiResponse.lastIndexOf("```"))
                        .trim();
            } else if (aiResponse.contains("```")) {
                aiResponse = aiResponse.substring(aiResponse.indexOf("```") + 3, aiResponse.lastIndexOf("```")).trim();
            }

            Map<String, Object> aiMap = gson.fromJson(aiResponse, new TypeToken<Map<String, Object>>() {
            }.getType());
            analysis.setAiDiagnosticSummary((String) aiMap.get("diagnosticSummary"));
            analysis.setSynthesizedStudyNotes((String) aiMap.get("studyNotes"));

            @SuppressWarnings("unchecked")
            List<Map<String, String>> swList = (List<Map<String, String>>) aiMap.get("strengthWeaknessPairs");
            if (swList != null) {
                analysis.setStrengthWeaknessPairs(swList.stream()
                        .map(m -> new TestAnalysisDTO.StrengthWeaknessDTO(m.get("point"), m.get("strategy")))
                        .collect(Collectors.toList()));
            }

            // Map mistake counts
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mList = (List<Map<String, Object>>) aiMap.get("mistakeCategorization");
            if (mList != null) {
                Map<String, Integer> mCounts = new HashMap<>();
                for (Map<String, Object> m : mList) {
                    String type = (String) m.get("type");
                    mCounts.put(type, mCounts.getOrDefault(type, 0) + 1);
                }
                analysis.setMistakeTypeCounts(mCounts);
            }

        } catch (Exception e) {
            analysis.setAiDiagnosticSummary("AI analysis currently unavailable: " + e.getMessage());
        }

        return analysis;
    }
}
