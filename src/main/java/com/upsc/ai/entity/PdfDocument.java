package com.upsc.ai.entity;

import com.upsc.ai.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SoftDelete
public class PdfDocument extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @Column(columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onPrePersist() {
        if (uploadDate == null) {
            uploadDate = LocalDateTime.now();
        }
    }

    public enum DocumentType {
        PYQ, // Previous Year Questions
        BOOK, // UPSC preparation books
        CURRENT_AFFAIRS // Current affairs PDFs
    }

    public enum DocumentStatus {
        UPLOADED, // File uploaded successfully
        PROCESSING, // Being processed by AI
        PROCESSED, // Processing complete
        FAILED // Processing failed
    }
}
