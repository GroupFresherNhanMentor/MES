import { Component, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { StockAdjustmentApprovalDto } from '../../../../core/models/stock-adjustment.model';
import type { PageResponse } from '../../../../core/models/api.model';

@Component({
  selector: 'app-stock-adjustment-list',
  imports: [
    DatePipe, DecimalPipe,
    MatTableModule, MatButtonModule, MatIconModule, MatCardModule,
    MatPaginatorModule, MatProgressBarModule, MatTooltipModule, MatSnackBarModule,
  ],
  template: `
    <div class="page-container ff-fade-in">
      <mat-card>
        @if (loading()) {
          <mat-progress-bar mode="indeterminate" class="!rounded-t-lg"></mat-progress-bar>
        }

        <mat-card-header class="!flex !items-center !justify-between !pb-4">
          <div class="flex flex-col gap-1">
            <mat-card-title class="!text-xl !font-semibold !text-text-primary">Pending Adjustments</mat-card-title>
            <p class="text-sm text-text-secondary">
              Stock adjustments exceeding the auto-approval threshold — review and approve or reject each one.
            </p>
          </div>
          @if (total() > 0) {
            <span class="ff-badge ff-badge--pending text-sm px-3 py-1">
              {{ total() }} pending
            </span>
          }
        </mat-card-header>

        <mat-card-content>
          <table mat-table [dataSource]="items()">

            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>Product</th>
              <td mat-cell *matCellDef="let a">
                <div class="flex flex-col">
                  <span class="font-medium text-text-primary">{{ a.product?.name || '—' }}</span>
                  <span class="font-mono text-xs text-text-muted">{{ a.product?.code }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="warehouse">
              <th mat-header-cell *matHeaderCellDef>Warehouse / Location</th>
              <td mat-cell *matCellDef="let a">
                <div class="flex flex-col">
                  <span class="text-text-primary">{{ a.warehouse?.name || '—' }}</span>
                  <span class="font-mono text-xs text-text-muted">{{ a.location?.code }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="adjustment">
              <th mat-header-cell *matHeaderCellDef class="!text-right">Adjustment</th>
              <td mat-cell *matCellDef="let a" class="!text-right">
                <span class="font-mono font-semibold"
                      [class.text-success]="a.quantityAdjustment > 0"
                      [class.text-error]="a.quantityAdjustment < 0">
                  {{ a.quantityAdjustment > 0 ? '+' : '' }}{{ a.quantityAdjustment | number:'1.0-4' }}
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="reason">
              <th mat-header-cell *matHeaderCellDef>Reason</th>
              <td mat-cell *matCellDef="let a" class="text-text-secondary text-sm max-w-48 truncate"
                  [matTooltip]="a.reason || ''">
                {{ a.reason || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="referenceNo">
              <th mat-header-cell *matHeaderCellDef>Reference</th>
              <td mat-cell *matCellDef="let a" class="font-mono text-sm text-text-secondary">
                {{ a.referenceNo || '—' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="requestedBy">
              <th mat-header-cell *matHeaderCellDef>Requested By</th>
              <td mat-cell *matCellDef="let a">
                <div class="flex flex-col">
                  <span class="text-text-primary text-sm">{{ a.creator?.fullName || '—' }}</span>
                  <span class="text-text-muted text-xs">{{ a.creator?.username }}</span>
                </div>
              </td>
            </ng-container>

            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>Submitted</th>
              <td mat-cell *matCellDef="let a" class="text-text-secondary text-sm">
                {{ a.createdAt | date:'dd/MM/yyyy HH:mm' }}
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef class="!text-center">Actions</th>
              <td mat-cell *matCellDef="let a" class="!text-center">
                <div class="flex items-center justify-center gap-2">
                  <button mat-stroked-button
                          class="!border-success !text-success"
                          [disabled]="actionInProgress()"
                          (click)="approve(a.id)"
                          matTooltip="Approve this adjustment">
                    <mat-icon class="!text-base">check_circle</mat-icon>
                    Approve
                  </button>
                  <button mat-stroked-button
                          class="!border-error !text-error"
                          [disabled]="actionInProgress()"
                          (click)="reject(a.id)"
                          matTooltip="Reject and remove this adjustment">
                    <mat-icon class="!text-base">cancel</mat-icon>
                    Reject
                  </button>
                </div>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="cursor-default"></tr>

            <tr class="mat-row" *matNoDataRow>
              <td [attr.colspan]="displayedColumns.length" class="!py-16">
                <div class="flex flex-col items-center justify-center text-text-muted gap-2">
                  <mat-icon class="!text-5xl opacity-30">task_alt</mat-icon>
                  <span class="text-sm">No pending adjustments — all clear</span>
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
export class StockAdjustmentList {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  items = signal<StockAdjustmentApprovalDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);
  actionInProgress = signal(false);

  displayedColumns = ['product', 'warehouse', 'adjustment', 'reason', 'referenceNo', 'requestedBy', 'createdAt', 'actions'];

  constructor() { this.load(); }

  load() {
    this.loading.set(true);
    const params = new URLSearchParams({ page: String(this.page()), size: String(this.size()) });
    this.api.get<PageResponse<StockAdjustmentApprovalDto>>(`${API.stockAdjustments.pending}?${params}`).subscribe({
      next: r => {
        if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
      },
      complete: () => this.loading.set(false),
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }

  approve(id: string) {
    this.actionInProgress.set(true);
    this.api.post<unknown>(API.stockAdjustments.approve(id), {}).subscribe({
      next: () => {
        this.snackBar.open('Adjustment approved and stock updated.', 'OK', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.snackBar.open('Failed to approve adjustment.', 'Dismiss', { duration: 4000 });
        this.actionInProgress.set(false);
      },
      complete: () => this.actionInProgress.set(false),
    });
  }

  reject(id: string) {
    this.actionInProgress.set(true);
    this.api.post<unknown>(API.stockAdjustments.reject(id), {}).subscribe({
      next: () => {
        this.snackBar.open('Adjustment rejected and removed.', 'OK', { duration: 3000 });
        this.load();
      },
      error: () => {
        this.snackBar.open('Failed to reject adjustment.', 'Dismiss', { duration: 4000 });
        this.actionInProgress.set(false);
      },
      complete: () => this.actionInProgress.set(false),
    });
  }
}
