package com.upsc.ai.controller;

import com.upsc.ai.dto.PdfChunkDTO;
import com.upsc.ai.dto.PdfDocumentDTO;
import com.upsc.ai.dto.PdfUploadResponse;
import com.upsc.ai.entity.PdfDocument;
import com.upsc.ai.entity.PdfDocument.DocumentType;
import com.upsc.ai.entity.User;
import com.upsc.ai.repository.PdfDocumentRepository;
import com.upsc.ai.repository.UserRepository;
import com.upsc.ai.security.UserPrincipal;
import com.upsc.ai.service.PdfChunkService;
import com.upsc.ai.service.PdfDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pdfs")
@Tag(name = "PDF Documents", description = "PDF upload, management, and download endpoints")
public class PdfUploadController {

        @Autowired
        private PdfDocumentService pdfService;

        @Autowired
        private PdfChunkService pdfChunkService;

        @Autowired
        private PdfDocumentRepository pdfDocumentRepository;

        @Autowired
        private UserRepository userRepository;

        @Operation(summary = "Upload PDF", description = "Upload a PDF document with type classification", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF uploaded successfully", content = @Content(schema = @Schema(implementation = PdfUploadResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid file or parameters", content = @Content)
        })
        @PostMapping("/upload")
        public ResponseEntity<PdfUploadResponse> uploadPdf(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam("type") DocumentType type,
                        @RequestParam(value = "description", required = false) String description,
                        @AuthenticationPrincipal UserPrincipal currentUser) {

                if (currentUser == null) {
                        throw new RuntimeException("Unauthorized");
                }
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                PdfUploadResponse response = pdfService.uploadPdf(file, type, user, description);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "List all PDFs", description = "Retrieve all PDF documents")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDFs retrieved successfully", content = @Content(schema = @Schema(implementation = PdfDocumentDTO.class)))
        })
        @GetMapping
        public ResponseEntity<List<PdfDocumentDTO>> listAllPdfs() {
                List<PdfDocumentDTO> pdfs = pdfService.getAllPdfs();
                return ResponseEntity.ok(pdfs);
        }

        @Operation(summary = "List PDFs by type", description = "Retrieve PDFs filtered by document type")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDFs retrieved successfully", content = @Content(schema = @Schema(implementation = PdfDocumentDTO.class)))
        })
        @GetMapping("/type/{type}")
        public ResponseEntity<List<PdfDocumentDTO>> listPdfsByType(@PathVariable DocumentType type) {
                List<PdfDocumentDTO> pdfs = pdfService.getPdfsByType(type);
                return ResponseEntity.ok(pdfs);
        }

        @Operation(summary = "List my uploaded PDFs", description = "Retrieve PDFs uploaded by the current user", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDFs retrieved successfully", content = @Content(schema = @Schema(implementation = PdfDocumentDTO.class)))
        })
        @GetMapping("/my-uploads")
        public ResponseEntity<List<PdfDocumentDTO>> listMyPdfs(@AuthenticationPrincipal UserPrincipal currentUser) {
                if (currentUser == null) {
                        throw new RuntimeException("Unauthorized");
                }
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                List<PdfDocumentDTO> pdfs = pdfService.getPdfsByUser(user);
                return ResponseEntity.ok(pdfs);
        }

        @Operation(summary = "Get PDF details", description = "Retrieve detailed information about a PDF")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF details retrieved", content = @Content(schema = @Schema(implementation = PdfDocumentDTO.class))),
                        @ApiResponse(responseCode = "404", description = "PDF not found", content = @Content)
        })
        @GetMapping("/{id}")
        public ResponseEntity<PdfDocumentDTO> getPdfDetails(@PathVariable Long id) {
                PdfDocumentDTO pdf = pdfService.getPdfById(id);
                return ResponseEntity.ok(pdf);
        }

        @Operation(summary = "Get PDF chunks", description = "Retrieve text chunks extracted from a PDF")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Chunks retrieved successfully", content = @Content(schema = @Schema(implementation = PdfChunkDTO.class))),
                        @ApiResponse(responseCode = "404", description = "PDF not found", content = @Content)
        })
        @GetMapping("/{id}/chunks")
        public ResponseEntity<List<PdfChunkDTO>> getPdfChunks(@PathVariable Long id) {
                PdfDocument pdf = pdfDocumentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("PDF not found"));

                List<PdfChunkDTO> chunks = pdfChunkService.getChunksByPdf(pdf).stream()
                                .map(chunk -> new PdfChunkDTO(chunk.getChunkText(), chunk.getChunkOrder()))
                                .collect(Collectors.toList());

                return ResponseEntity.ok(chunks);
        }

        @Operation(summary = "Download PDF", description = "Download the original PDF file")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF file", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "404", description = "PDF not found", content = @Content)
        })
        @GetMapping("/{id}/download")
        public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
                Resource resource = pdfService.downloadPdf(id);

                return ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_PDF)
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + resource.getFilename() + "\"")
                                .body(resource);
        }

        @Operation(summary = "Delete PDF", description = "Delete a PDF document and its associated data", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF deleted successfully", content = @Content(schema = @Schema(implementation = String.class))),
                        @ApiResponse(responseCode = "404", description = "PDF not found", content = @Content)
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<String> deletePdf(@PathVariable Long id) {
                pdfService.deletePdf(id);
                return ResponseEntity.ok("PDF deleted successfully");
        }
}
