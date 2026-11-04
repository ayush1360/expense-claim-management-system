package com.expense.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimAuditLogResponse {

    private Long id;
    private Long claimId;
    private String performedBy;
    private String action;
    private String details;
    private LocalDateTime performedAt;
}
