package com.expense.service.impl;

import com.expense.dto.response.ReceiptResponse;
import com.expense.entity.ExpenseClaim;
import com.expense.entity.Receipt;
import com.expense.entity.User;
import com.expense.enums.ClaimStatus;
import com.expense.enums.Role;
import com.expense.repository.ExpenseClaimRepository;
import com.expense.repository.ReceiptRepository;
import com.expense.repository.UserRepository;
import com.expense.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ExpenseClaimRepository claimRepository;
    private final UserRepository userRepository;
    private final ReceiptStorageService storageService;

    @Override
    @Transactional
    public ReceiptResponse uploadReceipt(Long claimId, MultipartFile file) {

        User currentUser = getCurrentUser();

        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Claim not found"));

        if (currentUser.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only employees can upload receipts");
        }

        if (!claim.getEmployee().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot upload a receipt for another employee's claim");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Receipts can only be uploaded for pending claims");
        }

        if (receiptRepository.findByClaimId(claimId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A receipt has already been uploaded for this claim");
        }

        String storedFilename = storageService.storeFile(file);

        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "receipt";
            }

            originalFilename = originalFilename.replace("\\", "/");
            originalFilename = originalFilename.substring(
                    originalFilename.lastIndexOf('/') + 1);

            Receipt receipt = Receipt.builder()
                    .claim(claim)
                    .originalFileName(originalFilename)
                    .storedFileName(storedFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

            Receipt savedReceipt = receiptRepository.save(receipt);

            return mapToResponse(savedReceipt);

        } catch (RuntimeException ex) {
            storageService.deleteFile(storedFilename);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse getReceiptMetadata(Long claimId) {

        Receipt receipt = getAuthorizedReceipt(claimId);
        return mapToResponse(receipt);
    }

    @Override
    @Transactional(readOnly = true)
    public Path getReceiptFile(Long claimId) {

        Receipt receipt = getAuthorizedReceipt(claimId);
        return storageService.loadFile(receipt.getStoredFileName());
    }

    private Receipt getAuthorizedReceipt(Long claimId) {

        User currentUser = getCurrentUser();

        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Claim not found"));

        boolean isManager = currentUser.getRole() == Role.MANAGER;
        boolean isOwner = claim.getEmployee().getId()
                .equals(currentUser.getId());

        if (!isManager && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this receipt");
        }

        return receiptRepository.findByClaimId(claimId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receipt not found"));
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private ReceiptResponse mapToResponse(Receipt receipt) {

        return ReceiptResponse.builder()
                .id(receipt.getId())
                .claimId(receipt.getClaim().getId())
                .originalFileName(receipt.getOriginalFileName())
                .contentType(receipt.getContentType())
                .fileSize(receipt.getFileSize())
                .uploadedAt(receipt.getUploadedAt())
                .build();
    }
}
