
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  ExpenseClaim,
  PageResponse,
  ClaimSearchFilters,
  ReviewClaimRequest,
  ClaimStatus
} from '../../shared/models/claim.model';

export interface ClaimRequest {
  title: string;
  amount: number;
  category: string;
  expenseDate: string;
  description: string;
}

@Injectable({ providedIn: 'root' })
export class ClaimService {
  private http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/claims';

  // Manager: search all claims
  getClaims(filters: ClaimSearchFilters): Observable<PageResponse<ExpenseClaim>> {
    let params = new HttpParams()
      .set('page', String(filters.page ?? 0))
      .set('size', String(filters.size ?? 10));

    if (filters.status) params = params.set('status', filters.status);
    if (filters.employeeId) params = params.set('employeeId', String(filters.employeeId));
    if (filters.fromDate) params = params.set('fromDate', `${filters.fromDate}T00:00:00`);
    if (filters.toDate) params = params.set('toDate', `${filters.toDate}T23:59:59`);
    if (filters.search?.trim()) params = params.set('search', filters.search.trim());

    return this.http.get<PageResponse<ExpenseClaim>>(
      `${this.apiUrl}/search`, { params }
    );
  }

  // Employee: own claims
  getMyClaims(
    page = 0,
    size = 10,
    status?: ClaimStatus
  ): Observable<PageResponse<ExpenseClaim>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));

    if (status) params = params.set('status', status);

    return this.http.get<PageResponse<ExpenseClaim>>(
      `${this.apiUrl}/my`, { params }
    );
  }

  createClaim(request: ClaimRequest): Observable<ExpenseClaim> {
    return this.http.post<ExpenseClaim>(this.apiUrl, request);
  }

  updateClaim(id: number, request: ClaimRequest): Observable<ExpenseClaim> {
    return this.http.put<ExpenseClaim>(`${this.apiUrl}/${id}`, request);
  }

  deleteClaim(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Receipt upload
  uploadReceipt(id: number, file: File): Observable<unknown> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post(
      `${this.apiUrl}/${id}/receipt`, formData
    );
  }

  downloadReceipt(id: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${id}/receipt`,
      { responseType: 'blob' }
    );
  }

  getClaimById(id: number): Observable<ExpenseClaim> {
    return this.http.get<ExpenseClaim>(`${this.apiUrl}/${id}`);
  }

  reviewClaim(
    id: number,
    request: ReviewClaimRequest
  ): Observable<ExpenseClaim> {
    return this.http.patch<ExpenseClaim>(
      `${this.apiUrl}/${id}/review`, request
    );
  }
}
