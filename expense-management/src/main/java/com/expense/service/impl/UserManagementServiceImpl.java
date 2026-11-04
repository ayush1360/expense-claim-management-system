package com.expense.service.impl;

import com.expense.dto.response.UserManagementResponse;
import com.expense.entity.User;
import com.expense.enums.Role;
import com.expense.repository.UserRepository;
import com.expense.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserManagementResponse updateUserStatus(
            Long userId, boolean active) {

        User currentManager = getCurrentUser();

        if (currentManager.getRole() != Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can manage users");
        }

        if (currentManager.getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot change your own account status");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        if (user.getRole() == Role.MANAGER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Manager accounts cannot be deactivated here");
        }

        user.setActive(active);

        User updatedUser = userRepository.save(user);

        return mapToResponse(updatedUser);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));
    }

    private UserManagementResponse mapToResponse(User user) {

        return UserManagementResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
