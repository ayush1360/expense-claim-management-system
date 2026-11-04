package com.expense.service;

import com.expense.dto.request.ExpenseClaimRequest;
import com.expense.dto.request.ReviewClaimRequest;
import com.expense.dto.response.ExpenseClaimResponse;
import com.expense.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ExpenseClaimService {

    ExpenseClaimResponse createClaim(ExpenseClaimRequest request);

    ExpenseClaimResponse getClaim(Long id);

    Page<ExpenseClaimResponse> getMyClaims(
            ClaimStatus status,
            Pageable pageable
    );

    Page<ExpenseClaimResponse> getAllClaims(
            ClaimStatus status,
            Pageable pageable
    );

    Page<ExpenseClaimResponse> searchClaims(
            ClaimStatus status,
            Long employeeId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String search,
            Pageable pageable
    );

    ExpenseClaimResponse updateClaim(
            Long id,
            ExpenseClaimRequest request
    );

    void deleteClaim(Long id);

    ExpenseClaimResponse reviewClaim(
            Long id,
            ReviewClaimRequest request
    );
}
