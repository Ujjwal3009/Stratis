package com.upsc.ai.dto;

import com.upsc.ai.entity.PdfDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocumentDTO {
    private Long id;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String documentType;
    private String status;
    private LocalDateTime uploadDate;
    private String uploadedByUsername;
    private String description;

    public static PdfDocumentDTO fromEntity(PdfDocument document) {
        return new PdfDocumentDTO(
                document.getId(),
                document.getFilename(),
                document.getOriginalFilename(),
                document.getFileSize(),
                document.getDocumentType().name(),
                document.getStatus().name(),
                document.getUploadDate(),
                document.getUploadedBy() != null ? document.getUploadedBy().getUsername() : null,
                document.getDescription());
    }
}
