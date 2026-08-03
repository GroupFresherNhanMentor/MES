import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/services/api';
import { API } from '../../../configs/api-endpoints';
import type { CreateQualityInspectionRequest } from '../../../core/models/quality-inspection.model';

interface Option { id: string; label: string; }

@Component({
  selector: 'app-quality-inspection-create-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title>Create Quality Inspection</h2>
    <mat-dialog-content class="!text-text-primary">
      <form [formGroup]="form" class="flex flex-col gap-4 pt-2">
        <mat-form-field appearance="outline">
          <mat-label>Work Order</mat-label>
          <mat-select formControlName="workOrderId">
            <mat-option *ngFor="let wo of workOrders()" [value]="wo.id">{{ wo.label }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Product</mat-label>
          <mat-select formControlName="productId">
            <mat-option *ngFor="let p of products()" [value]="p.id">{{ p.label }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Stock Lot</mat-label>
          <mat-select formControlName="lotId">
            <mat-option *ngFor="let lot of lots()" [value]="lot.id">{{ lot.label }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>QC Status</mat-label>
          <mat-select formControlName="qcStatusId">
            <mat-option *ngFor="let s of qcStatuses()" [value]="s.id">{{ s.label }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Quantity</mat-label>
          <input matInput type="number" min="0" step="any" formControlName="quantity" placeholder="0">
        </mat-form-field>

        <p *ngIf="errorMessage()" class="text-sm text-red-500">{{ errorMessage() }}</p>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="form.invalid || loading()">Save</button>
    </mat-dialog-actions>
  `,
})
export class QualityInspectionCreateDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<QualityInspectionCreateDialog>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  workOrders = signal<Option[]>([]);
  products = signal<Option[]>([]);
  lots = signal<Option[]>([]);
  qcStatuses = signal<Option[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    workOrderId: ['', Validators.required],
    productId: ['', Validators.required],
    lotId: ['', Validators.required],
    qcStatusId: ['', Validators.required],
    quantity: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.loadOptions();
  }

  private loadOptions(): void {
    this.api.get<any>(`${API.workOrders.base}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.workOrders.set(items.map((item: any) => ({ id: item.id, label: item.code || item.name || item.id })));
      },
    });

    this.api.get<any>(`${API.products.base}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.products.set(items.map((item: any) => ({ id: item.id, label: item.name || item.code || item.id })));
      },
    });

    this.api.get<any>(`${API.stockLots.base}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.lots.set(items.map((item: any) => ({ id: item.id, label: item.lotNumber || item.name || item.id })));
      },
    });

    this.api.get<any>(`${API.qualityInspections.qcStatuses}?page=0&size=100`).subscribe({
      next: (res) => {
        const items = this.unwrapList(res);
        this.qcStatuses.set(items.map((item: any) => ({ id: item.id, label: item.name || item.id })));
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const body: CreateQualityInspectionRequest = {
      workOrderId: this.form.value.workOrderId!,
      productId: this.form.value.productId!,
      lotId: this.form.value.lotId!,
      qcStatusId: this.form.value.qcStatusId!,
      quantity: Number(this.form.value.quantity),
    };

    this.api.post<any>(API.qualityInspections.base, body).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.snackBar.open('Inspection created successfully', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
          return;
        }
        this.errorMessage.set(res.message || 'Failed to create inspection');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Unable to create inspection');
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
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