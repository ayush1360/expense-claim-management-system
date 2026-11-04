package com.expense.dto.response;

import com.expense.enums.ClaimStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ExpenseClaimResponse {

    private Long id;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDateTime expenseDate;
    private String description;
    private ClaimStatus status;
    private String employeeName;
    private String employeeEmail;
    private String reviewedBy;
    private String rejectionComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
