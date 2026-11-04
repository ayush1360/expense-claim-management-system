package com.expense.controller;

import com.expense.dto.response.ClaimAuditLogResponse;
import com.expense.service.ClaimAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.expense.service.ClaimAuditLogService;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimAuditLogController {

    private final ClaimAuditLogService auditLogService;

    @GetMapping("/{claimId}/history")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'MANAGER')")
    public List<ClaimAuditLogResponse> getClaimHistory(@PathVariable Long claimId) {

        return auditLogService.getClaimHistory(claimId);
    }
}
