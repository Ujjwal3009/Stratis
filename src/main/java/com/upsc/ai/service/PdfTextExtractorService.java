package com.upsc.ai.service;

import com.upsc.ai.exception.BusinessException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class PdfTextExtractorService {

    public String extractText(String pdfFilePath) {
        try (PDDocument document = Loader.loadPDF(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();

            // Basic settings for text stripping
            stripper.setSortByPosition(true);
            stripper.setStartPage(1);
            stripper.setEndPage(document.getNumberOfPages());

            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new BusinessException(
                        "Could not extract any text from the PDF file. It might be scanned or empty.");
            }

            return sanitizeText(text);
        } catch (IOException e) {
            throw new BusinessException("Error reading PDF file: " + e.getMessage());
        }
    }

    public String extractTextFromPages(String pdfFilePath, int startPage, int endPage) {
        try (PDDocument document = Loader.loadPDF(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();

            stripper.setSortByPosition(true);
            stripper.setStartPage(Math.max(1, startPage));
            stripper.setEndPage(Math.min(document.getNumberOfPages(), endPage));

            String text = stripper.getText(document);
            return sanitizeText(text);
        } catch (IOException e) {
            throw new BusinessException("Error reading PDF file: " + e.getMessage());
        }
    }

    private String sanitizeText(String text) {
        if (text == null)
            return "";
        // Remove excessive whitespace, normalize line endings
        return text.replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll(" +", " ")
                .replaceAll("\\n\\s*\\n+", "\n\n")
                .trim();
    }
}
