package com.upsc.ai.service;

import com.google.gson.Gson;
import com.upsc.ai.dto.TestSubmissionDTO;
import com.upsc.ai.entity.*;
import com.upsc.ai.repository.UserTestMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BehaviourAnalyticsService {

    @Autowired
    private UserTestMetricsRepository metricsRepository;

    private final Gson gson = new Gson();

    @Transactional
    public void analyzeAndSaveMetrics(TestAttempt attempt, List<TestSubmissionDTO.AnswerDTO> answers,
            List<Question> questions) {
        UserTestMetrics metrics = new UserTestMetrics();
        metrics.setUser(attempt.getUser());
        metrics.setTestAttempt(attempt);

        int totalQuestions = questions.size();
        int attempted = 0;
        int correct = 0;
        int wrong = 0;
        int firstInstinctCorrect = 0;
        int firstInstinctTotal = 0;
        int eliminationSuccess = 0;
        int eliminationTotal = 0;
        int impulsiveErrors = 0;
        int overthinkingErrors = 0;

        // Map helpers
        Map<Long, TestSubmissionDTO.AnswerDTO> answerMap = answers.stream()
                .collect(Collectors.toMap(TestSubmissionDTO.AnswerDTO::getQuestionId, a -> a));

        for (Question q : questions) {
            TestSubmissionDTO.AnswerDTO ans = answerMap.get(q.getId());
            if (ans == null || ans.getSelectedOptionId() == null)
                continue;

            attempted++;
            boolean isCorrect = isOptionCorrect(q, ans.getSelectedOptionId());
            if (isCorrect)
                correct++;
            else
                wrong++;

            // First Instinct
            if (ans.getFirstSelectedOptionId() != null) {
                firstInstinctTotal++;
                if (isOptionCorrect(q, ans.getFirstSelectedOptionId())) {
                    firstInstinctCorrect++;
                }
            }

            // Elimination Efficiency (Simplistic: if eliminated choices were wrong,
            // efficiency +)
            if (ans.getEliminatedOptionIds() != null && !ans.getEliminatedOptionIds().isEmpty()) {
                eliminationTotal++;
                boolean eliminatedOnlyWrong = ans.getEliminatedOptionIds().stream()
                        .noneMatch(optId -> isOptionCorrect(q, optId));
                if (eliminatedOnlyWrong)
                    eliminationSuccess++;
            }

            // Impulsive Errors (Time < 5s & Wrong)
            int time = ans.getTimeSpentSeconds() != null ? ans.getTimeSpentSeconds() : 0;
            if (time < 5 && !isCorrect) {
                impulsiveErrors++;
            }

            // Overthinking Errors (Changed mind > 1 & Wrong & Time > 60s)
            if (ans.getSelectionChangeCount() > 1 && !isCorrect && time > 60) {
                overthinkingErrors++;
            }
        }

        // Calculations
        metrics.setAccuracyPercentage(calculatePercentage(correct, attempted));
        metrics.setAttemptRatio(calculatePercentage(attempted, totalQuestions));
        metrics.setNegativeMarks(calculateNegativeMarks(wrong));

        metrics.setFirstInstinctAccuracy(calculatePercentage(firstInstinctCorrect, firstInstinctTotal));
        metrics.setEliminationEfficiency(calculatePercentage(eliminationSuccess, eliminationTotal));

        metrics.setImpulsiveErrorCount(impulsiveErrors);
        metrics.setOverthinkingErrorCount(overthinkingErrors);

        metrics.setGuessProbability(calculateGuessProbability(questions, answerMap));
        metrics.setCognitiveBreakdownJson(gson.toJson(calculateCognitiveBreakdown(questions, answerMap)));
        metrics.setFatigueCurveJson(gson.toJson(calculateFatigueCurve(questions, answerMap)));
        metrics.setRiskAppetiteScore(calculateRiskAppetite(questions, answerMap));

        metrics.setConfidenceIndex(calculateConfidenceIndex(firstInstinctCorrect, firstInstinctTotal, answers));
        metrics.setConsistencyIndex(calculateConsistencyIndex(questions, answerMap));

        metricsRepository.save(metrics);
    }

    private boolean isOptionCorrect(Question q, Long optionId) {
        if (q.getOptions() == null)
            return false;
        return q.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .map(QuestionOption::getIsCorrect)
                .orElse(false);
    }

    private BigDecimal calculatePercentage(int part, int total) {
        if (total == 0)
            return BigDecimal.ZERO;
        return BigDecimal.valueOf(part)
                .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNegativeMarks(int wrong) {
        return BigDecimal.valueOf(wrong).multiply(BigDecimal.valueOf(0.66)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateGuessProbability(List<Question> questions,
            Map<Long, TestSubmissionDTO.AnswerDTO> answerMap) {
        int guessScore = 0;
        int count = 0;

        for (Question q : questions) {
            TestSubmissionDTO.AnswerDTO ans = answerMap.get(q.getId());
            if (ans == null || ans.getSelectedOptionId() == null)
                continue;

            count++;
            int time = ans.getTimeSpentSeconds() != null ? ans.getTimeSpentSeconds() : 0;
            boolean isCorrect = isOptionCorrect(q, ans.getSelectedOptionId());

            // Guess indicator: Low time (< 8s), no changes, correct answer
            if (time < 8 && ans.getSelectionChangeCount() == 0 && isCorrect) {
                guessScore++;
            }
        }
        return calculatePercentage(guessScore, count);
    }

    private Map<String, Object> calculateCognitiveBreakdown(List<Question> questions,
            Map<Long, TestSubmissionDTO.AnswerDTO> answerMap) {
        int size = questions.size();
        if (size == 0)
            return new HashMap<>();

        Map<String, Object> breakdown = new HashMap<>();
        int quarterSize = Math.max(1, size / 4);

        for (int i = 0; i < 4; i++) {
            int start = i * quarterSize;
            int end = (i == 3) ? size : (i + 1) * quarterSize;

            int qCorrect = 0;
            int qAttempted = 0;
            for (int j = start; j < end; j++) {
                Question q = questions.get(j);
                TestSubmissionDTO.AnswerDTO ans = answerMap.get(q.getId());
                if (ans != null && ans.getSelectedOptionId() != null) {
                    qAttempted++;
                    if (isOptionCorrect(q, ans.getSelectedOptionId()))
                        qCorrect++;
                }
            }
            breakdown.put("q" + (i + 1) + "_accuracy", calculatePercentage(qCorrect, qAttempted));
        }
        return breakdown;
    }

    private Map<String, Object> calculateFatigueCurve(List<Question> questions,
            Map<Long, TestSubmissionDTO.AnswerDTO> answerMap) {
        int midpoint = questions.size() / 2;
        if (midpoint == 0)
            return new HashMap<>();

        // Compare first half vs second half speed and accuracy
        double firstHalfTime = 0, secondHalfTime = 0;
        int firstHalfAcc = 0, secondHalfAcc = 0;

        for (int i = 0; i < questions.size(); i++) {
            TestSubmissionDTO.AnswerDTO ans = answerMap.get(questions.get(i).getId());
            if (ans == null)
                continue;

            int time = ans.getTimeSpentSeconds() != null ? ans.getTimeSpentSeconds() : 0;
            boolean correct = isOptionCorrect(questions.get(i), ans.getSelectedOptionId());

            if (i < midpoint) {
                firstHalfTime += time;
                if (correct)
                    firstHalfAcc++;
            } else {
                secondHalfTime += time;
                if (correct)
                    secondHalfAcc++;
            }
        }

        Map<String, Object> curve = new HashMap<>();
        curve.put("fatigue_index", secondHalfTime > firstHalfTime ? "SLOWING_DOWN" : "CONSISTENT");
        curve.put("accuracy_drop", firstHalfAcc - secondHalfAcc);
        return curve;
    }

    private BigDecimal calculateConfidenceIndex(int firstInstinctCorrect, int firstInstinctTotal,
            List<TestSubmissionDTO.AnswerDTO> answers) {
        // High confidence if low changes and high first instinct accuracy
        BigDecimal instinctAcc = calculatePercentage(firstInstinctCorrect, firstInstinctTotal);
        long changes = answers.stream().filter(a -> a.getSelectionChangeCount() != null)
                .mapToInt(a -> a.getSelectionChangeCount()).sum();

        double confidence = instinctAcc.doubleValue() - (changes * 2.0);
        return BigDecimal.valueOf(Math.max(0, Math.min(100, confidence))).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateConsistencyIndex(List<Question> questions,
            Map<Long, TestSubmissionDTO.AnswerDTO> answerMap) {
        // Simple consistency: Performance across difficulty levels
        Map<Question.DifficultyLevel, Integer> levelCorrect = new HashMap<>();
        Map<Question.DifficultyLevel, Integer> levelTotal = new HashMap<>();

        for (Question q : questions) {
            TestSubmissionDTO.AnswerDTO ans = answerMap.get(q.getId());
            if (ans == null || ans.getSelectedOptionId() == null)
                continue;

            levelTotal.merge(q.getDifficultyLevel(), 1, Integer::sum);
            if (isOptionCorrect(q, ans.getSelectedOptionId())) {
                levelCorrect.merge(q.getDifficultyLevel(), 1, Integer::sum);
            }
        }

        if (levelTotal.isEmpty())
            return BigDecimal.ZERO;

        double variance = levelTotal.keySet().stream()
                .mapToDouble(l -> calculatePercentage(levelCorrect.getOrDefault(l, 0), levelTotal.get(l)).doubleValue())
                .summaryStatistics().getAverage();

        return BigDecimal.valueOf(variance).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRiskAppetite(List<Question> questions,
            Map<Long, TestSubmissionDTO.AnswerDTO> answerMap) {
        long hardAttempted = questions.stream()
                .filter(q -> q.getDifficultyLevel() == Question.DifficultyLevel.HARD)
                .filter(q -> answerMap.containsKey(q.getId()) && answerMap.get(q.getId()).getSelectedOptionId() != null)
                .count();

        long hardTotal = questions.stream()
                .filter(q -> q.getDifficultyLevel() == Question.DifficultyLevel.HARD)
                .count();

        return calculatePercentage((int) hardAttempted, (int) hardTotal);
    }
}
