import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
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
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { StockBalanceDto } from '../../../../core/models/stock-balance.model';
import type { PageResponse } from '../../../../core/models/api.model';
import type { ProductDto } from '../../../../core/models/product.model';
import { StockInFormComponent } from '../../components/stock-in-form/stock-in-form';
import { StockAdjustmentFormComponent } from '../../components/stock-adjustment-form/stock-adjustment-form';

interface LookupEntry {
  id: string;
  name: string;
  code?: string;
}

@Component({
  selector: 'app-stock-inventory',
  standalone: true,
  imports: [
    CommonModule, DatePipe, DecimalPipe, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatCardModule, MatDialogModule, MatTooltipModule, MatProgressBarModule,
  ],
  template: `
    <div class="page-container ff-fade-in p-6 space-y-6">

      <!-- Metric KPI Summary Banner -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="bg-white border border-slate-200 rounded-xl p-4 flex items-center gap-4 shadow-sm">
          <div class="p-3 bg-blue-50 text-blue-600 rounded-lg">
            <mat-icon class="!text-2xl">inventory_2</mat-icon>
          </div>
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wider">Total Stock Lines</p>
            <h3 class="text-xl font-bold text-slate-800">{{ total() }}</h3>
          </div>
        </div>

        <div class="bg-white border border-slate-200 rounded-xl p-4 flex items-center gap-4 shadow-sm">
          <div class="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
            <mat-icon class="!text-2xl">pin</mat-icon>
          </div>
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wider">Total Quantity</p>
            <h3 class="text-xl font-bold text-slate-800">{{ totalQuantity() | number:'1.0-2' }}</h3>
          </div>
        </div>

        <div class="bg-white border border-slate-200 rounded-xl p-4 flex items-center gap-4 shadow-sm">
          <div class="p-3 bg-teal-50 text-teal-600 rounded-lg">
            <mat-icon class="!text-2xl">check_circle</mat-icon>
          </div>
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wider">Available Stock</p>
            <h3 class="text-xl font-bold text-slate-800">{{ availableCount() }}</h3>
          </div>
        </div>

        <div class="bg-white border border-slate-200 rounded-xl p-4 flex items-center gap-4 shadow-sm">
          <div class="p-3 bg-amber-50 text-amber-600 rounded-lg">
            <mat-icon class="!text-2xl">lock</mat-icon>
          </div>
          <div>
            <p class="text-xs font-medium text-slate-500 uppercase tracking-wider">Reserved / Hold</p>
            <h3 class="text-xl font-bold text-slate-800">{{ reservedOrHoldCount() }}</h3>
          </div>
        </div>
      </div>

      <!-- Main Stock Balances Card -->
      <mat-card class="shadow-sm border border-slate-200 rounded-xl overflow-hidden">
        @if (loading()) {
          <mat-progress-bar mode="indeterminate"></mat-progress-bar>
        }

        <!-- Header Toolbar -->
        <mat-card-header class="!flex !items-center !justify-between !p-5 !border-b !border-slate-100">
          <div>
            <mat-card-title class="!text-lg !font-bold !text-slate-800">Inventory Stock Balances</mat-card-title>
            <p class="text-xs text-slate-500 mt-1">Real-time balances tracked across warehouses, locations, products, and lots.</p>
          </div>

          <div class="flex items-center gap-3">
            @if (canWarehouseManage) {
              <button mat-raised-button class="ff-btn-primary" (click)="openStockIn()">
                <mat-icon>add</mat-icon> Stock In
              </button>
            }
            <button mat-icon-button (click)="load()" matTooltip="Refresh list" class="!text-slate-500">
              <mat-icon>refresh</mat-icon>
            </button>
          </div>
        </mat-card-header>

        <!-- Filters Section -->
        <div class="p-4 bg-slate-50 border-b border-slate-200 flex items-center gap-3 flex-wrap">
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-60">
            <mat-label>Product</mat-label>
            <mat-select [(ngModel)]="filterProductId" (selectionChange)="onFilter()">
              <mat-option value="">All Products</mat-option>
              @for (p of products(); track p.id) {
                <mat-option [value]="p.id">{{ p.name }} ({{ p.code }})</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-52">
            <mat-label>Warehouse</mat-label>
            <mat-select [(ngModel)]="filterWarehouseId" (selectionChange)="onFilter()">
              <mat-option value="">All Warehouses</mat-option>
              @for (w of warehouses(); track w.id) {
                <mat-option [value]="w.id">{{ w.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-48">
            <mat-label>Stock Status</mat-label>
            <mat-select [(ngModel)]="filterStatusId" (selectionChange)="onFilter()">
              <mat-option value="">All Statuses</mat-option>
              @for (s of stockStatuses(); track s.id) {
                <mat-option [value]="s.id">{{ s.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
            <mat-label>Lot Number</mat-label>
            <input matInput [(ngModel)]="filterLotNumber" (keyup.enter)="onFilter()" placeholder="Search lot...">
          </mat-form-field>

          <button mat-icon-button (click)="resetFilters()" matTooltip="Clear all filters" class="!text-slate-500">
            <mat-icon>filter_list_off</mat-icon>
          </button>
        </div>

        <!-- Data Table -->
        <mat-card-content class="!p-0">
          <table mat-table [dataSource]="items()" class="w-full">

            <!-- Product Column -->
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Product</th>
              <td mat-cell *matCellDef="let s">
                <div class="flex flex-col py-1">
                  <span class="font-semibold text-slate-800 text-sm">{{ s.product?.name || '—' }}</span>
                  <div class="flex items-center gap-2 mt-0.5">
                    <span class="font-mono text-xs bg-slate-100 text-slate-600 px-1.5 py-0.5 rounded border border-slate-200">
                      {{ s.product?.code || '—' }}
                    </span>
                    @if (s.product?.unitName || s.product?.unit?.name) {
                      <span class="text-xs text-slate-500">
                        ({{ s.product?.unitName || s.product?.unit?.name }})
                      </span>
                    }
                  </div>
                </div>
              </td>
            </ng-container>

            <!-- Lot Number Column -->
            <ng-container matColumnDef="lot">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Lot Number</th>
              <td mat-cell *matCellDef="let s">
                @if (s.lot?.lotNumber) {
                  <span class="font-mono text-sm px-2 py-0.5 bg-blue-50 text-blue-700 rounded border border-blue-200">
                    {{ s.lot.lotNumber }}
                  </span>
                } @else {
                  <span class="text-slate-400">—</span>
                }
              </td>
            </ng-container>

            <!-- Warehouse Column -->
            <ng-container matColumnDef="warehouse">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Warehouse</th>
              <td mat-cell *matCellDef="let s">
                <div class="flex flex-col">
                  <span class="text-slate-800 text-sm font-medium">{{ s.warehouse?.name || '—' }}</span>
                  <span class="font-mono text-xs text-slate-500">{{ s.warehouse?.code }}</span>
                </div>
              </td>
            </ng-container>

            <!-- Location Column -->
            <ng-container matColumnDef="location">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Location</th>
              <td mat-cell *matCellDef="let s" class="font-mono text-sm text-slate-600">
                {{ s.location?.name || s.location?.code || '—' }}
              </td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="stockStatus">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Status</th>
              <td mat-cell *matCellDef="let s">
                @if (s.stockStatus) {
                  <span class="ff-badge" [class]="statusBadgeClass(s.stockStatus.name)">
                    {{ s.stockStatus.name }}
                  </span>
                } @else {
                  <span class="text-slate-400">—</span>
                }
              </td>
            </ng-container>

            <!-- Quantity Column -->
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700 !text-right">Quantity</th>
              <td mat-cell *matCellDef="let s" class="!text-right">
                <span class="font-mono font-bold text-base text-slate-900">
                  {{ s.quantity | number:'1.0-4' }}
                </span>
              </td>
            </ng-container>

            <!-- Last Updated Column -->
            <ng-container matColumnDef="updatedAt">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700">Last Updated</th>
              <td mat-cell *matCellDef="let s" class="text-slate-500 text-xs font-mono">
                {{ s.updatedAt | date:'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef class="!font-semibold !text-slate-700 !text-center">Action</th>
              <td mat-cell *matCellDef="let s" class="!text-center">
                @if (canWarehouseManage) {
                  <button mat-stroked-button color="primary" class="!text-xs" (click)="openStockAdjustment(s)" matTooltip="Adjust this stock balance">
                    <mat-icon class="!text-base">tune</mat-icon> Adjust
                  </button>
                } @else {
                  <span class="text-slate-400 text-xs">—</span>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="hover:bg-slate-50/80 transition-colors"></tr>

            <!-- Empty Data Row -->
            <tr class="mat-row" *matNoDataRow>
              <td [attr.colspan]="displayedColumns.length" class="!py-16 text-center">
                <div class="flex flex-col items-center justify-center text-slate-400 gap-2">
                  <mat-icon class="!text-5xl opacity-40">inventory_2</mat-icon>
                  <span class="text-sm font-medium">No stock balances found matching criteria</span>
                </div>
              </td>
            </tr>
          </table>

          <!-- Paginator -->
          <mat-paginator
            [length]="total()"
            [pageSize]="size()"
            [pageIndex]="page()"
            [pageSizeOptions]="[10, 20, 50, 100]"
            (page)="onPage($event)"
            class="!border-t !border-slate-200">
          </mat-paginator>
        </mat-card-content>
      </mat-card>

    </div>
  `,
})
export class StockInventoryComponent {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private dialog = inject(MatDialog);

  items = signal<StockBalanceDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);

  filterProductId = '';
  filterWarehouseId = '';
  filterStatusId = '';
  filterLotNumber = '';

  products = signal<ProductDto[]>([]);
  warehouses = signal<LookupEntry[]>([]);
  stockStatuses = signal<LookupEntry[]>([]);

  displayedColumns = ['product', 'lot', 'warehouse', 'location', 'stockStatus', 'quantity', 'updatedAt', 'actions'];

  get canWarehouseManage(): boolean {
    return this.auth.hasAnyRole('WAREHOUSE_MANAGER');
  }

  totalQuantity = computed(() => {
    return this.items().reduce((acc, item) => acc + (item.quantity || 0), 0);
  });

  availableCount = computed(() => {
    return this.items().filter(i => i.stockStatus?.name?.toUpperCase() === 'AVAILABLE').length;
  });

  reservedOrHoldCount = computed(() => {
    return this.items().filter(i => {
      const name = i.stockStatus?.name?.toUpperCase() || '';
      return name === 'RESERVED' || name === 'QUARANTINE' || name === 'ON_HOLD';
    }).length;
  });

  constructor() {
    this.loadLookups();
    this.load();
  }

  private loadLookups() {
    this.api.get<PageResponse<ProductDto>>(`${API.products.base}?size=100`).subscribe(r => {
      if (r.success && r.data) this.products.set(r.data.items || []);
    });
    this.api.get<PageResponse<LookupEntry>>(`${API.warehouses.base}?size=100`).subscribe(r => {
      if (r.success && r.data) this.warehouses.set(r.data.items || []);
    });
    this.api.get<PageResponse<LookupEntry>>('/api/stock-statuses?size=50').subscribe(r => {
      if (r.success && r.data) this.stockStatuses.set(r.data.items || []);
    });
  }

  load() {
    this.loading.set(true);
    const params = new URLSearchParams({
      page: String(this.page()),
      size: String(this.size()),
    });
    if (this.filterProductId) params.set('productId', this.filterProductId);
    if (this.filterWarehouseId) params.set('warehouseId', this.filterWarehouseId);
    if (this.filterStatusId) params.set('stockStatusId', this.filterStatusId);
    if (this.filterLotNumber) params.set('lotNumber', this.filterLotNumber.trim());

    this.api.get<PageResponse<StockBalanceDto>>(`${API.stockBalances.base}?${params}`).subscribe({
      next: r => {
        if (r.success && r.data) {
          this.items.set(r.data.items || []);
          this.total.set(r.data.totalElements || 0);
        }
      },
      complete: () => this.loading.set(false),
      error: () => this.loading.set(false),
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
    this.filterProductId = '';
    this.filterWarehouseId = '';
    this.filterStatusId = '';
    this.filterLotNumber = '';
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
