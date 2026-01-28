package com.upsc.ai.service;

import com.upsc.ai.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String documentType) {
        try {
            // Create directory structure: uploads/pyq/, uploads/book/, etc.
            String typeDir = uploadDir + "/" + documentType.toLowerCase();
            Path uploadPath = Paths.get(typeDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename to avoid conflicts
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Copy file to the target location
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();

        } catch (IOException ex) {
            throw new BusinessException("Could not store file. Please try again!");
        }
    }

    public Resource loadFileAsResource(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("File not found: " + filePath);
            }
        } catch (Exception ex) {
            throw new BusinessException("File not found: " + filePath);
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            throw new BusinessException("Could not delete file: " + filePath);
        }
    }

    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
