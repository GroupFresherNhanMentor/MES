import { Component, inject } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import { API } from '../../../../configs/api-endpoints';
import type { ProductDto } from '../../../../core/models/product.model';
import type { CreateWorkOrderRequest, WorkOrderLookupDto } from '../../../../core/models/work-order.model';
import { ApiService } from '../../../../core/services/api';

export interface WorkOrderCreateDialogData {
  products: ProductDto[];
  priorities: WorkOrderLookupDto[];
  statuses: WorkOrderLookupDto[];
}

function endAfterStart(group: AbstractControl): ValidationErrors | null {
  const start = group.get('plannedStartDate')?.value as string | null;
  const end = group.get('plannedEndDate')?.value as string | null;
  if (start && end && end < start) {
    return { endBeforeStart: true };
  }
  return null;
}

@Component({
  selector: 'app-work-order-create-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  template: `
    <h2 mat-dialog-title>Create work order</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="flex min-w-[360px] flex-col gap-3 pt-2">

        <mat-form-field appearance="outline">
          <mat-label>Code</mat-label>
          <input matInput formControlName="code">
          @if (form.get('code')?.hasError('required') && form.get('code')?.touched) {
            <mat-error>Code is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Finished product</mat-label>
          <mat-select formControlName="finishedProductId">
            @for (product of data.products; track product.id) {
              <mat-option [value]="product.id">{{ product.code }} - {{ product.name }}</mat-option>
            }
          </mat-select>
          @if (form.get('finishedProductId')?.hasError('required') && form.get('finishedProductId')?.touched) {
            <mat-error>Finished product is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Planned quantity</mat-label>
          <input matInput type="number" min="0.0001" formControlName="plannedQuantity">
          @if (form.get('plannedQuantity')?.hasError('required') && form.get('plannedQuantity')?.touched) {
            <mat-error>Planned quantity is required</mat-error>
          }
          @if (form.get('plannedQuantity')?.hasError('min')) {
            <mat-error>Quantity must be greater than 0</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Planned start date</mat-label>
          <input matInput type="date" formControlName="plannedStartDate" [min]="minDate">
          @if (form.get('plannedStartDate')?.hasError('required') && form.get('plannedStartDate')?.touched) {
            <mat-error>Planned start date is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Planned end date</mat-label>
          <input matInput type="date" formControlName="plannedEndDate"
                 [min]="form.get('plannedStartDate')?.value || minDate">
          @if (form.get('plannedEndDate')?.hasError('required') && form.get('plannedEndDate')?.touched) {
            <mat-error>Planned end date is required</mat-error>
          }
          @if (form.hasError('endBeforeStart') && form.get('plannedEndDate')?.touched) {
            <mat-error>End date must not be before start date</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Priority</mat-label>
          <mat-select formControlName="priorityId">
            <mat-option value="">No priority</mat-option>
            @for (priority of data.priorities; track priority.id) {
              <mat-option [value]="priority.id">{{ priority.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Initial status</mat-label>
          <mat-select formControlName="workOrderStatusId">
            @for (status of data.statuses; track status.id) {
              <mat-option [value]="status.id">{{ status.name }}</mat-option>
            }
          </mat-select>
          @if (form.get('workOrderStatusId')?.hasError('required') && form.get('workOrderStatusId')?.touched) {
            <mat-error>Status is required</mat-error>
          }
        </mat-form-field>

      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button class="ff-btn-primary" [disabled]="form.invalid || submitting" (click)="submit()">
        Create
      </button>
    </mat-dialog-actions>
  `,
})
export class WorkOrderCreateDialog {
  readonly data = inject<WorkOrderCreateDialogData>(MAT_DIALOG_DATA);
  private readonly api = inject(ApiService);
  private readonly dialogRef = inject(MatDialogRef<WorkOrderCreateDialog>);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  submitting = false;

  readonly minDate: string = new Date().toISOString().slice(0, 10);

  readonly form = this.formBuilder.group(
    {
      code: ['', [Validators.required]],
      finishedProductId: ['', [Validators.required]],
      plannedQuantity: [null as number | null, [Validators.required, Validators.min(0.0001)]],
      plannedStartDate: ['', [Validators.required]],
      plannedEndDate: ['', [Validators.required]],
      priorityId: [''],
      workOrderStatusId: [this.data.statuses.find((s) => s.name === 'DRAFT')?.id ?? '', [Validators.required]],
    },
    { validators: endAfterStart },
  );

  submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const value = this.form.getRawValue();
    const payload: CreateWorkOrderRequest = {
      code: value.code ?? '',
      finishedProductId: value.finishedProductId ?? '',
      plannedQuantity: Number(value.plannedQuantity),
      plannedStartDate: new Date(value.plannedStartDate!).toISOString(),
      plannedEndDate: new Date(value.plannedEndDate!).toISOString(),
      priorityId: value.priorityId || undefined,
      workOrderStatusId: value.workOrderStatusId ?? '',
    };
    this.submitting = true;
    this.api.post(API.workOrders.base, payload).subscribe({
      next: (response) => {
        this.submitting = false;
        if (response.success) this.dialogRef.close(true);
      },
      error: (response) => {
        this.submitting = false;
        this.snackBar.open(response?.error?.message ?? 'Unable to create work order.', 'OK', { duration: 5000 });
      },
    });
  }
}
