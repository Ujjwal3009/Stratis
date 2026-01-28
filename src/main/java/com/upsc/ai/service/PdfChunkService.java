package com.upsc.ai.service;

import com.upsc.ai.entity.PdfChunk;
import com.upsc.ai.entity.PdfDocument;
import com.upsc.ai.repository.PdfChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PdfChunkService {

    @Autowired
    private PdfChunkRepository pdfChunkRepository;

    private static final int CHUNK_SIZE = 3000; // Character count per chunk
    private static final int CHUNK_OVERLAP = 500; // Overlap between chunks for context continuity

    @Transactional
    public void createChunks(PdfDocument pdf, String fullText) {
        if (fullText == null || fullText.isBlank()) {
            return;
        }

        // Clear existing chunks if any
        pdfChunkRepository.deleteByPdfDocument(pdf);

        List<PdfChunk> chunks = new ArrayList<>();
        int length = fullText.length();
        int order = 0;

        for (int i = 0; i < length; i += (CHUNK_SIZE - CHUNK_OVERLAP)) {
            int end = Math.min(i + CHUNK_SIZE, length);
            String chunkText = fullText.substring(i, end);

            PdfChunk chunk = new PdfChunk();
            chunk.setPdfDocument(pdf);
            chunk.setChunkText(chunkText);
            chunk.setChunkOrder(order++);
            chunks.add(chunk);

            if (end == length)
                break;
        }

        pdfChunkRepository.saveAll(chunks);
    }

    public List<PdfChunk> getChunksByPdf(PdfDocument pdf) {
        return pdfChunkRepository.findByPdfDocumentOrderByChunkOrderAsc(pdf);
    }
}
