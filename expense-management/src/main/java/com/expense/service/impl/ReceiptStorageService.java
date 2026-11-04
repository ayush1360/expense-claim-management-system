package com.expense.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
public class ReceiptStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "application/pdf", ".pdf"
    );

    private final Path storageLocation;

    public ReceiptStorageService(
            @Value("${app.upload.dir}") String uploadDirectory) {

        this.storageLocation = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(storageLocation);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not create receipt storage directory", ex
            );
        }
    }

    public String storeFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a receipt file"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size cannot exceed 5 MB"
            );
        }

        String contentType = file.getContentType();

        String extension = ALLOWED_TYPES.get(contentType);

        if (extension == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPG, PNG and PDF files are allowed"
            );
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || !originalFilename.toLowerCase().endsWith(extension)
                && !(extension.equals(".jpg")
                && originalFilename.toLowerCase().endsWith(".jpeg"))) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File extension does not match its content type"
            );
        }

        String storedFilename = UUID.randomUUID() + extension;

        Path destination = storageLocation
                .resolve(storedFilename)
                .normalize();

        if (!destination.startsWith(storageLocation)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }

        try {
            Files.copy(file.getInputStream(), destination);
            return storedFilename;

        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save receipt file"
            );
        }
    }

    public Path loadFile(String filename) {

        Path filePath = storageLocation
                .resolve(filename)
                .normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path"
            );
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Receipt file not found"
            );
        }

        return filePath;
    }

    public void deleteFile(String filename) {

        Path filePath = loadFile(filename);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not delete receipt file"
            );
        }
    }
}
