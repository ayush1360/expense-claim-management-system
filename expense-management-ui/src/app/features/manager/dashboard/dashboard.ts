
import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

interface ManagerDashboardData {
  totalClaims: number;
  pendingClaims: number;
  approvedClaims: number;
  rejectedClaims: number;
  categorySummaries: any[];
  monthlySummaries: any[];
}

@Component({
  selector: 'app-manager-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  router = inject(Router);

  dashboard: ManagerDashboardData | null = null;

  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    console.log('Manager dashboard initialized');
    this.loadDashboard();
  }

  loadDashboard(): void {

    this.loading = true;
    this.errorMessage = '';

    this.http.get<ManagerDashboardData>(
      'http://localhost:8080/api/dashboard/manager'
    )
    .pipe(
      finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();

        console.log('Dashboard loading completed');
      })
    )
    .subscribe({

      next: (response) => {

        console.log('Dashboard API response:', response);

        this.dashboard = response;
        this.loading = false;

        this.cdr.detectChanges();
      },

      error: (error) => {

        console.error('Dashboard API failed:', error);

        this.errorMessage =
          error.error?.message ||
          'Unable to load dashboard data.';

        this.loading = false;

        this.cdr.detectChanges();
      }

    });
  }

  logout(): void {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
