import { Component, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { StockBalanceDto } from '../../../../core/models/stock-balance.model';

@Component({
  selector: 'app-stock-balance-list',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Stock Balances</h1>
      <p class="page-subtitle">View current inventory levels across warehouses</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="product">
            <th mat-header-cell *matHeaderCellDef>Product</th>
            <td mat-cell *matCellDef="let s">{{ s.productName }}</td>
          </ng-container>
          <ng-container matColumnDef="warehouse">
            <th mat-header-cell *matHeaderCellDef>Warehouse</th>
            <td mat-cell *matCellDef="let s">{{ s.warehouseName }}</td>
          </ng-container>
          <ng-container matColumnDef="location">
            <th mat-header-cell *matHeaderCellDef>Location</th>
            <td mat-cell *matCellDef="let s">{{ s.locationCode }}</td>
          </ng-container>
          <ng-container matColumnDef="quantity">
            <th mat-header-cell *matHeaderCellDef>On Hand</th>
            <td mat-cell *matCellDef="let s">{{ s.quantity }}</td>
          </ng-container>
          <ng-container matColumnDef="available">
            <th mat-header-cell *matHeaderCellDef>Available</th>
            <td mat-cell *matCellDef="let s">{{ s.availableQuantity }}</td>
          </ng-container>
          <ng-container matColumnDef="uom">
            <th mat-header-cell *matHeaderCellDef>UoM</th>
            <td mat-cell *matCellDef="let s">{{ s.unitOfMeasure }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class StockBalanceList {
  private api = inject(ApiService);
  items = signal<StockBalanceDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['product', 'warehouse', 'location', 'quantity', 'available', 'uom'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: StockBalanceDto[]; totalElements: number }>(`/api/stock-balances?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
