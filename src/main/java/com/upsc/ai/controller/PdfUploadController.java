package com.upsc.ai.controller;

import com.upsc.ai.dto.PdfDocumentDTO;
import com.upsc.ai.dto.PdfUploadResponse;
import com.upsc.ai.entity.PdfDocument.DocumentType;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.PdfDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pdfs")
public class PdfUploadController {

    @Autowired
    private PdfDocumentService pdfService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<PdfUploadResponse> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") DocumentType type,
            @RequestParam(value = "description", required = false) String description,
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

        PdfUploadResponse response = pdfService.uploadPdf(file, type, user, description);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PdfDocumentDTO>> listAllPdfs() {
        List<PdfDocumentDTO> pdfs = pdfService.getAllPdfs();
        return ResponseEntity.ok(pdfs);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<PdfDocumentDTO>> listPdfsByType(@PathVariable DocumentType type) {
        List<PdfDocumentDTO> pdfs = pdfService.getPdfsByType(type);
        return ResponseEntity.ok(pdfs);
    }

    @GetMapping("/my-uploads")
    public ResponseEntity<List<PdfDocumentDTO>> listMyPdfs(@AuthenticationPrincipal UserPrincipal currentUser) {
        User user;
        if (currentUser == null) {
            user = userRepository.findByEmail("test@upsc-ai.com")
                    .orElseGet(() -> userRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No users found in database")));
        } else {
            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        List<PdfDocumentDTO> pdfs = pdfService.getPdfsByUser(user);
        return ResponseEntity.ok(pdfs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PdfDocumentDTO> getPdfDetails(@PathVariable Long id) {
        PdfDocumentDTO pdf = pdfService.getPdfById(id);
        return ResponseEntity.ok(pdf);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        Resource resource = pdfService.downloadPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePdf(@PathVariable Long id) {
        pdfService.deletePdf(id);
        return ResponseEntity.ok("PDF deleted successfully");
    }
}
