package com.upsc.ai.controller;

import com.upsc.ai.entity.Question;
import com.upsc.ai.entity.ReportedQuestion;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.QuestionRepository;
import com.upsc.ai.repository.ReportedQuestionRepository;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    @Autowired
    private ReportedQuestionRepository reportedQuestionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/report-question")
    public ResponseEntity<?> reportQuestion(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        Long questionId = Long.valueOf(request.get("questionId").toString());
        String description = (String) request.get("description");

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        ReportedQuestion report = new ReportedQuestion();
        report.setQuestion(question);
        report.setReportedBy(user);
        report.setIssueDescription(description);

        reportedQuestionRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Issue reported successfully"));
    }
}
