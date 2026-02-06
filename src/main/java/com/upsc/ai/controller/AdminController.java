package com.upsc.ai.controller;

import com.upsc.ai.service.DataImportService;
import com.upsc.ai.repository.UserTokenUsageRepository;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.repository.ReportedQuestionRepository;
import com.upsc.ai.entity.ReportedQuestion;
import com.upsc.ai.entity.SystemConfig;
import com.upsc.ai.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private DataImportService dataImportService;

    @PostMapping("/import-pyqs")
    public ResponseEntity<String> importPyqs(@RequestParam(defaultValue = "upscpyqs_tagged.csv") String filePath) {
        try {
            int count = dataImportService.importQuestionsFromCsv(filePath);
            return ResponseEntity.ok("Successfully imported " + count + " questions from " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error importing data: " + e.getMessage());
        }
    }

    @Autowired
    private UserTokenUsageRepository tokenUsageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportedQuestionRepository reportedQuestionRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTokens", tokenUsageRepository.getTotalTokensUsed());
        stats.put("totalUsers", userRepository.count());
        stats.put("usageByArea", tokenUsageRepository.getUsageByFeatureArea());
        stats.put("pendingReports",
                reportedQuestionRepository.findByStatus(ReportedQuestion.IssueStatus.PENDING).size());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        return ResponseEntity.ok(reportedQuestionRepository.findAll());
    }

    @PostMapping("/reports/{id}/resolve")
    public ResponseEntity<?> resolveReport(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ReportedQuestion report = reportedQuestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportedQuestion.IssueStatus.RESOLVED);
        report.setAdminComment(body.get("comment"));
        reportedQuestionRepository.save(report);
        return ResponseEntity.ok(Map.of("message", "Report resolved"));
    }

    @Autowired
    private ConfigService configService;

    @GetMapping("/configs")
    public ResponseEntity<List<SystemConfig>> getConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @PostMapping("/configs")
    public ResponseEntity<SystemConfig> updateConfig(@RequestBody SystemConfig config) {
        return ResponseEntity.ok(configService.setConfig(config));
    }
}
