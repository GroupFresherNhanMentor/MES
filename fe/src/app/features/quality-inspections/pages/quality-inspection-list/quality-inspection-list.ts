import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { QualityInspectionDto } from '../../../../core/models/quality-inspection.model';

@Component({
  selector: 'app-quality-inspection-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Quality Inspections</h1>
      <p class="page-subtitle">Manage inspection records and quality control decisions</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let q">{{ q.inspectionCode }}</td>
          </ng-container>
          <ng-container matColumnDef="product">
            <th mat-header-cell *matHeaderCellDef>Product</th>
            <td mat-cell *matCellDef="let q">{{ q.productName }}</td>
          </ng-container>
          <ng-container matColumnDef="inspector">
            <th mat-header-cell *matHeaderCellDef>Inspector</th>
            <td mat-cell *matCellDef="let q">{{ q.inspectorName }}</td>
          </ng-container>
          <ng-container matColumnDef="sample">
            <th mat-header-cell *matHeaderCellDef>Sample/Defects</th>
            <td mat-cell *matCellDef="let q">{{ q.sampleSize }}/{{ q.defectsFound }}</td>
          </ng-container>
          <ng-container matColumnDef="date">
            <th mat-header-cell *matHeaderCellDef>Date</th>
            <td mat-cell *matCellDef="let q">{{ q.inspectionDate | date:'shortDate' }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let q">
              <span class="ff-badge" [class.ff-badge--completed]="q.status === 'PASSED'" [class.ff-badge--cancelled]="q.status === 'FAILED'" [class.ff-badge--pending]="q.status === 'PENDING'" [class.ff-badge--on-hold]="q.status === 'ON_HOLD' || q.status === 'RELEASED'">{{ q.status }}</span>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class QualityInspectionList {
  private api = inject(ApiService);
  items = signal<QualityInspectionDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'product', 'inspector', 'sample', 'date', 'status'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: QualityInspectionDto[]; totalElements: number }>(`/api/quality-inspections?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
