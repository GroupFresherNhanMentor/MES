import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { StockBalanceDto } from '../../../../core/models/stock-balance.model';
import type { PageResponse } from '../../../../core/models/api.model';
import { StockInFormComponent } from '../../components/stock-in-form/stock-in-form';
import { StockAdjustmentFormComponent } from '../../components/stock-adjustment-form/stock-adjustment-form';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-stock-balance-list',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatCardModule, MatDialogModule, MatTooltipModule, MatProgressBarModule,
  ],
  template: `
    <div class="page-container ff-fade-in">
      <mat-card>
        @if (loading()) {
          <mat-progress-bar mode="indeterminate" class="!rounded-t-lg"></mat-progress-bar>
        }

        <!-- Header -->
        <mat-card-header class="!flex !items-center !justify-between !pb-4">
          <mat-card-title class="!text-xl !font-semibold !text-text-primary">Stock Balances</mat-card-title>
          <div class="toolbar flex items-center gap-3 flex-wrap">
            <button mat-raised-button class="ff-btn-primary" (click)="openStockIn()">
              <mat-icon>add</mat-icon> Stock In
            </button>

            <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-52">
              <mat-label>Warehouse</mat-label>
              <mat-select [(ngModel)]="filterWarehouseId" (selectionChange)="onFilter()">
                <mat-option value="">All</mat-option>
                @for (w of warehouses(); track w.id) {
                  <mat-option [value]="w.id">{{ w.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
              <mat-label>Stock Status</mat-label>
              <mat-select [(ngModel)]="filterStatusId" (selectionChange)="onFilter()">
                <mat-option value="">All</mat-option>
                @for (s of stockStatuses(); track s.id) {
                  <mat-option [value]="s.id">{{ s.name }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <button mat-icon-button (click)="resetFilters()" matTooltip="Clear filters" class="!text-text-muted">
              <mat-icon>filter_list_off</mat-icon>
            </button>
          </div>
        </mat-card-header>

        <!-- Table -->
        <mat-card-content>
          <table mat-table [dataSource]="items()">

            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Product</th>
              <td mat-cell *matCellDef="let s">
                <div class="flex flex-col">
                  <span class="font-medium text-text-primary">{{ s.product?.name || '—' }}</span>
                  <span class="font-mono text-xs text-text-muted">{{ s.product?.code }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="lot">
              <th mat-header-cell *matHeaderCellDef>Lot</th>
              <td mat-cell *matCellDef="let s" class="font-mono text-sm text-text-secondary">
                {{ s.lot?.lotNumber || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="warehouse">
              <th mat-header-cell *matHeaderCellDef>Warehouse</th>
              <td mat-cell *matCellDef="let s">
                <div class="flex flex-col">
                  <span class="text-text-primary">{{ s.warehouse?.name || '—' }}</span>
                  <span class="font-mono text-xs text-text-muted">{{ s.warehouse?.code }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef>Location</th>
              <td mat-cell *matCellDef="let s" class="font-mono text-sm text-text-secondary">
                {{ s.location?.code || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="stockStatus">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let s">
                @if (s.stockStatus) {
                  <span class="ff-badge" [class]="statusBadgeClass(s.stockStatus.name)">
                    {{ s.stockStatus.name }}
                  </span>
                } @else { <span class="text-text-muted">—</span> }
              </td>
            </ng-container>

            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef class="!text-right">Quantity</th>
              <td mat-cell *matCellDef="let s" class="!text-right">
                <span class="font-mono font-semibold text-text-primary">
                  {{ s.quantity | number:'1.0-4' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="updatedAt">
              <th mat-header-cell *matHeaderCellDef>Last Updated</th>
              <td mat-cell *matCellDef="let s" class="text-text-secondary text-sm">
                {{ s.updatedAt | date:'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef class="!text-center">Action</th>
              <td mat-cell *matCellDef="let s" class="!text-center">
                <button mat-stroked-button color="primary" class="!text-xs" (click)="openStockAdjustment(s)" matTooltip="Adjust this stock balance">
                  <mat-icon class="!text-base">tune</mat-icon> Adjust
                </button>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="cursor-default"></tr>

            <!-- Empty state -->
            <tr class="mat-row" *matNoDataRow>
              <td [attr.colspan]="displayedColumns.length" class="!py-16">
                <div class="flex flex-col items-center justify-center text-text-muted gap-2">
                  <mat-icon class="!text-5xl opacity-30">inventory_2</mat-icon>
                  <span class="text-sm">No stock balances found</span>
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
export class StockBalanceList {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);

  items = signal<StockBalanceDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);

  filterWarehouseId = '';
  filterStatusId = '';

  warehouses = signal<LookupEntry[]>([]);
  stockStatuses = signal<LookupEntry[]>([]);

  displayedColumns = ['product', 'lot', 'warehouse', 'location', 'stockStatus', 'quantity', 'updatedAt', 'actions'];

  constructor() {
    this.loadLookups();
    this.load();
  }

  private loadLookups() {
    this.api.get<PageResponse<LookupEntry>>('/api/warehouses?page=0&size=100').subscribe(r => {
      if (r.success) this.warehouses.set(r.data.items);
    });
    this.api.get<PageResponse<LookupEntry>>('/api/stock-statuses?page=0&size=50').subscribe(r => {
      if (r.success) this.stockStatuses.set(r.data.items);
    });
  }

  load() {
    this.loading.set(true);
    const params = new URLSearchParams({
      page: String(this.page()),
      size: String(this.size()),
    });
    if (this.filterWarehouseId) params.set('warehouseId', this.filterWarehouseId);
    if (this.filterStatusId)   params.set('stockStatusId', this.filterStatusId);

    this.api.get<PageResponse<StockBalanceDto>>(`${API.stockBalances.base}?${params}`).subscribe({
      next: r => {
        if (r.success) {
          this.items.set(r.data.items);
          this.total.set(r.data.totalElements);
        }
      },
      complete: () => this.loading.set(false),
    });
  }

  onFilter() {
    this.page.set(0);
    this.load();
  }

  onPage(e: PageEvent) {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }

  resetFilters() {
    this.filterWarehouseId = '';
    this.filterStatusId = '';
    this.onFilter();
  }

  openStockIn() {
    this.dialog.open(StockInFormComponent, {
      width: '600px',
      panelClass: 'ff-dialog-panel',
    }).afterClosed().subscribe(res => {
      if (res) this.load();
    });
  }

  openStockAdjustment(stockBalance: StockBalanceDto) {
    this.dialog.open(StockAdjustmentFormComponent, {
      width: '500px',
      panelClass: 'ff-dialog-panel',
      data: { stockBalance }
    }).afterClosed().subscribe(res => {
      if (res) this.load();
    });
  }

  statusBadgeClass(name: string): string {
    const n = (name || '').toUpperCase();
    if (n === 'AVAILABLE') return 'ff-badge--available';
    if (n === 'RESERVED')  return 'ff-badge--onhold';
    if (n === 'QUARANTINE') return 'ff-badge--pending';
    if (n === 'SCRAPPED')  return 'ff-badge--cancelled';
    return 'ff-badge--idle';
  }
}
