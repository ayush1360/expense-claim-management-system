package com.expense.controller;

import com.expense.dto.request.ExpenseClaimRequest;
import com.expense.dto.request.ReviewClaimRequest;
import com.expense.dto.response.ExpenseClaimResponse;
import com.expense.enums.ClaimStatus;
import com.expense.service.ExpenseClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ExpenseClaimController {

    private final ExpenseClaimService claimService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseClaimResponse> createClaim(
            @Valid @RequestBody ExpenseClaimRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(claimService.createClaim(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Page<ExpenseClaimResponse>> getMyClaims(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                claimService.getMyClaims(status, pageable)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<ExpenseClaimResponse>> getAllClaims(
            @RequestParam(required = false) ClaimStatus status,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(
                claimService.getAllClaims(status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseClaimResponse> getClaim(@PathVariable Long id) {

        return ResponseEntity.ok(claimService.getClaim(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<ExpenseClaimResponse> updateClaim(@PathVariable Long id, @Valid @RequestBody ExpenseClaimRequest request) {

        return ResponseEntity.ok(claimService.updateClaim(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id) {
        claimService.deleteClaim(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ExpenseClaimResponse> reviewClaim(@PathVariable Long id, @Valid @RequestBody ReviewClaimRequest request) {

        return ResponseEntity.ok(claimService.reviewClaim(id, request)
        );
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<ExpenseClaimResponse>> searchClaims(

            @RequestParam(required = false) ClaimStatus status,

            @RequestParam(required = false) Long employeeId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(required = false) String search,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                claimService.searchClaims(
                        status,
                        employeeId,
                        fromDate,
                        toDate,
                        search,
                        pageable
                )
        );
    }

}
