package com.upsc.ai.service;

import com.upsc.ai.dto.PdfDocumentDTO;
import com.upsc.ai.dto.PdfUploadResponse;
import com.upsc.ai.entity.PdfDocument;
import com.upsc.ai.entity.PdfDocument.DocumentStatus;
import com.upsc.ai.entity.PdfDocument.DocumentType;
import com.upsc.ai.entity.User;
import com.upsc.ai.exception.BusinessException;
import com.upsc.ai.repository.PdfDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfDocumentService {

    @Autowired
    private PdfDocumentRepository repository;

    @Autowired
    private FileStorageService fileStorageService;

    public PdfUploadResponse uploadPdf(MultipartFile file, DocumentType type, User user, String description) {
        // Validate file
        validatePdfFile(file);

        // Store file
        String filePath = fileStorageService.storeFile(file, type.name());

        // Save metadata to database
        PdfDocument document = new PdfDocument();
        document.setFilename(filePath.substring(filePath.lastIndexOf("/") + 1));
        document.setOriginalFilename(file.getOriginalFilename());
        document.setFilePath(filePath);
        document.setFileSize(file.getSize());
        document.setDocumentType(type);
        document.setUploadedBy(user);
        document.setStatus(DocumentStatus.UPLOADED);
        document.setDescription(description);
        document.setUploadDate(LocalDateTime.now());

        PdfDocument saved = repository.save(document);

        return new PdfUploadResponse(
                saved.getId(),
                saved.getFilename(),
                saved.getOriginalFilename(),
                saved.getFileSize(),
                saved.getDocumentType().name(),
                saved.getStatus().name(),
                saved.getUploadDate(),
                "PDF uploaded successfully");
    }

    public List<PdfDocumentDTO> getAllPdfs() {
        return repository.findAllByOrderByUploadDateDesc()
                .stream()
                .map(PdfDocumentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PdfDocumentDTO> getPdfsByType(DocumentType type) {
        return repository.findByDocumentType(type)
                .stream()
                .map(PdfDocumentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PdfDocumentDTO> getPdfsByUser(User user) {
        return repository.findByUploadedByOrderByUploadDateDesc(user)
                .stream()
                .map(PdfDocumentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PdfDocumentDTO getPdfById(Long id) {
        PdfDocument document = repository.findById(id)
                .orElseThrow(() -> new BusinessException("PDF document not found with id: " + id));
        return PdfDocumentDTO.fromEntity(document);
    }

    public Resource downloadPdf(Long id) {
        PdfDocument document = repository.findById(id)
                .orElseThrow(() -> new BusinessException("PDF document not found with id: " + id));

        return fileStorageService.loadFileAsResource(document.getFilePath());
    }

    public void deletePdf(Long id) {
        PdfDocument document = repository.findById(id)
                .orElseThrow(() -> new BusinessException("PDF document not found with id: " + id));

        // Delete file from storage
        fileStorageService.deleteFile(document.getFilePath());

        // Delete metadata from database
        repository.delete(document);
    }

    private void validatePdfFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty. Please select a valid PDF file.");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BusinessException("Only PDF files are allowed. Please upload a PDF file.");
        }

        // Check file size (max 50MB)
        long maxSize = 50 * 1024 * 1024; // 50MB in bytes
        if (file.getSize() > maxSize) {
            throw new BusinessException("File size exceeds 50MB limit. Please upload a smaller file.");
        }

        // Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new BusinessException("Invalid filename.");
        }
    }
}
