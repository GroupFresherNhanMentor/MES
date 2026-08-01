import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { ProductDto } from '../../../../core/models/product.model';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';
import type { LocationDto } from '../../../../core/models/location.model';

@Component({
  selector: 'app-stock-in-form',
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
    MatAutocompleteModule,
    MatTooltipModule,
  ],
  template: `
    <h2 mat-dialog-title>Stock In</h2>
    <mat-dialog-content>
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" style="margin-bottom: 16px;"></mat-progress-bar>
      }
      <form [formGroup]="form" style="display:flex; flex-direction:column; gap:12px; padding-top:8px;">
        <mat-form-field appearance="outline">
          <mat-label>Product</mat-label>
          <mat-select formControlName="productId" required>
            @for (prod of productsList(); track prod.id) {
              <mat-option [value]="prod.id">{{ prod.name }} ({{ prod.code }})</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <div style="display:flex; gap:12px;">
          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Warehouse</mat-label>
            <mat-select formControlName="warehouseId" required (selectionChange)="onWarehouseChange($event.value)">
              @for (wh of warehousesList(); track wh.id) {
                <mat-option [value]="wh.id">{{ wh.name }} ({{ wh.code }})</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Location</mat-label>
            <mat-select formControlName="locationId" required [disabled]="!form.get('warehouseId')?.value">
              @for (loc of locationsList(); track loc.id) {
                <mat-option [value]="loc.id">{{ loc.name }} ({{ loc.code }})</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </div>

        <div style="display:flex; gap:12px;">
          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Quantity</mat-label>
            <input matInput type="number" formControlName="quantity" required step="0.01">
          </mat-form-field>

          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Lot Number</mat-label>
            <input matInput formControlName="lotNumber" [matAutocomplete]="lotAuto" required>
            <button matSuffix mat-icon-button type="button" (click)="genLotNumber()" matTooltip="Auto-generate lot number">
              <mat-icon>auto_awesome</mat-icon>
            </button>
            <mat-autocomplete #lotAuto="matAutocomplete">
              @for (lot of filteredLotNumbersList(); track lot.id) {
                <mat-option [value]="lot.lotNumber">{{ lot.lotNumber }}</mat-option>
              }
            </mat-autocomplete>
          </mat-form-field>
        </div>

        <div style="display:flex; gap:12px;">
          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Reference No.</mat-label>
            <input matInput formControlName="referenceNo" required>
          </mat-form-field>

          <mat-form-field appearance="outline" style="flex:1">
            <mat-label>Reason (Optional)</mat-label>
            <input matInput formControlName="reason">
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button class="ff-btn-primary" (click)="onSubmit()" [disabled]="form.invalid || submitting()">Submit</button>
    </mat-dialog-actions>
  `
})
export class StockInFormComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<StockInFormComponent>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  loading = signal(true);
  submitting = signal(false);
  productsList = signal<ProductDto[]>([]);
  warehousesList = signal<WarehouseDto[]>([]);
  locationsList = signal<LocationDto[]>([]);
  lotNumbersList = signal<any[]>([]);
  filteredLotNumbersList = signal<any[]>([]);

  form = this.fb.group({
    productId: ['', [Validators.required]],
    warehouseId: ['', [Validators.required]],
    locationId: ['', [Validators.required]],
    quantity: [0, [Validators.required, Validators.min(0.0001)]],
    lotNumber: ['', [Validators.required]],
    referenceNo: ['', [Validators.required]],
    reason: [''],
  });

  ngOnInit(): void {
    this.form.get('lotNumber')?.valueChanges.subscribe(val => {
      const search = (val || '').toLowerCase();
      this.filteredLotNumbersList.set(
        this.lotNumbersList().filter(lot => lot.lotNumber?.toLowerCase().includes(search))
      );
    });

    // Load products, warehouses, and lots in parallel
    let loadedCount = 0;
    const checkLoading = () => {
      loadedCount++;
      if (loadedCount === 3) this.loading.set(false);
    };

    this.api.get<any>(`${API.products.base}?size=100`).subscribe({
      next: r => { if (r.success && r.data) this.productsList.set(r.data.items || []); checkLoading(); },
      error: () => checkLoading()
    });

    this.api.get<any>(`${API.warehouses.base}?size=100`).subscribe({
      next: r => { if (r.success && r.data) this.warehousesList.set(r.data.items || []); checkLoading(); },
      error: () => checkLoading()
    });

    this.api.get<any>(`${(API as any).stockLots.base}?size=100`).subscribe({
      next: r => { 
        if (r.success && r.data) {
          this.lotNumbersList.set(r.data.items || []); 
          this.filteredLotNumbersList.set(this.lotNumbersList());
        }
        checkLoading(); 
      },
      error: () => checkLoading()
    });
  }

  genLotNumber() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    const rand = Array.from({ length: 10 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
    this.form.get('lotNumber')?.setValue(`LOT-${rand}`);
  }

  onWarehouseChange(warehouseId: string) {
    this.form.get('locationId')?.setValue('');
    if (!warehouseId) {
      this.locationsList.set([]);
      return;
    }
    this.loading.set(true);
    this.api.get<any>(`${API.locations.base(warehouseId)}?size=100`).subscribe({
      next: r => {
        if (r.success && r.data) {
          this.locationsList.set(r.data.items || []);
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.submitting.set(true);

    const payload = {
      productId: this.form.value.productId,
      warehouseId: this.form.value.warehouseId,
      locationId: this.form.value.locationId,
      quantity: Number(this.form.value.quantity),
      lotNumber: this.form.value.lotNumber,
      referenceNo: this.form.value.referenceNo,
      reason: this.form.value.reason || undefined,
    };

    this.api.post((API as any).stockMovements.in || '/api/stock-in', payload).subscribe({
      next: r => {
        this.submitting.set(false);
        if (r.success) {
          this.snackBar.open('Stock received successfully', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
        }
      },
      error: e => {
        this.submitting.set(false);
        this.snackBar.open(e?.error?.message || 'Error processing stock in', 'OK', { duration: 4000 });
      }
    });
  }
}
