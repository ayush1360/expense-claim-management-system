
export type ClaimStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ExpenseClaim {
  id: number;
  title: string;
  amount: number;
  category: string;
  expenseDate: string;
  description: string;
  status: ClaimStatus;
  employeeName: string;
  employeeEmail: string;
  reviewedBy?: string;
  rejectionComment?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ClaimSearchFilters {
  status?: ClaimStatus | '';
  employeeId?: number | null;
  fromDate?: string;
  toDate?: string;
  search?: string;
  page?: number;
  size?: number;
}

export interface ReviewClaimRequest {
  status: 'APPROVED' | 'REJECTED';
  rejectionComment?: string;
}
