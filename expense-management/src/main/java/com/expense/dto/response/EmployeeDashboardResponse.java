package com.expense.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EmployeeDashboardResponse {

    private long totalClaims;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;

    private BigDecimal totalClaimedAmount;
    private BigDecimal pendingAmount;
    private BigDecimal approvedAmount;
    private BigDecimal rejectedAmount;
}
