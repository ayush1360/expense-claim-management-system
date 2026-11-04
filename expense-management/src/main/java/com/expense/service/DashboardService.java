package com.expense.service;

import com.expense.dto.response.EmployeeDashboardResponse;
import com.expense.dto.response.ManagerDashboardResponse;

public interface DashboardService {

    EmployeeDashboardResponse getEmployeeDashboard();

    ManagerDashboardResponse getManagerDashboard();
}
