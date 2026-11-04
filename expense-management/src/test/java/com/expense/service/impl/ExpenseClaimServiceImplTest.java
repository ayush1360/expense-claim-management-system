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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseClaimServiceImplTest {

    @Mock
    private ExpenseClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseClaimServiceImpl claimService;

    private User employee;
    private User manager;
    private ExpenseClaim claim;

    @BeforeEach
    void setUp() {

        employee = User.builder()
                .id(1L)
                .fullName("Test Employee")
                .email("employee@test.com")
                .role(Role.EMPLOYEE)
                .active(true)
                .build();

        manager = User.builder()
                .id(2L)
                .fullName("Test Manager")
                .email("manager@test.com")
                .role(Role.MANAGER)
                .active(true)
                .build();

        claim = ExpenseClaim.builder()
                .id(1L)
                .title("Office Travel")
                .amount(new BigDecimal("1500.00"))
                .category("Travel")
                .expenseDate(LocalDateTime.now())
                .status(ClaimStatus.PENDING)
                .employee(employee)
                .build();
    }

    private void authenticate(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        email,
                        null,
                        java.util.Collections.emptyList()
                )
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private ExpenseClaimRequest createRequest() {

        ExpenseClaimRequest request = new ExpenseClaimRequest();

        request.setTitle("Updated Travel");
        request.setAmount(new BigDecimal("2000.00"));
        request.setCategory("Travel");
        request.setExpenseDate(LocalDateTime.now());
        request.setDescription("Business travel");

        return request;
    }

    // TEST 1: Create claim

    @Test
    void shouldCreateClaimWithPendingStatus() {

        authenticate(employee.getEmail());

        when(userRepository.findByEmail(employee.getEmail()))
                .thenReturn(Optional.of(employee));

        when(claimRepository.save(any(ExpenseClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaimResponse response =
                claimService.createClaim(createRequest());

        assertNotNull(response);
        assertEquals("Updated Travel", response.getTitle());
        assertEquals(ClaimStatus.PENDING, response.getStatus());
        assertEquals("Test Employee", response.getEmployeeName());

        verify(claimRepository).save(any(ExpenseClaim.class));
    }

    // TEST 2: Update pending claim

    @Test
    void shouldUpdatePendingClaim() {

        authenticate(employee.getEmail());

        when(userRepository.findByEmail(employee.getEmail()))
                .thenReturn(Optional.of(employee));

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(ExpenseClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseClaimResponse response =
                claimService.updateClaim(1L, createRequest());

        assertEquals("Updated Travel", response.getTitle());
        assertEquals(new BigDecimal("2000.00"), response.getAmount());

        verify(claimRepository).save(claim);
    }

    // TEST 3: Delete pending claim

    @Test
    void shouldDeletePendingClaim() {

        authenticate(employee.getEmail());

        when(userRepository.findByEmail(employee.getEmail()))
                .thenReturn(Optional.of(employee));

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        claimService.deleteClaim(1L);

        verify(claimRepository).delete(claim);
    }

    // TEST 4: Manager approves claim

    @Test
    void shouldApprovePendingClaim() {

        authenticate(manager.getEmail());

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        when(claimRepository.save(any(ExpenseClaim.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReviewClaimRequest request = new ReviewClaimRequest();
        request.setStatus(ClaimStatus.APPROVED);

        ExpenseClaimResponse response =
                claimService.reviewClaim(1L, request);

        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals("Test Manager", response.getReviewedBy());

        verify(claimRepository).save(claim);
    }

    // TEST 5: Reject without comment

    @Test
    void shouldRejectClaimWithoutComment() {

        authenticate(manager.getEmail());

        when(userRepository.findByEmail(manager.getEmail()))
                .thenReturn(Optional.of(manager));

        when(claimRepository.findById(1L))
                .thenReturn(Optional.of(claim));

        ReviewClaimRequest request = new ReviewClaimRequest();
        request.setStatus(ClaimStatus.REJECTED);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> claimService.reviewClaim(1L, request)
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        assertEquals(
                ClaimStatus.PENDING,
                claim.getStatus()
        );

        verify(claimRepository, never()).save(any());
    }
}
