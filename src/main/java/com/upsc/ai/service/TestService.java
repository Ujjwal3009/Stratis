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

@Service
public class TestService {

    @Autowired
    private TestAttemptRepository attemptRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

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
    public TestResultDTO submitAttempt(TestSubmissionDTO submission) {
        TestAttempt attempt = attemptRepository.findById(submission.getAttemptId())
                .orElseThrow(() -> new BusinessException("Attempt not found"));

        if (attempt.getStatus() != TestAttempt.AttemptStatus.IN_PROGRESS) {
            throw new BusinessException("Attempt already submitted or abandoned");
        }

        Test test = attempt.getTest();
        BigDecimal score = BigDecimal.ZERO;
        List<TestResultDTO.QuestionResultDTO> results = new ArrayList<>();

        Map<Long, Long> userAnswers = submission.getAnswers();

        for (Question question : test.getQuestions()) {
            Long selectedOptionId = userAnswers.get(question.getId());
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
            userAnswerRepository.save(answer);

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
}
