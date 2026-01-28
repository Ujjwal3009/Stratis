package com.upsc.ai.controller;

import com.upsc.ai.dto.ProcessingResult;
import com.upsc.ai.dto.QuestionDTO;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.QuestionProcessingService;
import com.upsc.ai.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "Question management and PDF parsing endpoints")
public class QuestionController {

    @Autowired
    private QuestionProcessingService processingService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Parse PDF and extract questions", description = "Process a PDF document to extract questions using AI", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF parsed successfully", content = @Content(schema = @Schema(implementation = ProcessingResult.class))),
            @ApiResponse(responseCode = "404", description = "PDF not found", content = @Content)
    })
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

    @Operation(summary = "List questions", description = "Retrieve questions with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully", content = @Content(schema = @Schema(implementation = QuestionDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<QuestionDTO>> listQuestions(
            @Parameter(description = "Filter by subject ID") @RequestParam(required = false) Long subjectId,
            @Parameter(description = "Filter by difficulty level") @RequestParam(required = false) String difficulty) {

        return ResponseEntity.ok(questionService.listQuestions(subjectId, difficulty));
    }

    @Operation(summary = "Get question by ID", description = "Retrieve a specific question by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question found", content = @Content(schema = @Schema(implementation = QuestionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Question not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @Operation(summary = "Delete question", description = "Delete a question by its ID", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Question deleted successfully", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Question not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok("Question deleted successfully");
    }
}
