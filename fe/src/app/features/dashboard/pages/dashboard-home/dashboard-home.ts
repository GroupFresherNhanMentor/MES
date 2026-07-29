import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard-home',
  imports: [MatCardModule, MatIconModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Dashboard</h1>
      <p class="page-subtitle">Manufacturing operations overview</p>
    </div>

    <div class="dashboard-grid">
      <mat-card class="ff-card stat-card">
        <mat-icon class="stat-icon">precision_manufacturing</mat-icon>
        <div class="stat-value">--</div>
        <div class="stat-label">Active Orders</div>
      </mat-card>
      <mat-card class="ff-card stat-card">
        <mat-icon class="stat-icon">inventory_2</mat-icon>
        <div class="stat-value">--</div>
        <div class="stat-label">Products</div>
      </mat-card>
      <mat-card class="ff-card stat-card">
        <mat-icon class="stat-icon">settings</mat-icon>
        <div class="stat-value">--</div>
        <div class="stat-label">Machines</div>
      </mat-card>
      <mat-card class="ff-card stat-card">
        <mat-icon class="stat-icon">fact_check</mat-icon>
        <div class="stat-value">--</div>
        <div class="stat-label">Inspections Today</div>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; margin-top: 24px; }
    .stat-card { padding: 24px; display: flex; flex-direction: column; align-items: center; text-align: center; gap: 8px; }
    .stat-icon { font-size: 32px; width: 32px; height: 32px; color: #d97706; }
    .stat-value { font-size: 32px; font-weight: 700; color: #fafaf9; }
    .stat-label { font-size: 13px; color: #a8a29e; text-transform: uppercase; letter-spacing: 0.5px; }
  `],
})
export class DashboardHome {}
