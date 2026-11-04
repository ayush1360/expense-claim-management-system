package com.expense.repository;

import com.expense.entity.ClaimAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimAuditLogRepository extends JpaRepository<ClaimAuditLog, Long> {

    List<ClaimAuditLog> findByClaimIdOrderByPerformedAtDesc(Long claimId);
}
