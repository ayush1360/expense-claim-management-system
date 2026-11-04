package com.expense.service;

import com.expense.dto.response.UserManagementResponse;

import java.util.List;

public interface UserManagementService {

    List<UserManagementResponse> getAllUsers();

    UserManagementResponse updateUserStatus(Long userId, boolean active);
}
