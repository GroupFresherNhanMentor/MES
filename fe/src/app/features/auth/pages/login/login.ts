import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

interface MockUser {
  id: string; fullName: string; username: string; email: string; role: string; employeeId: string;
}

const MOCK_USERS: MockUser[] = [
  { id: '00000000-0000-0000-0000-000000000001', fullName: 'Admin', username: 'admin', email: 'admin@mes.com', role: 'ADMIN', employeeId: 'EMP001' },
  { id: '00000000-0000-0000-0000-000000000002', fullName: 'Warehouse Manager', username: 'warehouse', email: 'warehouse@mes.com', role: 'WAREHOUSE_MANAGER', employeeId: 'EMP002' },
  { id: '00000000-0000-0000-0000-000000000003', fullName: 'Planner', username: 'planner', email: 'planner@mes.com', role: 'PLANNER', employeeId: 'EMP003' },
  { id: '00000000-0000-0000-0000-000000000004', fullName: 'Operator', username: 'operator', email: 'operator@mes.com', role: 'PRODUCTION_OPERATOR', employeeId: 'EMP004' },
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
  <div class="page">
    <div class="card">
      <div class="logo">MES</div>
      <h2>Manufacturing Execution System</h2>
      <form (ngSubmit)="login()" class="form">
        <mat-form-field appearance="outline" class="field">
          <mat-label>Username</mat-label>
          <input matInput [(ngModel)]="username" name="username" placeholder="admin" required>
        </mat-form-field>
        <mat-form-field appearance="outline" class="field">
          <mat-label>Password</mat-label>
          <input matInput type="password" [(ngModel)]="password" name="password" placeholder="admin" required>
        </mat-form-field>
        @if (error) { <div class="error">{{ error }}</div> }
        <button mat-raised-button class="btn" type="submit">
          <mat-icon>login</mat-icon>
          Sign In
        </button>
      </form>
      <div class="hint">Try: admin/admin, warehouse/warehouse, planner/planner, operator/operator, qc/qc, maintenance/maintenance, manager/manager, auditor/auditor</div>
    </div>
  </div>
  `,
  styles: [`
    .page { height:100vh; display:flex; align-items:center; justify-content:center; background:#0c0a09; }
    .card { text-align:center; padding:40px; background:#292524; border-radius:12px; border:1px solid #44403c; width:400px; }
    .logo { width:64px; height:64px; margin:0 auto 16px; display:flex; align-items:center; justify-content:center; background:#ea580c; border-radius:12px; color:#fff; font-weight:800; font-size:28px; }
    h2 { color:#fafaf9; font-size:18px; margin:0 0 24px; font-weight:500; }
    .form { display:flex; flex-direction:column; gap:16px; }
    .field { width:100%; }
    .btn { padding:10px 32px!important; font-size:15px!important; width:100%; }
    .error { color:#dc2626; font-size:13px; text-align:left; }
    .hint { margin-top:16px; font-size:11px; color:#57534e; text-align:left; line-height:1.4; }
  `]
})
export class Login {
  private router = inject(Router);
  username = '';
  password = '';
  error = '';

  login() {
    if (!this.username || !this.password) {
      this.error = 'Please enter username and password';
      return;
    }
    const user = MOCK_USERS.find(u => u.username === this.username && this.password === this.username);
    if (user) {
      localStorage.setItem('ff_access_token', 'mock-token');
      localStorage.setItem('ff_user', JSON.stringify({ ...user, status: 'ACTIVE' }));
      this.router.navigateByUrl('/dashboard');
    } else {
      this.error = 'Invalid username or password';
    }
  }
}
