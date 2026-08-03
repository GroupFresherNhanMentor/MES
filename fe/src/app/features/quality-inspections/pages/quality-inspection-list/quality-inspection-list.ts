import { Component, inject, signal } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatTooltipModule } from '@angular/material/tooltip';

import { QualityInspectionService } from '../../services/quality-inspection.service';
import { QualityInspectionCreateDialog } from '../../components/quality-inspection-create-dialog';
import { QualityInspectionDecisionDialog } from '../../components/quality-inspection-decision-dialog';
import { QualityInspectionResultsDialog } from '../../components/quality-inspection-results-dialog';
import type { QualityInspectionDto } from '../../../../core/models/quality-inspection.model';

@Component({
  selector: 'app-quality-inspection-list',
  standalone: true,
  imports: [
    CommonModule, DatePipe, DecimalPipe,
    MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatDialogModule,
    MatSnackBarModule, MatPaginatorModule, MatTooltipModule,
  ],
  template: `
    <div class="p-4">
      <mat-card class="ff-card">
        <mat-card-content>
          <div class="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-gray-800 mb-4">
            <div class="flex items-center gap-4">
              <h1 class="text-xl font-semibold m-0 text-text-primary">Quality Inspections</h1>
              <button mat-flat-button color="primary" (click)="openCreateDialog()">
                <mat-icon>add</mat-icon> Create Inspection
              </button>
            </div>
          </div>

          <table mat-table [dataSource]="items()" class="full-width">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>CODE</th>
              <td mat-cell *matCellDef="let q">{{ q.workOrder?.workOrderCode || '-' }}</td>
            </ng-container>
            <ng-container matColumnDef="product">
              <th mat-header-cell *matHeaderCellDef>PRODUCT</th>
              <td mat-cell *matCellDef="let q">{{ q.product?.productCode || '' }} {{ q.product?.productName || '-' }}</td>
            </ng-container>
            <ng-container matColumnDef="lot">
              <th mat-header-cell *matHeaderCellDef>LOT</th>
              <td mat-cell *matCellDef="let q">{{ q.lot?.lotNumber || '-' }}</td>
            </ng-container>
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>QUANTITY</th>
              <td mat-cell *matCellDef="let q">{{ q.quantity | number }}</td>
            </ng-container>
            <ng-container matColumnDef="remaining">
              <th mat-header-cell *matHeaderCellDef>REMAINING</th>
              <td mat-cell *matCellDef="let q">{{ q.remainingQuantity | number }}</td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>STATUS</th>
              <td mat-cell *matCellDef="let q">
                <span class="ff-badge"
                      [class.ff-badge--pending]="q.qcStatusName === 'PENDING_INSPECTION'"
                      [class.ff-badge--active]="q.qcStatusName === 'IN_PROGRESS'"
                      [class.ff-badge--completed]="q.qcStatusName === 'PASSED'"
                      [class.ff-badge--cancelled]="q.qcStatusName === 'FAILED' || q.qcStatusName === 'SCRAPPED'"
                      [class.ff-badge--on-hold]="q.qcStatusName === 'ON_HOLD' || q.qcStatusName === 'REWORK_REQUIRED'">{{ q.qcStatusName }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="createdAt">
              <th mat-header-cell *matHeaderCellDef>DATE</th>
              <td mat-cell *matCellDef="let q">{{ q.createdAt | date:'shortDate' }}</td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>ACTIONS</th>
              <td mat-cell *matCellDef="let q">
                <div class="flex flex-wrap gap-2">
                  <button mat-icon-button color="primary" matTooltip="View results" (click)="openResultsDialog(q)">
                    <mat-icon>fact_check</mat-icon>
                  </button>
                  <button mat-stroked-button color="primary" (click)="openDecisionDialog(q, 'pass')"
                          *ngIf="canAct(q)">Pass</button>
                  <button mat-stroked-button color="warn" (click)="openDecisionDialog(q, 'fail')"
                          *ngIf="canAct(q)">Fail</button>
                </div>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <div *ngIf="items().length === 0" class="text-center py-8 text-text-muted">
            <mat-icon class="text-4xl h-10 w-10 mb-2">fact_check</mat-icon>
            <p>No quality inspections found.</p>
          </div>

          <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
})
export class QualityInspectionList {
  private service = inject(QualityInspectionService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<QualityInspectionDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'product', 'lot', 'quantity', 'remaining', 'status', 'createdAt', 'actions'];

  constructor() { this.load(); }

  load() {
    this.service.getInspections({ page: this.page(), size: this.size() }).subscribe(r => {
      if (r.success && r.data) {
        this.items.set(r.data.items || []);
        this.total.set(r.data.totalElements || 0);
      }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }

  canAct(item: QualityInspectionDto): boolean {
    return item.qcStatusName !== 'FAILED' && item.qcStatusName !== 'PASSED' && item.qcStatusName !== 'SCRAPPED';
  }

  openCreateDialog(): void {
    this.dialog.open(QualityInspectionCreateDialog, { width: '520px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe((success) => {
      if (success) {
        this.snackBar.open('Inspection created', 'OK', { duration: 2000 });
        this.load();
      }
    });
  }

  openDecisionDialog(inspection: QualityInspectionDto, action: 'pass' | 'fail'): void {
    this.dialog.open(QualityInspectionDecisionDialog, {
      width: '440px',
      panelClass: 'ff-dialog-panel',
      data: { inspectionId: inspection.id, action, quantity: inspection.quantity },
    }).afterClosed().subscribe((success) => {
      if (success) {
        this.snackBar.open('Inspection updated', 'OK', { duration: 2000 });
        this.load();
      }
    });
  }

  openResultsDialog(inspection: QualityInspectionDto): void {
    this.dialog.open(QualityInspectionResultsDialog, {
      width: '640px',
      panelClass: 'ff-dialog-panel',
      data: { inspectionId: inspection.id },
    });
  }
}