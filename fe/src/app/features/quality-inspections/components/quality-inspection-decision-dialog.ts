import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/services/api';
import { API } from '../../../configs/api-endpoints';
import { QualityInspectionService } from '../services/quality-inspection.service';
import type { FailQcRequest, PassQcRequest, QualityLookupDto } from '../../../core/models/quality-inspection.model';

interface DecisionDialogData {
  inspectionId: string;
  action: 'pass' | 'fail';
  quantity: number;
}

interface Option { id: string; label: string; }

@Component({
  selector: 'app-quality-inspection-decision-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title>{{ title() }}</h2>
    <mat-dialog-content class="!text-text-primary">
      <form [formGroup]="form" class="flex flex-col gap-4 pt-2">
        <div class="flex items-center justify-between gap-2 text-sm">
          <span class="text-text-secondary">Remaining</span>
          <span class="font-semibold text-text-primary">
            <ng-container *ngIf="remaining() !== null; else remainingLoading">
              {{ remaining() | number }} / {{ quantity | number }}
            </ng-container>
            <ng-template #remainingLoading class="text-text-muted">Loading…</ng-template>
          </span>
        </div>

        <mat-form-field appearance="outline">
          <mat-label>{{ action === 'pass' ? 'Passed Quantity' : 'Failed Quantity' }}</mat-label>
          <input matInput type="number" min="0.0001" step="any" formControlName="quantity" placeholder="0">
        </mat-form-field>

        <ng-container *ngIf="action === 'fail'">
          <mat-form-field appearance="outline">
            <mat-label>Defect Type</mat-label>
            <mat-select formControlName="defectTypeId">
              <mat-option *ngFor="let d of defectTypes()" [value]="d.id">{{ d.label }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>QC Action</mat-label>
            <mat-select formControlName="actionId">
              <mat-option *ngFor="let a of qcActions()" [value]="a.id">{{ a.label }}</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Reason</mat-label>
            <textarea matInput rows="2" formControlName="reason"></textarea>
          </mat-form-field>
        </ng-container>

        <mat-form-field appearance="outline">
          <mat-label>Note</mat-label>
          <textarea matInput rows="2" formControlName="note"></textarea>
        </mat-form-field>

        <p *ngIf="errorMessage()" class="text-sm text-red-500">{{ errorMessage() }}</p>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || loading()">Confirm</button>
    </mat-dialog-actions>
  `,
})
export class QualityInspectionDecisionDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<QualityInspectionDecisionDialog>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private service = inject(QualityInspectionService);
  private data = inject<DecisionDialogData>(MAT_DIALOG_DATA);

  readonly action = this.data.action;
  readonly quantity = this.data.quantity;
  readonly title = signal(this.action === 'pass' ? 'Pass Inspection' : 'Fail Inspection');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly defectTypes = signal<Option[]>([]);
  readonly qcActions = signal<Option[]>([]);
  readonly remaining = signal<number | null>(null);

  form = this.fb.group({
    quantity: [0, [Validators.required, Validators.min(0.0001)]],
    defectTypeId: [''],
    actionId: [''],
    reason: [''],
    note: [''],
  });

  ngOnInit(): void {
    if (this.action === 'fail') {
      this.loadFailOptions();
      this.form.controls.defectTypeId.setValidators([Validators.required]);
      this.form.controls.actionId.setValidators([Validators.required]);
      this.form.controls.reason.setValidators([Validators.required]);
    }
    this.loadRemaining();
  }

  private loadRemaining(): void {
    this.service.getInspectionRemaining(this.data.inspectionId, this.quantity).subscribe({
      next: (remaining) => {
        this.remaining.set(remaining);
        this.form.controls.quantity.addValidators(Validators.max(remaining));
        this.form.controls.quantity.updateValueAndValidity();
      },
      error: () => this.remaining.set(null),
    });
  }

  private loadFailOptions(): void {
    this.api.get<any>(`${API.qualityInspections.defectTypes}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.defectTypes.set(items.map((item: QualityLookupDto) => ({ id: item.id, label: item.name || item.id })));
      },
    });

    this.api.get<any>(`${API.qualityInspections.qcActions}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.qcActions.set(items.map((item: QualityLookupDto) => ({ id: item.id, label: item.name || item.id })));
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set(this.action === 'pass' ? 'Please enter a valid passed quantity' : 'Please fill required fail fields');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);
    this.form.disable();

    const inspectionId = this.data.inspectionId;
    const url = this.action === 'pass'
      ? API.qualityInspections.pass(inspectionId)
      : API.qualityInspections.fail(inspectionId);

    const payload = this.buildPayload();
    this.api.post<any>(url, payload).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.form.enable();
        if (res.success) {
          this.snackBar.open(this.action === 'pass' ? 'Inspection passed' : 'Inspection failed', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
          return;
        }
        this.errorMessage.set(res.message || 'Action failed');
      },
      error: (err) => {
        this.loading.set(false);
        this.form.enable();
        this.errorMessage.set(err?.error?.message || 'Unable to complete action');
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  private buildPayload(): PassQcRequest | FailQcRequest {
    const v = this.form.value;
    if (this.action === 'pass') {
      const payload: PassQcRequest = { passedQuantity: Number(v.quantity), note: v.note || undefined };
      return payload;
    }
    const payload: FailQcRequest = {
      failedQuantity: Number(v.quantity),
      actionId: v.actionId!,
      defectTypeId: v.defectTypeId!,
      reason: v.reason!,
      note: v.note || undefined,
    };
    return payload;
  }

  private unwrapList(response: any): any[] {
    const body = this.unwrapBody<any>(response);
    return (Array.isArray(body) ? body : body?.items) || [];
  }

  private unwrapBody<T>(response: any): T {
    if (response && typeof response === 'object' && 'data' in response) {
      return response.data as T;
    }
    return response as T;
  }
}