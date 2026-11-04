package com.expense.service.impl;

import com.expense.dto.response.ClaimAuditLogResponse;
import com.expense.entity.ClaimAuditLog;
import com.expense.entity.User;
import com.expense.repository.ClaimAuditLogRepository;
import com.expense.repository.UserRepository;
import com.expense.service.ClaimAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.expense.repository.ExpenseClaimRepository;
import com.expense.entity.ExpenseClaim;
import com.expense.enums.Role;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimAuditLogServiceImpl implements ClaimAuditLogService {

    private final ClaimAuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ExpenseClaimRepository claimRepository;

    @Override
    @Transactional
    public void logAction(Long claimId, String action, String details) {

        User currentUser = getCurrentUser();

        ClaimAuditLog auditLog = ClaimAuditLog.builder()
                .claimId(claimId)
                .performedBy(currentUser)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ClaimAuditLogResponse> getClaimHistory(Long claimId) {

        User currentUser = getCurrentUser();

        ExpenseClaim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Claim not found"));

        boolean isManager = currentUser.getRole() == Role.MANAGER;
        boolean isOwner = claim.getEmployee().getId()
                .equals(currentUser.getId());

        if (!isManager && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view this claim's history");
        }

        return auditLogRepository
                .findByClaimIdOrderByPerformedAtDesc(claimId)
                .stream()
                .map(log -> ClaimAuditLogResponse.builder()
                        .id(log.getId())
                        .claimId(log.getClaimId())
                        .performedBy(log.getPerformedBy().getFullName())
                        .action(log.getAction())
                        .details(log.getDetails())
                        .performedAt(log.getPerformedAt())
                        .build())
                .toList();
    }


    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }


}
