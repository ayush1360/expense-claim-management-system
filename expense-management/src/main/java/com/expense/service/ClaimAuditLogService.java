package com.expense.service;

import com.expense.dto.response.ClaimAuditLogResponse;

import java.util.List;

public interface ClaimAuditLogService {

    void logAction(Long claimId, String action, String details);

    List<ClaimAuditLogResponse> getClaimHistory(Long claimId);
}
