import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';

import { ClaimService } from '../../../core/services/claim.service';

import {
  ExpenseClaim,
  ClaimStatus,
  ClaimSearchFilters,
  PageResponse,
  ReviewClaimRequest
} from '../../../shared/models/claim.model';

@Component({
  selector: 'app-manager-claims',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './claims.html',
  styleUrls: ['./claims.css']
})
export class Claims implements OnInit {

  private claimService = inject(ClaimService);
  private cdr = inject(ChangeDetectorRef);

  router = inject(Router);

  claims: ExpenseClaim[] = [];

  loading = false;
  errorMessage = '';
  successMessage = '';

  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;

  search = '';
  selectedStatus: ClaimStatus | '' = '';
  fromDate = '';
  toDate = '';

  selectedClaim: ExpenseClaim | null = null;

  showReviewModal = false;

  reviewStatus: 'APPROVED' | 'REJECTED' = 'APPROVED';
  rejectionComment = '';

  actionLoading = false;
  actionError = '';

  ngOnInit(): void {
    this.loadClaims();
  }

  // Load all employee claims for the manager.
  loadClaims(): void {

    this.loading = true;
    this.errorMessage = '';

    const filters: ClaimSearchFilters = {
      page: this.currentPage,
      size: this.pageSize,
      status: this.selectedStatus,
      search: this.search,
      fromDate: this.fromDate,
      toDate: this.toDate
    };

    this.claimService.getClaims(filters)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({

        next: (response: PageResponse<ExpenseClaim>) => {

          this.claims = response.content ?? [];
          this.totalElements = response.totalElements ?? 0;
          this.totalPages = response.totalPages ?? 0;
          this.currentPage = response.number ?? 0;

          this.cdr.detectChanges();
        },

        error: error => {

          console.error('Manager claims loading error:', error);

          this.claims = [];
          this.totalElements = 0;
          this.totalPages = 0;

          this.errorMessage =
            error.error?.message || 'Unable to load expense claims.';

          this.cdr.detectChanges();
        }
      });
  }

  // Apply search and date/status filters.
  applyFilters(): void {
    this.currentPage = 0;
    this.loadClaims();
  }

  // Clear all filters.
  clearFilters(): void {

    this.search = '';
    this.selectedStatus = '';
    this.fromDate = '';
    this.toDate = '';

    this.currentPage = 0;

    this.loadClaims();
  }

  refreshClaims(): void {
    this.loadClaims();
  }

  // Pagination.
  changePage(page: number): void {

    if (page < 0 || page >= this.totalPages) {
      return;
    }

    this.currentPage = page;
    this.loadClaims();
  }

  // Open review modal for a pending claim.
  openReview(claim: ExpenseClaim): void {

    if (claim.status !== 'PENDING') {
      return;
    }

    this.selectedClaim = claim;
    this.reviewStatus = 'APPROVED';
    this.rejectionComment = '';
    this.actionError = '';

    this.showReviewModal = true;
  }

  closeReview(): void {

    if (this.actionLoading) {
      return;
    }

    this.showReviewModal = false;
    this.selectedClaim = null;
    this.actionError = '';
    this.rejectionComment = '';
  }

  // Approve or reject a claim.
  submitReview(): void {

    if (!this.selectedClaim || this.actionLoading) {
      return;
    }

    if (
      this.reviewStatus === 'REJECTED' &&
      !this.rejectionComment.trim()
    ) {
      this.actionError = 'A rejection comment is mandatory.';
      return;
    }

    this.actionLoading = true;
    this.actionError = '';

    const request: ReviewClaimRequest = {
      status: this.reviewStatus,
      ...(this.reviewStatus === 'REJECTED'
        ? { rejectionComment: this.rejectionComment.trim() }
        : {})
    };

    this.claimService.reviewClaim(
      this.selectedClaim.id,
      request
    )
    .pipe(
      finalize(() => {
        this.actionLoading = false;
        this.cdr.detectChanges();
      })
    )
    .subscribe({

      next: () => {

        this.showReviewModal = false;
        this.selectedClaim = null;

        this.successMessage =
          this.reviewStatus === 'APPROVED'
            ? 'Claim approved successfully.'
            : 'Claim rejected successfully.';

        this.rejectionComment = '';

        this.loadClaims();
      },

      error: error => {

        console.error('Claim review error:', error);

        this.actionError =
          error.error?.message || 'Unable to process this claim.';

        this.cdr.detectChanges();
      }
    });
  }

  // Download employee receipt.
downloadReceipt(claim: ExpenseClaim): void {
  this.claimService.downloadReceipt(claim.id).subscribe({
    next: (blob: Blob) => {
      if (!blob || blob.size === 0) {
        this.errorMessage = 'Receipt file is empty.';
        return;
      }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = url;
      link.download = `receipt-${claim.id}`;
      document.body.appendChild(link);

      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);
    },
    error: (error) => {
      console.error('Receipt download failed:', error);
      this.errorMessage = 'Unable to download receipt. Please try again.';
    }
  });
}

  getStatusClass(status: ClaimStatus): string {
    return status.toLowerCase();
  }

  logout(): void {

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');

    this.router.navigate(['/']);
  }
}
