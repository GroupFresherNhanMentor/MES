import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { WorkOrderDto } from '../../../../core/models/work-order.model';

@Component({
  selector: 'app-work-order-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Work Orders</h1>
      <p class="page-subtitle">Create, track, and manage production work orders</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Order Code</th>
            <td mat-cell *matCellDef="let w">{{ w.orderCode }}</td>
          </ng-container>
          <ng-container matColumnDef="product">
            <th mat-header-cell *matHeaderCellDef>Product</th>
            <td mat-cell *matCellDef="let w">{{ w.productName }}</td>
          </ng-container>
          <ng-container matColumnDef="quantity">
            <th mat-header-cell *matHeaderCellDef>Qty</th>
            <td mat-cell *matCellDef="let w">{{ w.completedQuantity }}/{{ w.quantity }}</td>
          </ng-container>
          <ng-container matColumnDef="line">
            <th mat-header-cell *matHeaderCellDef>Line</th>
            <td mat-cell *matCellDef="let w">{{ w.productionLineName }}</td>
          </ng-container>
          <ng-container matColumnDef="due">
            <th mat-header-cell *matHeaderCellDef>Due</th>
            <td mat-cell *matCellDef="let w">{{ w.dueDate ? (w.dueDate | date:'shortDate') : '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let w">
              <span class="ff-badge" [class.ff-badge--active]="w.status === 'IN_PROGRESS'" [class.ff-badge--completed]="w.status === 'COMPLETED'" [class.ff-badge--pending]="w.status === 'DRAFT' || w.status === 'RELEASED'" [class.ff-badge--on-hold]="w.status === 'PAUSED'" [class.ff-badge--cancelled]="w.status === 'CANCELLED'">{{ w.status }}</span>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class WorkOrderList {
  private api = inject(ApiService);
  items = signal<WorkOrderDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'product', 'quantity', 'line', 'due', 'status'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: WorkOrderDto[]; totalElements: number }>(`/api/work-orders?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
