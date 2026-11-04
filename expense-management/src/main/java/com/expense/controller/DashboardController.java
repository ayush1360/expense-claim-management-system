package com.expense.controller;

import com.expense.dto.response.EmployeeDashboardResponse;
import com.expense.dto.response.ManagerDashboardResponse;
import com.expense.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/employee")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public EmployeeDashboardResponse getEmployeeDashboard() {
        return dashboardService.getEmployeeDashboard();
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public ManagerDashboardResponse getManagerDashboard() {
        return dashboardService.getManagerDashboard();
    }
}
