import {
  Component,
  OnInit,
  ChangeDetectorRef,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import {
  finalize,
  timeout
} from 'rxjs';

import {
  ClaimService,
  ClaimRequest
} from '../../../core/services/claim.service';

import {
  ExpenseClaim,
  ClaimStatus,
  PageResponse
} from '../../../shared/models/claim.model';

@Component({
  selector: 'app-employee-claims',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './claims.html',
  styleUrl: './claims.css'
})
export class EmployeeClaims implements OnInit {

  private claimService = inject(ClaimService);
  private cdr = inject(ChangeDetectorRef);

  router = inject(Router);

  claims: ExpenseClaim[] = [];

  loading = false;
  saving = false;
  deletingId: number | null = null;
  downloadingId: number | null = null;

  errorMessage = '';
  successMessage = '';

  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 10;

  selectedStatus: ClaimStatus | '' = '';

  showModal = false;
  editingClaim: ExpenseClaim | null = null;

  form: ClaimRequest = this.emptyForm();
  selectedFile: File | null = null;

  ngOnInit(): void {
    this.loadClaims();
  }

  // EMPTY FORM

  emptyForm(): ClaimRequest {
    return {
      title: '',
      amount: 0,
      category: '',
      expenseDate: '',
      description: ''
    };
  }

  // DASHBOARD COUNTS

  get pendingCount(): number {
    return this.claims.filter(
      c => c.status === 'PENDING'
    ).length;
  }

  get approvedCount(): number {
    return this.claims.filter(
      c => c.status === 'APPROVED'
    ).length;
  }

  get rejectedCount(): number {
    return this.claims.filter(
      c => c.status === 'REJECTED'
    ).length;
  }

  // LOAD CLAIMS

  loadClaims(): void {

    this.loading = true;
    this.errorMessage = '';

    this.cdr.markForCheck();

    this.claimService.getMyClaims(
      this.currentPage,
      this.pageSize,
      this.selectedStatus || undefined
    )
    .pipe(
      timeout(15000),
      finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      })
    )
    .subscribe({

      next: (response: PageResponse<ExpenseClaim>) => {

        console.log('Employee claims response:', response);

        this.claims = response.content ?? [];
        this.totalElements = response.totalElements ?? 0;
        this.totalPages = response.totalPages ?? 0;
        this.currentPage = response.number ?? 0;

        this.cdr.markForCheck();
      },

      error: error => {

        console.error('Employee claims error:', error);

        this.errorMessage =
          error.name === 'TimeoutError'
            ? 'Server is taking too long to respond. Please try again.'
            : error.error?.message || 'Unable to load your claims.';

        this.cdr.markForCheck();
      }

    });
  }

  // FILTERS

  applyStatus(): void {
    this.currentPage = 0;
    this.loadClaims();
  }

  clearStatus(): void {
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadClaims();
  }

  // CREATE CLAIM

  openCreate(): void {

    if (this.saving) return;

    this.editingClaim = null;
    this.form = this.emptyForm();
    this.selectedFile = null;

    this.errorMessage = '';
    this.successMessage = '';

    this.showModal = true;

    this.cdr.markForCheck();
  }

  // EDIT CLAIM

  openEdit(claim: ExpenseClaim): void {

    if (claim.status !== 'PENDING' || this.saving) {
      return;
    }

    this.editingClaim = claim;

    this.form = {
      title: claim.title,
      amount: claim.amount,
      category: claim.category,
      expenseDate: claim.expenseDate?.substring(0, 10) ?? '',
      description: claim.description ?? ''
    };

    this.selectedFile = null;

    this.errorMessage = '';
    this.successMessage = '';

    this.showModal = true;

    this.cdr.markForCheck();
  }

  // CLOSE MODAL

  closeModal(): void {

    if (this.saving) return;

    this.showModal = false;
    this.editingClaim = null;
    this.selectedFile = null;
    this.errorMessage = '';

    this.cdr.markForCheck();
  }

  // RECEIPT VALIDATION

  onFileSelected(event: Event): void {

    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;

    this.selectedFile = null;
    this.errorMessage = '';

    if (!file) return;

    const allowedTypes = [
      'image/jpeg',
      'image/png',
      'application/pdf'
    ];

    const maxSize = 5 * 1024 * 1024;

    if (!allowedTypes.includes(file.type)) {

      this.errorMessage =
        'Only JPG, PNG and PDF files are allowed.';

      input.value = '';
      this.cdr.markForCheck();
      return;
    }

    if (file.size > maxSize) {

      this.errorMessage =
        'Receipt must be smaller than 5 MB.';

      input.value = '';
      this.cdr.markForCheck();
      return;
    }

    this.selectedFile = file;
    this.cdr.markForCheck();
  }

  // CREATE / UPDATE CLAIM

  submitClaim(): void {

    if (this.saving) return;

    if (
      !this.form.title.trim() ||
      !this.form.category ||
      !this.form.expenseDate ||
      !this.form.amount ||
      this.form.amount <= 0
    ) {

      this.errorMessage =
        'Please complete all required fields with a valid amount.';

      this.cdr.markForCheck();
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.cdr.markForCheck();

    const request: ClaimRequest = {
      ...this.form,
      title: this.form.title.trim(),
      description: this.form.description.trim(),
      expenseDate: `${this.form.expenseDate}T00:00:00`
    };

    const isNew = !this.editingClaim;

    // Preserve the selected file during the API operation.
    const receiptFile = this.selectedFile;

    const operation = this.editingClaim
      ? this.claimService.updateClaim(
          this.editingClaim.id,
          request
        )
      : this.claimService.createClaim(request);

    operation
      .pipe(timeout(15000))
      .subscribe({

        next: claim => {

          console.log('Claim saved successfully:', claim);

          // No receipt selected.
          if (!receiptFile) {

            this.saving = false;

            this.finishSave(
              isNew,
              'success'
            );

            return;
          }

          // Upload receipt after claim is saved.
          this.claimService.uploadReceipt(
            claim.id,
            receiptFile
          )
          .pipe(
            timeout(30000),
            finalize(() => {
              this.saving = false;
              this.cdr.markForCheck();
            })
          )
          .subscribe({

            next: () => {

              console.log('Receipt uploaded successfully');

              this.finishSave(
                isNew,
                'success'
              );
            },

            error: error => {

              console.error(
                'Receipt upload error:',
                error
              );

              // Claim was saved, but receipt upload failed.
              this.finishSave(
                isNew,
                'receipt-error'
              );
            }

          });
        },

        error: error => {

          console.error('Claim save error:', error);

          this.saving = false;

          this.errorMessage =
            error.name === 'TimeoutError'
              ? 'Request timed out. Please check your connection and try again.'
              : error.error?.message || 'Unable to save claim.';

          this.cdr.markForCheck();
        }

      });
  }

  // AFTER SUCCESSFUL SAVE

  private finishSave(
    isNew: boolean,
    result: 'success' | 'receipt-error'
  ): void {

    this.showModal = false;

    this.editingClaim = null;
    this.selectedFile = null;

    if (result === 'receipt-error') {

      this.successMessage =
        'Claim saved successfully, but receipt upload failed.';

    } else {

      this.successMessage = isNew
        ? 'Expense claim submitted successfully.'
        : 'Expense claim updated successfully.';

    }

    // Reset filters so the new claim is visible.
    this.currentPage = 0;
    this.selectedStatus = '';

    this.cdr.markForCheck();

    // Immediately reload claims from backend.
    this.loadClaims();
  }

  // DELETE CLAIM

  deleteClaim(claim: ExpenseClaim): void {

    if (
      claim.status !== 'PENDING' ||
      this.deletingId !== null
    ) {
      return;
    }

    if (
      !confirm(
        `Delete "${claim.title}"? This action cannot be undone.`
      )
    ) {
      return;
    }

    this.deletingId = claim.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.cdr.markForCheck();

    this.claimService.deleteClaim(claim.id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.deletingId = null;
          this.cdr.markForCheck();
        })
      )
      .subscribe({

        next: () => {

          this.successMessage =
            'Expense claim deleted successfully.';

          if (
            this.claims.length === 1 &&
            this.currentPage > 0
          ) {
            this.currentPage--;
          }

          this.loadClaims();
        },

        error: error => {

          console.error('Delete claim error:', error);

          this.errorMessage =
            error.error?.message || 'Unable to delete claim.';

          this.cdr.markForCheck();
        }

      });
  }

  // DOWNLOAD RECEIPT

  downloadReceipt(claim: ExpenseClaim): void {

    if (this.downloadingId !== null) return;

    this.downloadingId = claim.id;
    this.errorMessage = '';

    this.cdr.markForCheck();

    this.claimService.downloadReceipt(claim.id)
      .pipe(
        timeout(15000),
        finalize(() => {
          this.downloadingId = null;
          this.cdr.markForCheck();
        })
      )
      .subscribe({

        next: blob => {

          const url = URL.createObjectURL(blob);
          const link = document.createElement('a');

          link.href = url;
          link.download = `claim-${claim.id}-receipt`;

          document.body.appendChild(link);
          link.click();
          link.remove();

          URL.revokeObjectURL(url);
        },

        error: error => {

          console.error('Receipt download error:', error);

          this.errorMessage =
            error.error?.message ||
            'Receipt could not be downloaded. It may not exist yet.';

          this.cdr.markForCheck();
        }

      });
  }

  // PAGINATION

  changePage(page: number): void {

    if (
      page < 0 ||
      page >= this.totalPages ||
      this.loading
    ) {
      return;
    }

    this.currentPage = page;
    this.loadClaims();
  }

  // STATUS CSS CLASS

  getStatusClass(status: ClaimStatus): string {
    return status.toLowerCase();
  }

  // LOGOUT

  logout(): void {

    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');

    this.router.navigate(['/']);
  }
}
