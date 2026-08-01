import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { ReportService, InventorySummaryRow } from '../../services/report.service';

@Component({
  selector: 'app-inventory-summary',
  standalone: true,
  imports: [
    FormsModule,
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
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-64">
          <mat-label>Product Code</mat-label>
          <input matInput [(ngModel)]="productCode" (keyup.enter)="load()" placeholder="Search product code">
          <mat-icon matSuffix class="text-text-muted">search</mat-icon>
        </mat-form-field>
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-48">
          <mat-label>Product Type</mat-label>
          <input matInput [(ngModel)]="productType" (keyup.enter)="load()" placeholder="e.g. RAW_MATERIAL">
        </mat-form-field>
        <button mat-raised-button class="ff-btn-primary" (click)="load()">
          <mat-icon>filter_alt</mat-icon> Filter
        </button>
      </div>
    </div>

    @if (items().length > 0) {
      <table mat-table [dataSource]="items()" class="w-full">
        <ng-container matColumnDef="productCode">
          <th mat-header-cell *matHeaderCellDef>Product Code</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">{{ r.productCode }}</td>
        </ng-container>
        <ng-container matColumnDef="productName">
          <th mat-header-cell *matHeaderCellDef>Product Name</th>
          <td mat-cell *matCellDef="let r">{{ r.productName }}</td>
        </ng-container>
        <ng-container matColumnDef="productType">
          <th mat-header-cell *matHeaderCellDef>Type</th>
          <td mat-cell *matCellDef="let r">
            <span class="ff-badge ff-badge--active">{{ r.productType || 'N/A' }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="warehouseName">
          <th mat-header-cell *matHeaderCellDef>Warehouse</th>
          <td mat-cell *matCellDef="let r">{{ r.warehouseName }}</td>
        </ng-container>
        <ng-container matColumnDef="available">
          <th mat-header-cell *matHeaderCellDef class="text-right">Available</th>
          <td mat-cell *matCellDef="let r" class="text-right font-medium text-success">{{ r.availableQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="reserved">
          <th mat-header-cell *matHeaderCellDef class="text-right">Reserved</th>
          <td mat-cell *matCellDef="let r" class="text-right text-warning">{{ r.reservedQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="inspection">
          <th mat-header-cell *matHeaderCellDef class="text-right">Inspection</th>
          <td mat-cell *matCellDef="let r" class="text-right text-text-muted">{{ r.qualityInspectionQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="onHold">
          <th mat-header-cell *matHeaderCellDef class="text-right">On Hold</th>
          <td mat-cell *matCellDef="let r" class="text-right text-error">{{ r.onHoldQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="scrapped">
          <th mat-header-cell *matHeaderCellDef class="text-right">Scrapped</th>
          <td mat-cell *matCellDef="let r" class="text-right text-text-muted">{{ r.scrappedQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="totalOnHand">
          <th mat-header-cell *matHeaderCellDef class="text-right font-semibold">Total On Hand</th>
          <td mat-cell *matCellDef="let r" class="text-right font-bold text-text-primary">{{ r.totalOnHand }}</td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
      <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40">inventory_2</mat-icon>
        <span class="text-sm">No inventory summary records found</span>
      </div>
    }
  `,
})
export class InventorySummaryComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<InventorySummaryRow[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);

  productCode = '';
  productType = '';

  displayedColumns = [
    'productCode',
    'productName',
    'productType',
    'warehouseName',
    'available',
    'reserved',
    'inspection',
    'onHold',
    'scrapped',
    'totalOnHand',
  ];

  ngOnInit() {
    this.load();
  }

  load() {
    const params: Record<string, any> = {
      page: this.page(),
      size: this.size(),
    };
    if (this.productCode.trim()) params['productCode'] = this.productCode.trim();
    if (this.productType.trim()) params['productType'] = this.productType.trim();

    this.reportService.getInventorySummary(params).subscribe((r) => {
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
