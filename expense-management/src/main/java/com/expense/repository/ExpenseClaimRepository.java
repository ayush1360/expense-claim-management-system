package com.expense.repository;

import com.expense.entity.ExpenseClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseClaimRepository extends JpaRepository<ExpenseClaim , Long> {


}
