import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { AuthService } from '../../../../core/services/auth';

interface MockUser {
  id: string; fullName: string; username: string; email: string; role: string; employeeId: string;
}

const MOCK_USERS: MockUser[] = [
  { id: '00000000-0000-0000-0000-000000000001', fullName: 'Admin', username: 'admin', email: 'admin@mes.com', role: 'ADMIN', employeeId: 'EMP001' },
  { id: '00000000-0000-0000-0000-000000000002', fullName: 'Warehouse Manager', username: 'warehouse', email: 'warehouse@mes.com', role: 'WAREHOUSE_MANAGER', employeeId: 'EMP002' },
  { id: '00000000-0000-0000-0000-000000000003', fullName: 'Planner', username: 'planner', email: 'planner@mes.com', role: 'PLANNER', employeeId: 'EMP003' },
  { id: '00000000-0000-0000-0000-000000000004', fullName: 'Operator', username: 'operator', email: 'operator@mes.com', role: 'OPERATOR', employeeId: 'EMP004' },
  { id: '00000000-0000-0000-0000-000000000005', fullName: 'QC Inspector', username: 'qc', email: 'qc@mes.com', role: 'QC_INSPECTOR', employeeId: 'EMP005' },
  { id: '00000000-0000-0000-0000-000000000006', fullName: 'Maintenance Engineer', username: 'maintenance', email: 'maintenance@mes.com', role: 'MAINTENANCE_ENGINEER', employeeId: 'EMP006' },
  { id: '00000000-0000-0000-0000-000000000007', fullName: 'Factory Manager', username: 'manager', email: 'manager@mes.com', role: 'FACTORY_MANAGER', employeeId: 'EMP007' },
  { id: '00000000-0000-0000-0000-000000000008', fullName: 'Auditor', username: 'auditor', email: 'auditor@mes.com', role: 'AUDITOR', employeeId: 'EMP008' },
];

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule],
  template: `
  <div class="page ff-fade-in">
    <div class="glow-bg"></div>
    <div class="card">
      <div class="logo-box">
        <div class="logo">MES</div>
      </div>
      <h2>Manufacturing Execution System</h2>
      <p class="sub-heading">Operations & Factory Management Platform</p>

      <form (ngSubmit)="login()" class="form">
        <mat-form-field appearance="outline" class="field">
          <mat-label>Username</mat-label>
          <input matInput [(ngModel)]="username" name="username" placeholder="Enter username" required>
          <mat-icon matPrefix class="text-text-muted">person</mat-icon>
        </mat-form-field>

        <mat-form-field appearance="outline" class="field">
          <mat-label>Password</mat-label>
          <input matInput type="password" [(ngModel)]="password" name="password" placeholder="Enter password" required>
          <mat-icon matPrefix class="text-text-muted">lock</mat-icon>
        </mat-form-field>

        @if (error) {
          <div class="error-msg">
            <mat-icon class="!w-4 !h-4 !text-base">error_outline</mat-icon>
            <span>{{ error }}</span>
          </div>
        }

        <button mat-raised-button class="ff-btn-primary !w-full !h-11 !text-base" type="submit">
          <mat-icon class="!w-5 !h-5 text-xl">login</mat-icon> Sign In
        </button>
      </form>

      <div class="quick-login">
        <span class="quick-label">Quick Demo Access:</span>
        <div class="role-pills">
          @for (u of mockUsers; track u.username) {
            <button type="button" class="role-pill" (click)="quickSelect(u.username)">
              {{ u.fullName }}
            </button>
          }
        </div>
      </div>
    </div>
  </div>
  `,
  styles: [`
    .page {
      position: relative; height: 100vh; display: flex; align-items: center; justify-content: center;
      background: #0c0a09; overflow: hidden;
    }
    .glow-bg {
      position: absolute; width: 600px; height: 600px; border-radius: 50%;
      background: radial-gradient(circle, rgba(234, 88, 12, 0.15) 0%, rgba(12, 10, 9, 0) 70%);
      top: 50%; left: 50%; transform: translate(-50%, -50%); pointer-events: none;
    }
    .card {
      position: relative; z-index: 10; max-width: 440px; width: 92vw; min-width: 280px; padding: 40px; text-align: center;
      background: rgba(41, 37, 36, 0.85); backdrop-filter: blur(16px); -webkit-backdrop-filter: blur(16px);
      border: 1px solid #44403c; border-radius: 16px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
    }
    .logo-box { display: flex; justify-content: center; margin-bottom: 16px; }
    .logo {
      width: 68px; height: 68px; display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #ea580c 0%, #c2410c 100%);
      border-radius: 16px; color: #fff; font-weight: 800; font-size: 28px; letter-spacing: 1px;
      box-shadow: 0 8px 20px rgba(234, 88, 12, 0.35);
    }
    h2 { color: #fafaf9; font-size: 20px; font-weight: 700; margin: 0 0 4px; letter-spacing: 0.3px; }
    .sub-heading { color: #a8a29e; font-size: 13px; margin: 0 0 28px; }
    .form { display: flex; flex-direction: column; gap: 14px; }
    .field { width: 100%; }
    .error-msg {
      display: flex; align-items: center; gap: 6px; color: #f87171; background: rgba(248, 113, 113, 0.1);
      padding: 8px 12px; border-radius: 6px; font-size: 13px; text-align: left;
    }
    .quick-login { margin-top: 24px; padding-top: 20px; border-top: 1px solid #44403c; text-align: left; }
    .quick-label { font-size: 11px; font-weight: 600; text-transform: uppercase; color: #78716c; letter-spacing: 0.5px; }
    .role-pills { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 8px; }
    .role-pill {
      background: #1c1917; border: 1px solid #44403c; color: #a8a29e; font-size: 11px;
      padding: 4px 10px; border-radius: 12px; cursor: pointer; transition: all 0.15s ease;
    }
    .role-pill:hover { background: rgba(234, 88, 12, 0.2); border-color: #ea580c; color: #fafaf9; }
  `]
})
export class Login {
  private router = inject(Router);
  private authService = inject(AuthService);
  username = 'admin';
  password = 'Admin@1234';
  error = '';
  mockUsers = MOCK_USERS;

  quickSelect(usr: string) {
    this.username = usr;
    this.password = 'Admin@1234';
  }

  login() {
    if (!this.username || !this.password) {
      this.error = 'Please enter username and password';
      return;
    }
    this.error = '';
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: () => {
        void this.router.navigateByUrl('/dashboard');
      },
      error: (err) => {
        this.error = err?.error?.message || 'Invalid username or password';
      },
    });
  }
}
