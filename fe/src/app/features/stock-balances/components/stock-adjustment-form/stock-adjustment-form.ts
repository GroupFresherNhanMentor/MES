import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { StockBalanceDto } from '../../../../core/models/stock-balance.model';

export interface StockAdjustmentDialogData {
  stockBalance?: StockBalanceDto;
}

@Component({
  selector: 'app-stock-adjustment-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule,
  ],
  template: `
    <h2 mat-dialog-title>Adjust Stock Balance</h2>
    <mat-dialog-content>
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" style="margin-bottom: 16px;"></mat-progress-bar>
      }

      @if (data?.stockBalance; as sb) {
        <div style="padding: 12px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 16px; font-size: 13px;">
          <div style="display:flex; justify-content:space-between; margin-bottom: 4px;">
            <span style="color:#64748b;">Product:</span>
            <span style="font-weight:600; color:#1e293b;">{{ sb.product?.name }} ({{ sb.product?.code }})</span>
          </div>
          <div style="display:flex; justify-content:space-between; margin-bottom: 4px;">
            <span style="color:#64748b;">Warehouse / Location:</span>
            <span style="font-family: monospace; color:#334155;">{{ sb.warehouse?.name }} / {{ sb.location?.code }}</span>
          </div>
          <div style="display:flex; justify-content:space-between; margin-bottom: 4px;">
            <span style="color:#64748b;">Current Balance:</span>
            <span style="font-family: monospace; font-weight:700; color:#0f172a;">{{ sb.quantity | number:'1.0-4' }}</span>
          </div>
          @if (sb.lot?.lotNumber) {
            <div style="display:flex; justify-content:space-between;">
              <span style="color:#64748b;">Lot Number:</span>
              <span style="font-family: monospace; color:#1d4ed8;">{{ sb.lot?.lotNumber }}</span>
            </div>
          }
        </div>
      }

      <form [formGroup]="form" style="display:flex; flex-direction:column; gap:12px; padding-top:4px;">
        @if (!data?.stockBalance) {
          <mat-form-field appearance="outline">
            <mat-label>Stock Balance</mat-label>
            <mat-select formControlName="stockBalanceId" required>
              @for (sb of stockBalancesList(); track sb.id) {
                <mat-option [value]="sb.id">
                  {{ sb.product?.name }} — {{ sb.warehouse?.code }}/{{ sb.location?.code }} (Qty: {{ sb.quantity }})
                </mat-option>
              }
            </mat-select>
          </mat-form-field>
        }

        <mat-form-field appearance="outline">
          <mat-label>Quantity Adjustment (+ or -)</mat-label>
          <input matInput type="number" formControlName="quantityAdjustment" required step="0.01" placeholder="e.g. 5 or -2.5">
          <mat-hint>Enter positive number to add stock, negative number to reduce stock</mat-hint>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Reason</mat-label>
          <input matInput formControlName="reason" required placeholder="e.g. Cycle count adjustment, damage, audit correction">
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Reference No. (Optional)</mat-label>
          <input matInput formControlName="referenceNo" placeholder="e.g. ADJ-2026-08">
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button class="ff-btn-primary" (click)="onSubmit()" [disabled]="form.invalid || submitting()">Submit Adjustment</button>
    </mat-dialog-actions>
  `
})
export class StockAdjustmentFormComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<StockAdjustmentFormComponent>);
  data = inject<StockAdjustmentDialogData>(MAT_DIALOG_DATA, { optional: true });
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  loading = signal(false);
  submitting = signal(false);
  stockBalancesList = signal<StockBalanceDto[]>([]);

  form = this.fb.group({
    stockBalanceId: [this.data?.stockBalance?.id || '', [Validators.required]],
    quantityAdjustment: [0, [Validators.required]],
    reason: ['', [Validators.required]],
    referenceNo: [''],
  });

  ngOnInit(): void {
    if (!this.data?.stockBalance) {
      this.loading.set(true);
      this.api.get<any>(`${API.stockBalances.base}?size=100`).subscribe({
        next: r => {
          if (r.success && r.data) {
            this.stockBalancesList.set(r.data.items || []);
          }
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    }
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting.set(true);

    const payload = {
      stockBalanceId: this.form.value.stockBalanceId,
      quantityAdjustment: Number(this.form.value.quantityAdjustment),
      reason: this.form.value.reason,
      referenceNo: this.form.value.referenceNo || undefined,
    };

    this.api.post(API.stockAdjustments.base, payload).subscribe({
      next: (r: any) => {
        this.submitting.set(false);
        if (r.success) {
          this.snackBar.open('Stock adjustment submitted successfully', 'OK', { duration: 3000 });
          this.dialogRef.close(true);
        }
      },
      error: (e: any) => {
        this.submitting.set(false);
        this.snackBar.open(e?.error?.message || 'Failed to submit stock adjustment', 'OK', { duration: 4000 });
      }
    });
  }
}
