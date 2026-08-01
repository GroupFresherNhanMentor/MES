import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ReportService, DefectRateRow } from '../../services/report.service';

@Component({
  selector: 'app-defect-rate',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
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
        <ng-container matColumnDef="productCode">
          <th mat-header-cell *matHeaderCellDef>Product Code</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">{{ r.productCode }}</td>
        </ng-container>
        <ng-container matColumnDef="productName">
          <th mat-header-cell *matHeaderCellDef>Product Name</th>
          <td mat-cell *matCellDef="let r">{{ r.productName }}</td>
        </ng-container>
        <ng-container matColumnDef="totalInspected">
          <th mat-header-cell *matHeaderCellDef class="text-right">Total Inspected</th>
          <td mat-cell *matCellDef="let r" class="text-right font-medium">{{ r.totalInspected }}</td>
        </ng-container>
        <ng-container matColumnDef="defect">
          <th mat-header-cell *matHeaderCellDef class="text-right">Defect Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-warning font-semibold">{{ r.defectQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="scrap">
          <th mat-header-cell *matHeaderCellDef class="text-right">Scrap Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-error">{{ r.scrapQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="defectRate">
          <th mat-header-cell *matHeaderCellDef class="text-right">Defect Rate</th>
          <td mat-cell *matCellDef="let r" class="text-right">
            <span class="ff-badge" [class.ff-badge--completed]="r.defectRate === 0" [class.ff-badge--cancelled]="r.defectRate > 5" [class.ff-badge--on-hold]="r.defectRate > 0 && r.defectRate <= 5">
              {{ r.defectRate }}%
            </span>
          </td>
        </ng-container>
        <ng-container matColumnDef="topDefectTypes">
          <th mat-header-cell *matHeaderCellDef>Top Defect Types</th>
          <td mat-cell *matCellDef="let r">
            @if (r.topDefectTypes && r.topDefectTypes.length > 0) {
              <div class="flex flex-wrap gap-1">
                @for (dt of r.topDefectTypes; track dt) {
                  <span class="ff-badge ff-badge--on-hold">{{ dt }}</span>
                }
              </div>
            } @else {
              <span class="text-text-muted text-xs">None</span>
            }
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40">rule</mat-icon>
        <span class="text-sm">No quality inspection defect records found</span>
      </div>
    }
  `,
})
export class DefectRateComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<DefectRateRow[]>([]);
  fromDate = '';
  toDate = '';

  displayedColumns = [
    'productCode',
    'productName',
    'totalInspected',
    'defect',
    'scrap',
    'defectRate',
    'topDefectTypes',
  ];

  ngOnInit() {
    this.load();
  }

  load() {
    const params: Record<string, any> = {};
    if (this.fromDate) params['fromDate'] = this.fromDate;
    if (this.toDate) params['toDate'] = this.toDate;

    this.reportService.getDefectRates(params).subscribe((r) => {
      if (r.success && r.data) {
        this.items.set(r.data);
      }
    });
  }
}
