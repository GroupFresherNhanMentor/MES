import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { StockMovementDto } from '../../../../core/models/stock-movement.model';

@Component({
  selector: 'app-stock-movement-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Stock Movements</h1>
      <p class="page-subtitle">Track material receipts, issues, transfers, and adjustments</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let m">{{ m.movementCode }}</td>
          </ng-container>
          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let m">
              <span class="ff-badge" [class.ff-badge--active]="m.movementType === 'RECEIPT' || m.movementType === 'TRANSFER_IN'" [class.ff-badge--cancelled]="m.movementType === 'ISSUE' || m.movementType === 'TRANSFER_OUT'" [class.ff-badge--on-hold]="m.movementType === 'ADJUSTMENT'">{{ m.movementType }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="product">
            <th mat-header-cell *matHeaderCellDef>Product</th>
            <td mat-cell *matCellDef="let m">{{ m.productName }}</td>
          </ng-container>
          <ng-container matColumnDef="qty">
            <th mat-header-cell *matHeaderCellDef>Quantity</th>
            <td mat-cell *matCellDef="let m">{{ m.quantity }}</td>
          </ng-container>
          <ng-container matColumnDef="from">
            <th mat-header-cell *matHeaderCellDef>From</th>
            <td mat-cell *matCellDef="let m">{{ m.fromWarehouseName || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="to">
            <th mat-header-cell *matHeaderCellDef>To</th>
            <td mat-cell *matCellDef="let m">{{ m.toWarehouseName || '-' }}</td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Date</th>
            <td mat-cell *matCellDef="let m">{{ m.movementDate | date:'shortDate' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class StockMovementList {
  private api = inject(ApiService);
  items = signal<StockMovementDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'type', 'product', 'qty', 'from', 'to', 'date'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: StockMovementDto[]; totalElements: number }>(`/api/stock-movements?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
