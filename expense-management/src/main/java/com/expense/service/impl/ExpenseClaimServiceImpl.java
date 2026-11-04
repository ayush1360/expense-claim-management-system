
package com.expense.service.impl;

import com.expense.dto.request.ExpenseClaimRequest;
import com.expense.dto.request.ReviewClaimRequest;
import com.expense.dto.response.ExpenseClaimResponse;
import com.expense.entity.ExpenseClaim;
import com.expense.entity.User;
import com.expense.enums.ClaimStatus;
import com.expense.enums.Role;
import com.expense.repository.ExpenseClaimRepository;
import com.expense.repository.UserRepository;
import com.expense.service.ExpenseClaimService;

import jakarta.persistence.criteria.Predicate;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseClaimServiceImpl implements ExpenseClaimService {

    private final ExpenseClaimRepository claimRepository;
    private final UserRepository userRepository;

    // Get currently authenticated user
    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));
    }

    // Find claim by ID
    private ExpenseClaim findClaim(Long id) {

        return claimRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Claim not found"
                ));
    }

    // Check claim ownership
    private void checkOwnership(ExpenseClaim claim, User user) {

        if (user.getRole() != Role.MANAGER
                && !claim.getEmployee().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this claim"
            );
        }
    }

    // Convert Entity to Response DTO
    private ExpenseClaimResponse toResponse(ExpenseClaim claim) {

        return ExpenseClaimResponse.builder()
                .id(claim.getId())
                .title(claim.getTitle())
                .amount(claim.getAmount())
                .category(claim.getCategory())
                .expenseDate(claim.getExpenseDate())
                .description(claim.getDescription())
                .status(claim.getStatus())
                .employeeName(claim.getEmployee().getFullName())
                .employeeEmail(claim.getEmployee().getEmail())
                .reviewedBy(
                        claim.getReviewedBy() != null
                                ? claim.getReviewedBy().getFullName()
                                : null
                )
                .rejectionComment(claim.getRejectionComment())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    // Copy request data into claim entity
    private void copyRequest(
            ExpenseClaimRequest request,
            ExpenseClaim claim) {

        claim.setTitle(request.getTitle().trim());
        claim.setAmount(request.getAmount());
        claim.setCategory(request.getCategory().trim());
        claim.setExpenseDate(request.getExpenseDate());
        claim.setDescription(request.getDescription());
    }

    // CREATE CLAIM
    @Override
    public ExpenseClaimResponse createClaim(ExpenseClaimRequest request) {

        User employee = getCurrentUser();

        if (employee.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only employees can create claims"
            );
        }

        ExpenseClaim claim = ExpenseClaim.builder()
                .employee(employee)
                .status(ClaimStatus.PENDING)
                .build();

        copyRequest(request, claim);

        return toResponse(claimRepository.save(claim));
    }

    // GET CLAIM BY ID
    @Override
    @Transactional(readOnly = true)
    public ExpenseClaimResponse getClaim(Long id) {

        User currentUser = getCurrentUser();

        ExpenseClaim claim = findClaim(id);

        checkOwnership(claim, currentUser);

        return toResponse(claim);
    }

    // GET LOGGED-IN EMPLOYEE CLAIMS
    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseClaimResponse> getMyClaims(
            ClaimStatus status,
            Pageable pageable) {

        User employee = getCurrentUser();

        Page<ExpenseClaim> claims = status == null
                ? claimRepository.findByEmployee(employee, pageable)
                : claimRepository.findByEmployeeAndStatus(
                employee,
                status,
                pageable
        );

        return claims.map(this::toResponse);
    }

    // GET ALL CLAIMS (MANAGER)
    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseClaimResponse> getAllClaims(
            ClaimStatus status,
            Pageable pageable) {

        Page<ExpenseClaim> claims = status == null
                ? claimRepository.findAll(pageable)
                : claimRepository.findByStatus(status, pageable);

        return claims.map(this::toResponse);
    }

    // SEARCH AND FILTER CLAIMS (MANAGER)
    @Override
    @Transactional(readOnly = true)
    public Page<ExpenseClaimResponse> searchClaims(
            ClaimStatus status,
            Long employeeId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String search,
            Pageable pageable) {

        User manager = getCurrentUser();

        if (manager.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Manager access required"
            );
        }

        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From date cannot be after to date"
            );
        }

        Specification<ExpenseClaim> specification =
                (root, query, cb) -> {

                    List<Predicate> predicates = new ArrayList<>();

                    // Filter by claim status
                    if (status != null) {
                        predicates.add(
                                cb.equal(root.get("status"), status)
                        );
                    }

                    // Filter by employee ID
                    if (employeeId != null) {
                        predicates.add(
                                cb.equal(
                                        root.get("employee").get("id"),
                                        employeeId
                                )
                        );
                    }

                    // Filter by starting date
                    if (fromDate != null) {
                        predicates.add(
                                cb.greaterThanOrEqualTo(
                                        root.<LocalDateTime>get("expenseDate"),
                                        fromDate
                                )
                        );
                    }

                    // Filter by ending date
                    if (toDate != null) {
                        predicates.add(
                                cb.lessThanOrEqualTo(
                                        root.<LocalDateTime>get("expenseDate"),
                                        toDate
                                )
                        );
                    }

                    // Search by title, category or employee name
                    if (search != null && !search.isBlank()) {

                        String pattern =
                                "%" + search.trim().toLowerCase(Locale.ROOT) + "%";

                        predicates.add(
                                cb.or(
                                        cb.like(
                                                cb.lower(root.<String>get("title")),
                                                pattern
                                        ),
                                        cb.like(
                                                cb.lower(root.<String>get("category")),
                                                pattern
                                        ),
                                        cb.like(
                                                cb.lower(
                                                        root.get("employee")
                                                                .<String>get("fullName")
                                                ),
                                                pattern
                                        )
                                )
                        );
                    }

                    return cb.and(predicates.toArray(new Predicate[0]));
                };

        Page<ExpenseClaim> claims =
                claimRepository.findAll(specification, pageable);

        return claims.map(this::toResponse);
    }

    // UPDATE CLAIM
    @Override
    public ExpenseClaimResponse updateClaim(
            Long id,
            ExpenseClaimRequest request) {

        User employee = getCurrentUser();

        ExpenseClaim claim = findClaim(id);

        checkOwnership(claim, employee);

        if (employee.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only employees can update claims"
            );
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending claims can be updated"
            );
        }

        copyRequest(request, claim);

        return toResponse(claimRepository.save(claim));
    }

    // DELETE CLAIM
    @Override
    public void deleteClaim(Long id) {

        User employee = getCurrentUser();

        ExpenseClaim claim = findClaim(id);

        checkOwnership(claim, employee);

        if (employee.getRole() != Role.EMPLOYEE
                || claim.getStatus() != ClaimStatus.PENDING) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only your pending claims can be deleted"
            );
        }

        claimRepository.delete(claim);
    }

    // APPROVE OR REJECT CLAIM (MANAGER)
    @Override
    public ExpenseClaimResponse reviewClaim(
            Long id,
            ReviewClaimRequest request) {

        User manager = getCurrentUser();

        if (manager.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Manager access required"
            );
        }

        ExpenseClaim claim = findClaim(id);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This claim has already been reviewed"
            );
        }

        if (request.getStatus() != ClaimStatus.APPROVED
                && request.getStatus() != ClaimStatus.REJECTED) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision must be APPROVED or REJECTED"
            );
        }

        if (request.getStatus() == ClaimStatus.REJECTED
                && (request.getRejectionComment() == null
                || request.getRejectionComment().isBlank())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Rejection comment is required"
            );
        }

        claim.setStatus(request.getStatus());
        claim.setReviewedBy(manager);

        claim.setRejectionComment(
                request.getStatus() == ClaimStatus.REJECTED
                        ? request.getRejectionComment().trim()
                        : null
        );

        return toResponse(claimRepository.save(claim));
    }
}
