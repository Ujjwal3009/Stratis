package com.upsc.ai.repository;

import com.upsc.ai.entity.PdfChunk;
import com.upsc.ai.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfChunkRepository extends JpaRepository<PdfChunk, Long> {
    List<PdfChunk> findByPdfDocumentOrderByChunkOrderAsc(PdfDocument pdfDocument);

    void deleteByPdfDocument(PdfDocument pdfDocument);
}
