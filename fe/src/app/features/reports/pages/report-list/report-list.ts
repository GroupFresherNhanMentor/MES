import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';

interface ReportDto {
  id: string;
  name: string;
  category: string;
  description: string;
  lastRun?: string;
  createdAt: string;
}

@Component({
  selector: 'app-report-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Reports</h1>
      <p class="page-subtitle">Production analytics and operational insights (Admin only)</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Report Name</th>
            <td mat-cell *matCellDef="let r">{{ r.name }}</td>
          </ng-container>
          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Category</th>
            <td mat-cell *matCellDef="let r">
              <span class="ff-badge" [class.ff-badge--active]="r.category === 'Production'" [class.ff-badge--on-hold]="r.category === 'Quality'" [class.ff-badge--completed]="r.category === 'Inventory'">{{ r.category }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let r">{{ r.description }}</td>
          </ng-container>
          <ng-container matColumnDef="lastRun">
            <th mat-header-cell *matHeaderCellDef>Last Run</th>
            <td mat-cell *matCellDef="let r">{{ r.lastRun ? (r.lastRun | date:'shortDate') : 'Never' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class ReportList {
  private api = inject(ApiService);
  items = signal<ReportDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['name', 'category', 'description', 'lastRun'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: ReportDto[]; totalElements: number }>(`/api/reports?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
