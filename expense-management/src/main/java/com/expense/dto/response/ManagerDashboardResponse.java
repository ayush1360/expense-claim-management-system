package com.expense.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ManagerDashboardResponse {

    private long totalClaims;
    private long pendingClaims;
    private long approvedClaims;
    private long rejectedClaims;

    private List<CategorySummaryResponse> categorySummaries;
    private List<MonthlySummaryResponse> monthlySummaries;
}
