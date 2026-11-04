
package com.expense.repository;

import com.expense.entity.ExpenseClaim;
import com.expense.entity.User;
import com.expense.enums.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenseClaimRepository extends
        JpaRepository<ExpenseClaim, Long>,
        JpaSpecificationExecutor<ExpenseClaim> {

    Page<ExpenseClaim> findByEmployee(User employee, Pageable pageable);

    Page<ExpenseClaim> findByEmployeeAndStatus(
            User employee,
            ClaimStatus status,
            Pageable pageable
    );

    Page<ExpenseClaim> findByStatus(
            ClaimStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT
            COUNT(c),
            COALESCE(SUM(c.amount), 0),
            SUM(CASE WHEN c.status = 'PENDING' THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.status = 'APPROVED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.status = 'REJECTED' THEN 1 ELSE 0 END),
            COALESCE(SUM(CASE WHEN c.status = 'PENDING' THEN c.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN c.status = 'APPROVED' THEN c.amount ELSE 0 END), 0),
            COALESCE(SUM(CASE WHEN c.status = 'REJECTED' THEN c.amount ELSE 0 END), 0)
        FROM ExpenseClaim c
        WHERE c.employee.id = :employeeId
        """)
    List<Object[]> getEmployeeDashboardStats(
            @Param("employeeId") Long employeeId
    );

    @Query("""
        SELECT
            COUNT(c),
            SUM(CASE WHEN c.status = 'PENDING' THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.status = 'APPROVED' THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.status = 'REJECTED' THEN 1 ELSE 0 END)
        FROM ExpenseClaim c
        """)
    List<Object[]> getManagerDashboardStats();

    @Query("""
        SELECT
            c.category,
            COUNT(c),
            COALESCE(SUM(c.amount), 0)
        FROM ExpenseClaim c
        GROUP BY c.category
        ORDER BY SUM(c.amount) DESC
        """)
    List<Object[]> getCategorySummaries();

    @Query(value = """
        SELECT
            date_trunc('month', expense_date) AS month,
            COUNT(*) AS claim_count,
            COALESCE(SUM(amount), 0) AS total_amount
        FROM expense_claims
        GROUP BY date_trunc('month', expense_date)
        ORDER BY month
        """, nativeQuery = true)
    List<Object[]> getMonthlySummaries();
}
