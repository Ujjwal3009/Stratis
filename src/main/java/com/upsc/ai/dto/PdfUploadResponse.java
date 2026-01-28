package com.upsc.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfUploadResponse {
    private Long id;
    private String filename;
    private String originalFilename;
    private Long fileSize;
    private String documentType;
    private String status;
    private LocalDateTime uploadDate;
    private String message;
}
