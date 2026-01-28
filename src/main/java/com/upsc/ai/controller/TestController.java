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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tests")
@Tag(name = "Tests", description = "Test generation, execution, and submission endpoints")
public class TestController {

    @Autowired
    private TestGenerationService generationService;

    @Autowired
    private TestService testService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Generate test", description = "Generate a new test based on specified criteria (subject, difficulty, number of questions)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test generated successfully", content = @Content(schema = @Schema(implementation = TestResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
    })
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

    @Operation(summary = "Start test attempt", description = "Start a new attempt for an existing test", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test attempt started, returns attempt ID", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
    })
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

    @Operation(summary = "Submit test", description = "Submit test answers and get results")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test submitted successfully", content = @Content(schema = @Schema(implementation = TestResultDTO.class))),
            @ApiResponse(responseCode = "404", description = "Test attempt not found", content = @Content)
    })
    @PostMapping("/submit")
    public ResponseEntity<TestResultDTO> submitTest(@RequestBody TestSubmissionDTO submission) {
        return ResponseEntity.ok(testService.submitAttempt(submission));
    }

    @Operation(summary = "Get test by ID", description = "Retrieve test details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Test found", content = @Content(schema = @Schema(implementation = Test.class))),
            @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Test> getTest(@PathVariable Long id) {
        return ResponseEntity.ok(testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found")));
    }
}
