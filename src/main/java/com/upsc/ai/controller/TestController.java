package com.upsc.ai.controller;

import com.upsc.ai.dto.TestRequestDTO;
import com.upsc.ai.dto.TestResponseDTO;
import com.upsc.ai.dto.TestAnalysisDTO;
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
import java.util.List;
import java.util.stream.Collectors;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.exception.ResourceNotFoundException;
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
@RequestMapping("/api/v1/tests")
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

                if (currentUser == null) {
                        throw new RuntimeException("Unauthorized");
                }
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

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

                if (currentUser == null) {
                        throw new BusinessException("Unauthorized: Please log in again");
                }
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

                Test test = testRepository.findById(testId)
                                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", testId));

                TestAttempt attempt = testService.startAttempt(test, user);
                return ResponseEntity.ok(attempt.getId());
        }

        @Operation(summary = "Submit test", description = "Submit test answers and get results")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Test submitted successfully", content = @Content(schema = @Schema(implementation = TestResultDTO.class))),
                        @ApiResponse(responseCode = "404", description = "Test attempt not found", content = @Content)
        })
        @PostMapping("/submit")
        public ResponseEntity<TestResultDTO> submitTest(
                        @RequestBody TestSubmissionDTO submission,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                if (currentUser == null) {
                        throw new BusinessException("Unauthorized: Please log in again");
                }
                return ResponseEntity.ok(testService.submitAttempt(submission, currentUser.getId()));
        }

        @GetMapping("/attempts/{attemptId}/analysis")
        public ResponseEntity<TestAnalysisDTO> getTestAnalysis(
                        @PathVariable Long attemptId,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                if (currentUser == null) {
                        throw new BusinessException("Unauthorized: Please log in again");
                }
                return ResponseEntity.ok(testService.generateAnalysis(attemptId, currentUser.getId()));
        }

        @PostMapping("/attempts/{attemptId}/remedial")
        public ResponseEntity<TestResponseDTO> createRemedialTest(
                        @PathVariable Long attemptId,
                        @AuthenticationPrincipal UserPrincipal currentUser) {
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
                return ResponseEntity.ok(generationService.generateRemedialTest(attemptId, user));
        }

        @Operation(summary = "Get test by ID", description = "Retrieve test details by ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Test found", content = @Content(schema = @Schema(implementation = Test.class))),
                        @ApiResponse(responseCode = "404", description = "Test not found", content = @Content)
        })
        @GetMapping("/{id}")
        public ResponseEntity<TestResponseDTO> getTest(@PathVariable Long id) {
                Test test = testRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Test", "id", id));
                return ResponseEntity.ok(TestResponseDTO.fromEntity(test));
        }

        @Operation(summary = "Get user test history", description = "Retrieve past test attempts")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "History retrieved", content = @Content(schema = @Schema(implementation = TestResultDTO.class)))
        })
        @GetMapping("/history")
        public ResponseEntity<List<TestResultDTO>> getTestHistory(@AuthenticationPrincipal UserPrincipal currentUser) {
                if (currentUser == null) {
                        throw new BusinessException("Unauthorized: Please log in again");
                }
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<TestAttempt> attempts = testService.getAttemptsByUser(user);

                List<TestResultDTO> results = attempts.stream().map(attempt -> {
                        // Calculate results or use a lighter DTO if needed. Reusing TestResultDTO for
                        // now but without full question details could be better.
                        // For simplicity, we just map basic info. The query loads entities properly?
                        // We need to map TestAttempt to TestResultDTO manually or via helper.
                        // Converting manually for summary view:
                        return new TestResultDTO(
                                        attempt.getId(),
                                        attempt.getTest().getId(),
                                        attempt.getScore(),
                                        attempt.getTest().getTotalQuestions(),
                                        attempt.getStatus().name(),
                                        attempt.getStartedAt(),
                                        attempt.getCompletedAt(),
                                        null // Don't send full questions list for history list to save bandwidth
                        );
                }).collect(Collectors.toList());

                return ResponseEntity.ok(results);
        }
}
