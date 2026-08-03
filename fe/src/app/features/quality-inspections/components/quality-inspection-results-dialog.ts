import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { QualityInspectionService } from '../services/quality-inspection.service';
import type { QualityInspectionResultDto } from '../../../core/models/quality-inspection.model';

interface ResultsDialogData {
  inspectionId: string;
}

@Component({
  selector: 'app-quality-inspection-results-dialog',
  standalone: true,
  imports: [CommonModule, DatePipe, DecimalPipe, MatDialogModule, MatTableModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <h2 mat-dialog-title>Inspection Results</h2>
    <mat-dialog-content class="!text-text-primary">
      <mat-progress-bar *ngIf="loading()" mode="indeterminate"></mat-progress-bar>

      <table *ngIf="!loading() && results().length > 0" mat-table [dataSource]="results()" class="full-width mt-2">
        <ng-container matColumnDef="result">
          <th mat-header-cell *matHeaderCellDef>RESULT</th>
          <td mat-cell *matCellDef="let r">
            <span class="ff-badge"
                  [class.ff-badge--completed]="r.isPass"
                  [class.ff-badge--cancelled]="!r.isPass">{{ r.isPass ? 'PASS' : 'FAIL' }}</span>
          </td>
        </ng-container>
        <ng-container matColumnDef="quantity">
          <th mat-header-cell *matHeaderCellDef>QUANTITY</th>
          <td mat-cell *matCellDef="let r">{{ r.quantity | number }}</td>
        </ng-container>
        <ng-container matColumnDef="defect">
          <th mat-header-cell *matHeaderCellDef>DEFECT TYPE</th>
          <td mat-cell *matCellDef="let r">{{ r.defectTypeName || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="action">
          <th mat-header-cell *matHeaderCellDef>ACTION</th>
          <td mat-cell *matCellDef="let r">{{ r.actionName || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="reason">
          <th mat-header-cell *matHeaderCellDef>REASON</th>
          <td mat-cell *matCellDef="let r">{{ r.reason || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="inspector">
          <th mat-header-cell *matHeaderCellDef>INSPECTOR</th>
          <td mat-cell *matCellDef="let r">{{ r.inspector?.fullName || r.inspector?.username || '-' }}</td>
        </ng-container>
        <ng-container matColumnDef="inspectedAt">
          <th mat-header-cell *matHeaderCellDef>DATE</th>
          <td mat-cell *matCellDef="let r">{{ r.inspectedAt | date:'short' }}</td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>

      <div *ngIf="!loading() && results().length === 0" class="text-center py-8 text-text-muted">
        <mat-icon class="text-4xl h-10 w-10 mb-2">fact_check</mat-icon>
        <p>No inspection results recorded.</p>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onClose()">Close</button>
    </mat-dialog-actions>
  `,
})
export class QualityInspectionResultsDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<QualityInspectionResultsDialog>);
  private data = inject<ResultsDialogData>(MAT_DIALOG_DATA);
  private service = inject(QualityInspectionService);

  results = signal<QualityInspectionResultDto[]>([]);
  total = signal(0);
  loading = signal(false);
  displayedColumns = ['result', 'quantity', 'defect', 'action', 'reason', 'inspector', 'inspectedAt'];

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.service.getInspectionResults(this.data.inspectionId, { page: 0, size: 100 }).subscribe({
      next: (r) => {
        if (r.success && r.data) {
          this.results.set(r.data.items || []);
          this.total.set(r.data.totalElements || 0);
        }
      },
      complete: () => this.loading.set(false),
    });
  }

  onClose(): void {
    this.dialogRef.close(null);
  }
}