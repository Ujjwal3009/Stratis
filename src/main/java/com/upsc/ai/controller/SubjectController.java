package com.upsc.ai.controller;

import com.upsc.ai.entity.Subject;
import com.upsc.ai.repository.SubjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "Subjects", description = "Subject management endpoints")
public class SubjectController {

    @Autowired
    private SubjectRepository subjectRepository;

    @Operation(summary = "List all subjects", description = "Retrieve all available subjects")
    @GetMapping
    public ResponseEntity<List<Subject>> listSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }
}
