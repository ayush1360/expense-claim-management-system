package com.expense.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategorySummaryResponse {

    private String category;
    private long claimCount;
    private BigDecimal totalAmount;
}
