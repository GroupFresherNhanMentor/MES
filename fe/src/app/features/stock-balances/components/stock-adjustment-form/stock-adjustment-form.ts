import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
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
    DecimalPipe,
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
  templateUrl: './stock-adjustment-form.html',
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

  private submissionIdempotencyKey: string | null = null;

  onSubmit(): void {
    if (this.form.invalid) return;
    if (!this.submissionIdempotencyKey) {
      this.submissionIdempotencyKey = crypto.randomUUID();
    }
    this.submitting.set(true);

    const payload = {
      stockBalanceId: this.form.value.stockBalanceId,
      quantityAdjustment: Number(this.form.value.quantityAdjustment),
      reason: this.form.value.reason,
      referenceNo: this.form.value.referenceNo || undefined,
    };

    this.api.post(API.stockAdjustments.base, payload, { idempotencyKey: this.submissionIdempotencyKey }).subscribe({
      next: (r: any) => {
        this.submitting.set(false);
        if (r.success) {
          const message = r?.message || 'Stock adjustment submitted successfully';
          this.snackBar.open(message, 'OK', { duration: 3000 });
          this.dialogRef.close(true);
        }
      },
      error: (e: any) => {
        this.submitting.set(false);
        const errorMessage = e?.error?.message || e?.message || 'Failed to submit stock adjustment';
        this.snackBar.open(errorMessage, 'OK', { duration: 4000 });
      }
    });
  }
}
