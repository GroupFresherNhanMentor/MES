import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { StockMovementDto } from '../../../../core/models/stock-movement.model';
import type { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-stock-movement-list',
  imports: [
    DatePipe, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatCardModule, MatPaginatorModule, MatProgressBarModule,
  ],
  template: `
    <div class="page-container ff-fade-in">
      <mat-card>
        @if (loading()) {
          <mat-progress-bar mode="indeterminate" class="!rounded-t-lg"></mat-progress-bar>
        }

        <mat-card-header class="!flex !items-center !justify-between !pb-4">
          <mat-card-title class="!text-xl !font-semibold !text-text-primary">Stock Movements</mat-card-title>
          <div class="flex items-center gap-3">
            <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-56">
              <mat-label>Reference No.</mat-label>
              <input matInput [(ngModel)]="filterRef" (keyup.enter)="onFilter()" placeholder="Search reference…">
              <mat-icon matSuffix class="text-text-muted">search</mat-icon>
            </mat-form-field>
          </div>
        </mat-card-header>

        <mat-card-content>
          <table mat-table [dataSource]="items()">

            <ng-container matColumnDef="movementType">
              <th mat-header-cell *matHeaderCellDef>Type</th>
              <td mat-cell *matCellDef="let m">
                @if (m.movementType) {
                  <span class="ff-badge" [class]="typeBadgeClass(m.movementType.name)">
                    {{ m.movementType.name }}
                  </span>
                } @else { <span class="text-text-muted">—</span> }
              </td>
            </ng-container>

            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Product</th>
              <td mat-cell *matCellDef="let m">
                <div class="flex flex-col">
                  <span class="font-medium text-text-primary">{{ m.product?.name || '—' }}</span>
                  <span class="font-mono text-xs text-text-muted">{{ m.product?.code }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="lot">
              <th mat-header-cell *matHeaderCellDef>Lot</th>
              <td mat-cell *matCellDef="let m" class="font-mono text-sm text-text-secondary">
                {{ m.lot?.lotNumber || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="from">
              <th mat-header-cell *matHeaderCellDef>From</th>
              <td mat-cell *matCellDef="let m">
                @if (m.fromWarehouse) {
                  <div class="flex flex-col">
                    <span class="text-text-primary">{{ m.fromWarehouse.name }}</span>
                    <span class="font-mono text-xs text-text-muted">{{ m.fromLocation?.code }}</span>
                  </div>
                } @else { <span class="text-text-muted">—</span> }
              </td>
            </ng-container>

            <ng-container matColumnDef="to">
              <th mat-header-cell *matHeaderCellDef>To</th>
              <td mat-cell *matCellDef="let m">
                @if (m.toWarehouse) {
                  <div class="flex flex-col">
                    <span class="text-text-primary">{{ m.toWarehouse.name }}</span>
                    <span class="font-mono text-xs text-text-muted">{{ m.toLocation?.code }}</span>
                  </div>
                } @else { <span class="text-text-muted">—</span> }
              </td>
            </ng-container>

            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef class="!text-right">Qty</th>
              <td mat-cell *matCellDef="let m" class="!text-right font-mono font-semibold text-text-primary">
                {{ m.quantity }}
              </td>
            </ng-container>

            <ng-container matColumnDef="referenceNo">
              <th mat-header-cell *matHeaderCellDef>Reference</th>
              <td mat-cell *matCellDef="let m" class="font-mono text-sm text-text-secondary">
                {{ m.referenceNo || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Date</th>
              <td mat-cell *matCellDef="let m" class="text-text-secondary text-sm">
                {{ m.createdAt | date:'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="cursor-default"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td [attr.colspan]="displayedColumns.length" class="!py-16">
                <div class="flex flex-col items-center justify-center text-text-muted gap-2">
                  <mat-icon class="!text-5xl opacity-30">swap_horiz</mat-icon>
                  <span class="text-sm">No movements found</span>
                </div>
              </td>
            </tr>
          </table>

          <mat-paginator
            [length]="total()"
            [pageSize]="size()"
            [pageIndex]="page()"
            [pageSizeOptions]="[10, 20, 50]"
            (page)="onPage($event)">
          </mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
})
export class StockMovementList {
  private api = inject(ApiService);

  items = signal<StockMovementDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);
  filterRef = '';

  displayedColumns = ['movementType', 'product', 'lot', 'from', 'to', 'quantity', 'referenceNo', 'createdAt'];

  constructor() { this.load(); }

  load() {
    this.loading.set(true);
    const params = new URLSearchParams({ page: String(this.page()), size: String(this.size()) });
    if (this.filterRef) params.set('referenceNo', this.filterRef);

    this.api.get<PageResponse<StockMovementDto>>(`${API.stockMovements.base}?${params}`).subscribe({
      next: r => {
        if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
      },
      complete: () => this.loading.set(false),
    });
  }

  onFilter() { this.page.set(0); this.load(); }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }

  typeBadgeClass(name: string): string {
    const n = (name || '').toUpperCase();
    if (n.includes('IN') || n === 'PURCHASE_IN' || n === 'TRANSFER_IN') return 'ff-badge--active';
    if (n.includes('OUT') || n === 'TRANSFER_OUT') return 'ff-badge--cancelled';
    if (n === 'ADJUSTMENT') return 'ff-badge--pending';
    return 'ff-badge--idle';
  }
}
