package com.upsc.ai.controller;

import com.upsc.ai.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
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
}
