import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import { JoinWithPipe } from '../../../../shared/pipes/join-with.pipe';
import type { StockAdjustmentApprovalDto } from '../../../../core/models/stock-adjustment.model';
import type { PageResponse } from '../../../../core/models/api.model';

interface LookupItem { id: string; name: string; code?: string; }

@Component({
  selector: 'app-stock-adjustment-list',
  standalone: true,
  imports: [
    DatePipe, DecimalPipe, FormsModule, JoinWithPipe,
    MatTableModule, MatButtonModule, MatIconModule, MatCardModule,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatTooltipModule,
    MatPaginatorModule, MatProgressBarModule, MatSnackBarModule,
  ],
  templateUrl: './stock-adjustment-list.html',
  styleUrl: './stock-adjustment-list.css',
})
export class StockAdjustmentList {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  get canApproveAdjustments(): boolean {
    return this.auth.hasAnyRole('FACTORY_MANAGER', 'ADMIN');
  }

  items = signal<StockAdjustmentApprovalDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);
  actionInProgress = signal(false);

  filterProductId = '';
  filterWarehouseId = '';
  filterLocationId = '';

  products = signal<LookupItem[]>([]);
  warehouses = signal<LookupItem[]>([]);
  locations = signal<LookupItem[]>([]);

  displayedColumns = ['product', 'warehouse', 'adjustment', 'reason', 'referenceNo', 'requestedBy', 'createdAt', 'actions'];

  constructor() {
    this.loadLookups();
    this.load();
  }

  private loadLookups() {
    this.api.get<PageResponse<LookupItem>>(`${API.products.base}?size=100`).subscribe(r => {
      if (r.success && r.data) this.products.set(r.data.items || []);
    });
    this.api.get<PageResponse<LookupItem>>(`${API.warehouses.base}?size=100`).subscribe(r => {
      if (r.success && r.data) this.warehouses.set(r.data.items || []);
    });
  }

  onWarehouseChange(warehouseId: string) {
    this.filterLocationId = '';
    this.locations.set([]);
    if (warehouseId) {
      this.api.get<PageResponse<LookupItem>>(`${API.locations.base(warehouseId)}?size=100`).subscribe(r => {
        if (r.success && r.data) this.locations.set(r.data.items || []);
      });
    }
    this.onFilter();
  }

  load() {
    this.loading.set(true);
    const params = new URLSearchParams({ page: String(this.page()), size: String(this.size()) });
    if (this.filterProductId) params.set('productId', this.filterProductId);
    if (this.filterWarehouseId) params.set('warehouseId', this.filterWarehouseId);
    if (this.filterLocationId) params.set('locationId', this.filterLocationId);

    this.api.get<PageResponse<StockAdjustmentApprovalDto>>(`${API.stockAdjustments.pending}?${params}`).subscribe({
      next: r => {
        if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
      },
      complete: () => this.loading.set(false),
    });
  }

  onFilter() {
    this.page.set(0);
    this.load();
  }

  resetFilters() {
    this.filterProductId = '';
    this.filterWarehouseId = '';
    this.filterLocationId = '';
    this.locations.set([]);
    this.onFilter();
  }

  onPage(e: PageEvent) {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }

  approve(id: string) {
    this.actionInProgress.set(true);
    const key = crypto.randomUUID();
    this.api.post<any>(API.stockAdjustments.approve(id), {}, { idempotencyKey: key }).subscribe({
      next: (r: any) => {
        const message = r?.message || 'Adjustment approved successfully';
        this.snackBar.open(message, 'OK', { duration: 3000 });
        this.load();
      },
      error: (e: any) => {
        const errorMessage = e?.error?.message || e?.message || 'Failed to approve adjustment.';
        this.snackBar.open(errorMessage, 'Dismiss', { duration: 4000 });
        this.actionInProgress.set(false);
      },
      complete: () => this.actionInProgress.set(false),
    });
  }

  reject(id: string) {
    this.actionInProgress.set(true);
    const key = crypto.randomUUID();
    this.api.post<any>(API.stockAdjustments.reject(id), {}, { idempotencyKey: key }).subscribe({
      next: (r: any) => {
        const message = r?.message || 'Adjustment rejected and removed';
        this.snackBar.open(message, 'OK', { duration: 3000 });
        this.load();
      },
      error: (e: any) => {
        const errorMessage = e?.error?.message || e?.message || 'Failed to reject adjustment.';
        this.snackBar.open(errorMessage, 'Dismiss', { duration: 4000 });
        this.actionInProgress.set(false);
      },
      complete: () => this.actionInProgress.set(false),
    });
  }
}
