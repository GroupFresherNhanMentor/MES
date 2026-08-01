import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ReportService, StockMovementHistoryRow } from '../../services/report.service';

@Component({
  selector: 'app-stock-movement-history',
  standalone: true,
  imports: [
    FormsModule,
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
  ],
  template: `
    <div class="flex flex-wrap items-center justify-between gap-4 mb-4">
      <div class="flex items-center gap-3">
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
          <mat-label>From Date</mat-label>
          <input matInput type="date" [(ngModel)]="fromDate" (change)="load()">
        </mat-form-field>
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
          <mat-label>To Date</mat-label>
          <input matInput type="date" [(ngModel)]="toDate" (change)="load()">
        </mat-form-field>
        <button mat-raised-button class="ff-btn-primary" (click)="load()">
          <mat-icon>filter_alt</mat-icon> Filter
        </button>
      </div>
    </div>

    @if (items().length > 0) {
      <table mat-table [dataSource]="items()" class="w-full">
        <ng-container matColumnDef="movementTime">
          <th mat-header-cell *matHeaderCellDef>Time</th>
          <td mat-cell *matCellDef="let r" class="text-xs text-text-secondary">{{ r.movementTime | date:'short' }}</td>
        </ng-container>
        <ng-container matColumnDef="movementType">
          <th mat-header-cell *matHeaderCellDef>Movement Type</th>
          <td mat-cell *matCellDef="let r">
            <span class="ff-badge ff-badge--active">{{ r.movementType }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="productCode">
          <th mat-header-cell *matHeaderCellDef>Product</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">
            {{ r.productCode }}
            <span class="text-xs text-text-muted block">{{ r.productName }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="lotNumber">
          <th mat-header-cell *matHeaderCellDef>Lot Number</th>
          <td mat-cell *matCellDef="let r" class="text-xs">{{ r.lotNumber || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="from">
          <th mat-header-cell *matHeaderCellDef>From</th>
          <td mat-cell *matCellDef="let r" class="text-xs">
            @if (r.fromWarehouse) {
              {{ r.fromWarehouse }} / {{ r.fromLocation || '-' }}
            } @else {
              -
            }
          </td>
        </ng-container>
        <ng-container matColumnDef="to">
          <th mat-header-cell *matHeaderCellDef>To</th>
          <td mat-cell *matCellDef="let r" class="text-xs">
            @if (r.toWarehouse) {
              {{ r.toWarehouse }} / {{ r.toLocation || '-' }}
            } @else {
              -
            }
          </td>
        </ng-container>
        <ng-container matColumnDef="quantity">
          <th mat-header-cell *matHeaderCellDef class="text-right">Quantity</th>
          <td mat-cell *matCellDef="let r" class="text-right font-bold text-text-primary">{{ r.quantity }}</td>
        </ng-container>
        <ng-container matColumnDef="reference">
          <th mat-header-cell *matHeaderCellDef>Reference</th>
          <td mat-cell *matCellDef="let r" class="text-xs text-text-secondary">
            @if (r.referenceType) {
              {{ r.referenceType }}: {{ r.referenceId }}
            } @else {
              -
            }
          </td>
        </ng-container>
        <ng-container matColumnDef="createdBy">
          <th mat-header-cell *matHeaderCellDef>Created By</th>
          <td mat-cell *matCellDef="let r" class="text-xs">{{ r.createdBy || '-' }}</td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40">history</mat-icon>
        <span class="text-sm">No stock movement ledger records found</span>
      </div>
    }
  `,
})
export class StockMovementHistoryComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<StockMovementHistoryRow[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);

  fromDate = '';
  toDate = '';

  displayedColumns = [
    'movementTime',
    'movementType',
    'productCode',
    'lotNumber',
    'from',
    'to',
    'quantity',
    'reference',
    'createdBy',
  ];

  ngOnInit() {
    this.load();
  }

  load() {
    const params: Record<string, any> = {
      page: this.page(),
      size: this.size(),
    };
    if (this.fromDate) params['fromDate'] = this.fromDate;
    if (this.toDate) params['toDate'] = this.toDate;

    this.reportService.getStockMovementHistory(params).subscribe((r) => {
      if (r.success && r.data) {
        this.items.set(r.data.items);
        this.total.set(r.data.totalElements);
      }
    });
  }

  onPage(e: PageEvent) {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }
}
