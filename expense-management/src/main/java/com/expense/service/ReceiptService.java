package com.expense.service;

import com.expense.dto.response.ReceiptResponse;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface ReceiptService {

    ReceiptResponse uploadReceipt(Long claimId, MultipartFile file);

    Path getReceiptFile(Long claimId);

    ReceiptResponse getReceiptMetadata(Long claimId);
}
