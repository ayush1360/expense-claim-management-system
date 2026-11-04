package com.expense.dto.request;

import com.expense.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewClaimRequest {

    @NotNull(message = "Decision is required")
    private ClaimStatus status;

    @Size(max = 1000)
    private String rejectionComment;
}
