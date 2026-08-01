import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ReportService, ProductionOutputRow } from '../../services/report.service';

@Component({
  selector: 'app-production-output',
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
        <ng-container matColumnDef="date">
          <th mat-header-cell *matHeaderCellDef>Date</th>
          <td mat-cell *matCellDef="let r">{{ r.date }}</td>
        </ng-container>
        <ng-container matColumnDef="workOrderCode">
          <th mat-header-cell *matHeaderCellDef>Work Order</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">{{ r.workOrderCode }}</td>
        </ng-container>
        <ng-container matColumnDef="productCode">
          <th mat-header-cell *matHeaderCellDef>Product Code</th>
          <td mat-cell *matCellDef="let r">{{ r.productCode }}</td>
        </ng-container>
        <ng-container matColumnDef="productName">
          <th mat-header-cell *matHeaderCellDef>Product Name</th>
          <td mat-cell *matCellDef="let r">{{ r.productName }}</td>
        </ng-container>
        <ng-container matColumnDef="planned">
          <th mat-header-cell *matHeaderCellDef class="text-right">Planned Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right font-medium">{{ r.plannedQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="actual">
          <th mat-header-cell *matHeaderCellDef class="text-right">Actual Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right font-bold text-text-primary">{{ r.actualQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="good">
          <th mat-header-cell *matHeaderCellDef class="text-right">Good Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-success">{{ r.goodQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="defect">
          <th mat-header-cell *matHeaderCellDef class="text-right">Defect Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-warning">{{ r.defectQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="scrap">
          <th mat-header-cell *matHeaderCellDef class="text-right">Scrap Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-error">{{ r.scrapQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="completionRate">
          <th mat-header-cell *matHeaderCellDef class="text-right">Completion Rate</th>
          <td mat-cell *matCellDef="let r" class="text-right">
            <span class="ff-badge" [class.ff-badge--completed]="r.completionRate >= 100" [class.ff-badge--running]="r.completionRate < 100 && r.completionRate > 0">
              {{ r.completionRate }}%
            </span>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40">precision_manufacturing</mat-icon>
        <span class="text-sm">No production output records found for the selected date range</span>
      </div>
    }
  `,
})
export class ProductionOutputComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<ProductionOutputRow[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);

  fromDate = '';
  toDate = '';

  displayedColumns = [
    'date',
    'workOrderCode',
    'productCode',
    'productName',
    'planned',
    'actual',
    'good',
    'defect',
    'scrap',
    'completionRate',
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

    this.reportService.getProductionOutput(params).subscribe((r) => {
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
