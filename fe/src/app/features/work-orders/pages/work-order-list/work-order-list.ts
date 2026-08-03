import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';

import { API } from '../../../../configs/api-endpoints';
import type { PageResponse } from '../../../../core/models/api.model';
import type { ProductDto } from '../../../../core/models/product.model';
import type { WorkOrderDto } from '../../../../core/models/work-order.model';
import type { WorkOrderLookupDto } from '../../../../core/models/work-order.model';
import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';
import { WorkOrderCreateDialog } from '../../components/work-order-create-dialog/work-order-create-dialog';

@Component({
  selector: 'app-work-order-list',
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTableModule,
    StatusBadge,
  ],
  template: `
    <div class="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="m-0 text-2xl font-semibold text-text-primary">Work Orders</h1>
        <p class="mb-0 mt-1 text-sm text-text-secondary">Track planned production and execute available lifecycle actions.</p>
      </div>
    </div>

    <mat-card>
      <mat-card-header class="!flex !items-center !justify-between !pb-4">
        <mat-card-title class="!text-xl !font-semibold !text-text-primary">Production queue</mat-card-title>
        <div class="flex flex-wrap items-center gap-3">
          @if (canCreate()) {
            <button mat-raised-button class="ff-btn-primary" (click)="openCreate()"><mat-icon>add</mat-icon> Create work order</button>
          }
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-64">
            <mat-label>Search order code</mat-label>
            <input matInput [(ngModel)]="code" (keyup.enter)="search()" placeholder="WO-2026-0001">
            <mat-icon matSuffix class="text-text-muted">search</mat-icon>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-52">
            <mat-label>Status</mat-label>
            <mat-select [(ngModel)]="statusId" (selectionChange)="search()">
              <mat-option value="">All statuses</mat-option>
              @for (status of statuses(); track status.id) { <mat-option [value]="status.id">{{ status.name }}</mat-option> }
            </mat-select>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-64">
            <mat-label>Finished product</mat-label>
            <mat-select [(ngModel)]="finishedProductId" (selectionChange)="search()">
              <mat-option value="">All products</mat-option>
              @for (product of products(); track product.id) {
                <mat-option [value]="product.id">{{ product.code }} - {{ product.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <button mat-stroked-button (click)="reset()">Reset</button>
        </div>
      </mat-card-header>

      @if (loading()) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      <mat-card-content>
        @if (error()) {
          <div class="mb-4 rounded-md border border-error/40 bg-error/10 px-4 py-3 text-sm text-error">{{ error() }}</div>
        }
        @if (!loading() && items().length === 0) {
          <div class="flex flex-col items-center justify-center py-16 text-text-muted">
            <mat-icon class="mb-2 text-5xl opacity-40">assignment</mat-icon>
            <span class="text-sm">No work orders found</span>
          </div>
        } @else {
          <table mat-table [dataSource]="items()" class="w-full" aria-label="Work orders">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>Code</th>
              <td mat-cell *matCellDef="let workOrder" class="font-medium text-text-primary">{{ workOrder.code }}</td>
            </ng-container>
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Finished product</th>
              <td mat-cell *matCellDef="let workOrder">{{ productLabel(workOrder.finishedProductId) }}</td>
            </ng-container>
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>Planned qty</th>
              <td mat-cell *matCellDef="let workOrder">{{ workOrder.plannedQuantity }}</td>
            </ng-container>
            <ng-container matColumnDef="schedule">
              <th mat-header-cell *matHeaderCellDef>Planned schedule</th>
              <td mat-cell *matCellDef="let workOrder">{{ workOrder.plannedStartDate ? (workOrder.plannedStartDate | date:'mediumDate') : '-' }} to {{ workOrder.plannedEndDate ? (workOrder.plannedEndDate | date:'mediumDate') : '-' }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status reference</th>
              <td mat-cell *matCellDef="let workOrder"><app-status-badge [value]="statusLabel(workOrder.workOrderStatusId)" [label]="statusLabel(workOrder.workOrderStatusId)"></app-status-badge></td>
            </ng-container>
            <ng-container matColumnDef="created">
              <th mat-header-cell *matHeaderCellDef>Created</th>
              <td mat-cell *matCellDef="let workOrder">{{ workOrder.createdAt | date:'medium' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef class="!w-20">Actions</th>
              <td mat-cell *matCellDef="let workOrder">
                <button mat-icon-button class="ff-action-btn-view" (click)="openDetail(workOrder.id)" aria-label="View work order details"><mat-icon>visibility</mat-icon></button>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="cursor-pointer" (click)="openDetail(row.id)"></tr>
          </table>
        }
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" [pageSizeOptions]="[10, 20, 50, 100]" (page)="onPage($event)"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class WorkOrderList {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  readonly items = signal<WorkOrderDto[]>([]);
  readonly products = signal<ProductDto[]>([]);
  readonly statuses = signal<WorkOrderLookupDto[]>([]);
  readonly initialStatuses = signal<WorkOrderLookupDto[]>([]);
  readonly priorities = signal<WorkOrderLookupDto[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);

  code = '';
  finishedProductId = '';
  statusId = '';
  readonly displayedColumns = ['code', 'product', 'quantity', 'schedule', 'status', 'created', 'actions'];

  constructor() {
    this.loadProducts();
    this.loadLookups();
    this.load();
  }

  search(): void {
    this.page.set(0);
    this.load();
  }

  reset(): void {
    this.code = '';
    this.finishedProductId = '';
    this.statusId = '';
    this.search();
  }

  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.load();
  }

  openDetail(id: string): void {
    void this.router.navigate(['/work-orders', id]);
  }

  canCreate(): boolean {
    return this.auth.hasAnyRole('PLANNER') && this.initialStatuses().length > 0;
  }

  openCreate(): void {
    this.dialog.open(WorkOrderCreateDialog, {
      data: { products: this.products(), priorities: this.priorities(), statuses: this.initialStatuses() },
      panelClass: 'ff-dialog-panel',
    }).afterClosed().subscribe((created) => {
      if (created) this.load();
    });
  }

  productLabel(id: string): string {
    const product = this.products().find((item) => item.id === id);
    return product ? `${product.code} - ${product.name}` : this.shortId(id);
  }

  shortId(id?: string | null): string {
    return id ? `${id.slice(0, 8)}...` : '-';
  }

  statusLabel(id: string): string {
    return this.statuses().find((status) => status.id === id)?.name ?? this.shortId(id);
  }

  private loadProducts(): void {
    this.api.get<PageResponse<ProductDto>>(API.products.base, { page: 0, size: 100 }).subscribe({
      next: (response) => {
        if (response.success) this.products.set(response.data.items);
      },
    });
  }

  private loadLookups(): void {
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.statuses).subscribe({
      next: (response) => { if (response.success) this.statuses.set(response.data); },
    });
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.statuses, { isInitial: true }).subscribe({
      next: (response) => { if (response.success) this.initialStatuses.set(response.data); },
    });
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.priorities).subscribe({
      next: (response) => { if (response.success) this.priorities.set(response.data); },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.get<PageResponse<WorkOrderDto>>(API.workOrders.base, {
      page: this.page(),
      size: this.size(),
      code: this.code,
      finishedProductId: this.finishedProductId,
      statusId: this.statusId,
    }).subscribe({
      next: (response) => {
        if (response.success) {
          this.items.set(response.data.items);
          this.total.set(response.data.totalElements);
        }
        this.loading.set(false);
      },
      error: (response) => {
        this.error.set(response?.error?.message ?? 'Unable to load work orders.');
        this.loading.set(false);
      },
    });
  }
}
