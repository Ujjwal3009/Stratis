package com.upsc.ai.controller;

import com.upsc.ai.dto.TestRequestDTO;
import com.upsc.ai.dto.TestResponseDTO;
import com.upsc.ai.dto.TestResultDTO;
import com.upsc.ai.dto.TestSubmissionDTO;
import com.upsc.ai.entity.Test;
import com.upsc.ai.entity.TestAttempt;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.TestRepository;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.TestGenerationService;
import com.upsc.ai.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestGenerationService generationService;

    @Autowired
    private TestService testService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/generate")
    public ResponseEntity<TestResponseDTO> generateTest(
            @RequestBody TestRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        User user;
        if (currentUser == null) {
            // Fallback for testing/unauthenticated access
            user = userRepository.findByEmail("test@upsc-ai.com")
                    .orElseGet(() -> userRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No users found in database")));
        } else {
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        return ResponseEntity.ok(generationService.generateTest(request, user));
    }

    @PostMapping("/{testId}/start")
    public ResponseEntity<Long> startTest(
            @PathVariable Long testId,
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

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        TestAttempt attempt = testService.startAttempt(test, user);
        return ResponseEntity.ok(attempt.getId());
    }

    @PostMapping("/submit")
    public ResponseEntity<TestResultDTO> submitTest(@RequestBody TestSubmissionDTO submission) {
        return ResponseEntity.ok(testService.submitAttempt(submission));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Test> getTest(@PathVariable Long id) {
        return ResponseEntity.ok(testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found")));
    }
}
