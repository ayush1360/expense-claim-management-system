import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';

export const routes: Routes = [
  {
    path: '',
    component: Login
  },
  {
    path: 'manager',
    loadComponent: () =>
      import('./features/manager/dashboard/dashboard')
        .then(m => m.Dashboard)
  },
  {
    path: 'manager/claims',
    loadComponent: () =>
      import('./features/manager/claims/claims')
        .then(m => m.Claims)
  },
  {
    path: 'employee',
    loadComponent: () =>
      import('./features/employee/dashboard/dashboard')
        .then(m => m.Dashboard)
  },
  {
    path: 'employee/claims',
    loadComponent: () =>
      import('./features/employee/claims/claims')
        .then(m => m.EmployeeClaims)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
