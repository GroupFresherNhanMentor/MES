import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';

@Component({
  selector: 'app-warehouse-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Warehouses</h1>
      <p class="page-subtitle">Manage warehouses and storage locations</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let w">{{ w.warehouseCode }}</td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let w">{{ w.warehouseName }}</td>
          </ng-container>
          <ng-container matColumnDef="city">
            <th mat-header-cell *matHeaderCellDef>City</th>
            <td mat-cell *matCellDef="let w">{{ w.city }}</td>
          </ng-container>
          <ng-container matColumnDef="capacity">
            <th mat-header-cell *matHeaderCellDef>Capacity</th>
            <td mat-cell *matCellDef="let w">{{ w.usedCapacity }}/{{ w.capacity }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let w">
              <span class="ff-badge" [class.ff-badge--active]="w.status === 'ACTIVE'" [class.ff-badge--in-progress]="w.status !== 'ACTIVE'">{{ w.status }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="created">
            <th mat-header-cell *matHeaderCellDef>Created</th>
            <td mat-cell *matCellDef="let w">{{ w.createdAt | date:'shortDate' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class WarehouseList {
  private api = inject(ApiService);
  items = signal<WarehouseDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'name', 'city', 'capacity', 'status', 'created'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: WarehouseDto[]; totalElements: number }>(`/api/warehouses?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
