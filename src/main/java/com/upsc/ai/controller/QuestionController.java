package com.upsc.ai.controller;

import com.upsc.ai.dto.ProcessingResult;
import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.QuestionProcessingService;
import com.upsc.ai.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionProcessingService processingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/parse/{pdfId}")
    public ResponseEntity<ProcessingResult> parsePdf(
            @PathVariable Long pdfId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user;
        if (currentUser == null) {
            user = userRepository.findByEmail("test@upsc-ai.com")
                    .orElseGet(() -> userRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No users found in database")));
        } else {
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        ProcessingResult result = processingService.processPdf(pdfId, user);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> listQuestions(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String difficulty) {

        return ResponseEntity.ok(questionService.listQuestions(subjectId, difficulty));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }
}
