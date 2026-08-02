import { Component, inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import type { ProductDto } from '../../../../core/models/product.model';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-product-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatTooltipModule, MatDialogModule, MatSnackBarModule],
  template: `
  <h2 mat-dialog-title>{{ data ? 'Edit' : 'Add' }} Product</h2>
  <mat-dialog-content>
    <div style="display:flex; flex-direction:column; gap:12px; padding-top:8px;">
      <mat-form-field appearance="outline">
        <mat-label>Code</mat-label>
        <input matInput [(ngModel)]="code" name="code" required [disabled]="!!data">
        @if (!data) {
          <button matSuffix mat-icon-button type="button" (click)="genCode()" matTooltip="Auto-generate code">
            <mat-icon>auto_awesome</mat-icon>
          </button>
        }
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Name</mat-label>
        <input matInput [(ngModel)]="name" name="name" required>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Type</mat-label>
        <mat-select [(ngModel)]="productTypeId" name="type" required [disabled]="!!data">
          @for (t of types; track t.id) { <mat-option [value]="t.id">{{ t.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Version</mat-label>
        <input matInput [(ngModel)]="version" name="version" required [disabled]="!!data">
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Unit</mat-label>
        <mat-select [(ngModel)]="unitId" name="unit" required>
          @for (u of units; track u.id) { <mat-option [value]="u.id">{{ u.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button class="ff-btn-primary" (click)="save()" [disabled]="loading || !isValid()">Save</button>
  </mat-dialog-actions>
  `
})
export class ProductFormComponent {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<ProductFormComponent>);
  data = inject(MAT_DIALOG_DATA, { optional: true }) as ProductDto | null;

  code = '';
  name = '';
  version = '';
  productTypeId = '';
  unitId = '';

  types: LookupEntry[] = [];
  units: LookupEntry[] = [];
  loading = true;

  constructor() {
    forkJoin({
      types: this.api.get<{ items: LookupEntry[] }>('/api/product-types?page=0&size=100'),
      units: this.api.get<{ items: LookupEntry[] }>('/api/units-of-measure?page=0&size=100')
    }).subscribe({
      next: ({ types, units }) => {
        if (types.success) { this.types = types.data.items; if (!this.data && this.types.length) this.productTypeId = this.types[0].id; }
        if (units.success) { this.units = units.data.items; if (!this.data && this.units.length) this.unitId = this.units[0].id; }
      },
      complete: () => { this.loading = false; }
    });
    if (this.data) {
      const d = this.data as any;
      this.code = this.data.code;
      this.name = this.data.name;
      this.version = String(this.data.version ?? '');
      this.productTypeId = d.productType?.id || this.data.productTypeId || '';
      this.unitId = d.unit?.id || this.data.unitId || '';
    }
  }

  genCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    const rand = Array.from({ length: 8 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
    this.code = `PRD-${rand}`;
  }

  isValid(): boolean {
    if (this.data) return !!this.name && !!this.unitId;
    return !!this.code && !!this.name && !!this.version && !!this.productTypeId && !!this.unitId;
  }

  save() {
    const editId = this.data?.id;
    if (editId) {
      this.api.put(`/api/products/${editId}`, { name: this.name, unitId: this.unitId }).subscribe({
        next: () => { this.snackBar.open('Updated', 'OK', { duration: 2000 }); this.dialogRef.close(true); },
        error: e => this.snackBar.open(e?.error?.message || 'Error updating product', 'OK', { duration: 4000 })
      });
    } else {
      const createBody = {
        code: this.code,
        name: this.name,
        version: this.version,
        productTypeId: this.productTypeId,
        unitId: this.unitId
      };
      this.api.post('/api/products', createBody).subscribe({
        next: r => { if (r.success) { this.snackBar.open('Created', 'OK', { duration: 2000 }); this.dialogRef.close(true); } },
        error: e => this.snackBar.open(e?.error?.message || 'Error creating product', 'OK', { duration: 4000 })
      });
    }
  }
}
