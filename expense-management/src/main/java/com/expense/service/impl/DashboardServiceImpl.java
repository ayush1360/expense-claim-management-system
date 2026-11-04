
package com.expense.service.impl;

import com.expense.dto.response.*;
import com.expense.entity.User;
import com.expense.enums.Role;
import com.expense.repository.ExpenseClaimRepository;
import com.expense.repository.UserRepository;
import com.expense.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseClaimRepository claimRepository;
    private final UserRepository userRepository;

    @Override
    public EmployeeDashboardResponse getEmployeeDashboard() {

        User user = getCurrentUser();

        if (user.getRole() != Role.EMPLOYEE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only employees can access this dashboard"
            );
        }

        List<Object[]> rows =
                claimRepository.getEmployeeDashboardStats(user.getId());

        Object[] stats = firstRow(rows, 8);

        return EmployeeDashboardResponse.builder()
                .totalClaims(toLong(stats[0]))
                .totalClaimedAmount(toBigDecimal(stats[1]))
                .pendingClaims(toLong(stats[2]))
                .approvedClaims(toLong(stats[3]))
                .rejectedClaims(toLong(stats[4]))
                .pendingAmount(toBigDecimal(stats[5]))
                .approvedAmount(toBigDecimal(stats[6]))
                .rejectedAmount(toBigDecimal(stats[7]))
                .build();
    }

    @Override
    public ManagerDashboardResponse getManagerDashboard() {

        User user = getCurrentUser();

        if (user.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can access this dashboard"
            );
        }

        List<Object[]> rows =
                claimRepository.getManagerDashboardStats();

        Object[] stats = firstRow(rows, 4);

        List<CategorySummaryResponse> categorySummaries =
                claimRepository.getCategorySummaries()
                        .stream()
                        .map(row -> CategorySummaryResponse.builder()
                                .category((String) row[0])
                                .claimCount(toLong(row[1]))
                                .totalAmount(toBigDecimal(row[2]))
                                .build())
                        .toList();

        List<MonthlySummaryResponse> monthlySummaries =
                claimRepository.getMonthlySummaries()
                        .stream()
                        .map(row -> MonthlySummaryResponse.builder()
                                .month(formatMonth(row[0]))
                                .claimCount(toLong(row[1]))
                                .totalAmount(toBigDecimal(row[2]))
                                .build())
                        .toList();

        return ManagerDashboardResponse.builder()
                .totalClaims(toLong(stats[0]))
                .pendingClaims(toLong(stats[1]))
                .approvedClaims(toLong(stats[2]))
                .rejectedClaims(toLong(stats[3]))
                .categorySummaries(categorySummaries)
                .monthlySummaries(monthlySummaries)
                .build();
    }

    private Object[] firstRow(List<Object[]> rows, int expectedColumns) {
        if (rows == null || rows.isEmpty()) {
            return new Object[expectedColumns];
        }

        return rows.get(0);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal amount) {
            return amount;
        }

        return new BigDecimal(value.toString());
    }

    private String formatMonth(Object value) {

        if (value == null) {
            return "";
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toString().substring(0, 7);
        }

        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toString().substring(0, 7);
        }

        String month = value.toString();
        return month.substring(0, 7);
    }
}
