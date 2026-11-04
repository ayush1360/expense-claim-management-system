package com.expense.controller;

import com.expense.dto.response.UserManagementResponse;
import com.expense.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public List<UserManagementResponse> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @PatchMapping("/{userId}/status")
    public UserManagementResponse updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        return userManagementService.updateUserStatus(userId, active);
    }
}
