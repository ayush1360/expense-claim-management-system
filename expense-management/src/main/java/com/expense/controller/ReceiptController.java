package com.expense.controller;

import com.expense.dto.response.ReceiptResponse;
import com.expense.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/claims/{claimId}/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ReceiptResponse> uploadReceipt(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file) {

        ReceiptResponse response =
                receiptService.uploadReceipt(claimId, file);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public ResponseEntity<Resource> downloadReceipt(
            @PathVariable Long claimId) throws IOException {

        ReceiptResponse metadata =
                receiptService.getReceiptMetadata(claimId);

        Path filePath = receiptService.getReceiptFile(claimId);
        Resource resource = new UrlResource(filePath.toUri());

        String contentType = metadata.getContentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(metadata.getOriginalFileName())
                                .build().toString())
                .contentLength(metadata.getFileSize())
                .body(resource);
    }
}
