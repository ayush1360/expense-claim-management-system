
import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

interface EmployeeDashboard {
  totalClaims: number;
  pendingClaims: number;
  approvedClaims: number;
  rejectedClaims: number;
  totalClaimedAmount: number;
  pendingAmount: number;
  approvedAmount: number;
  rejectedAmount: number;
}

@Component({
  selector: 'app-employee-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  router = inject(Router);

  loading = false;
  errorMessage = '';

  dashboard: EmployeeDashboard = {
    totalClaims: 0,
    pendingClaims: 0,
    approvedClaims: 0,
    rejectedClaims: 0,
    totalClaimedAmount: 0,
    pendingAmount: 0,
    approvedAmount: 0,
    rejectedAmount: 0
  };

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http.get<EmployeeDashboard>(
      'http://localhost:8080/api/dashboard/employee'
    )
    .pipe(finalize(() => {
      this.loading = false;
      this.cdr.detectChanges();
    }))
    .subscribe({
      next: response => {
        this.dashboard = response;
        this.cdr.detectChanges();
      },
      error: error => {
        console.error('Employee dashboard error:', error);
        this.errorMessage =
          error.error?.message || 'Unable to load dashboard.';
      }
    });
  }

  goToClaims(): void {
    this.router.navigate(['/employee/claims']);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    this.router.navigate(['/']);
  }
}
