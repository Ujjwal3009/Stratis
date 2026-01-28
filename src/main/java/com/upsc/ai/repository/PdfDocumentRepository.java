package com.upsc.ai.repository;

import com.upsc.ai.entity.PdfDocument;
import com.upsc.ai.entity.PdfDocument.DocumentStatus;
import com.upsc.ai.entity.PdfDocument.DocumentType;
import com.upsc.ai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {
    List<PdfDocument> findByDocumentType(DocumentType documentType);

    List<PdfDocument> findByUploadedBy(User user);

    List<PdfDocument> findByStatus(DocumentStatus status);

    List<PdfDocument> findByDocumentTypeAndStatus(DocumentType documentType, DocumentStatus status);

    List<PdfDocument> findByUploadedByOrderByUploadDateDesc(User user);

    List<PdfDocument> findAllByOrderByUploadDateDesc();
}
