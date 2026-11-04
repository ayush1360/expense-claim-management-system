package com.expense.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MonthlySummaryResponse {

    private String month;
    private long claimCount;
    private BigDecimal totalAmount;
}
