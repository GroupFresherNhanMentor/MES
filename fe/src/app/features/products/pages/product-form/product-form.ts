import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import type { ProductDto } from '../../../../core/models/product.model';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-product-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatDialogModule, MatSnackBarModule],
  template: `
  <h2 mat-dialog-title>{{ data ? 'Edit' : 'Add' }} Product</h2>
  <mat-dialog-content>
    <div style="display:flex; flex-direction:column; gap:12px; padding-top:8px;">
      <mat-form-field appearance="outline">
        <mat-label>Code</mat-label>
        <input matInput [(ngModel)]="code" name="code" required [disabled]="!!data">
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
        <mat-label>Unit</mat-label>
        <mat-select [(ngModel)]="unitId" name="unit" required>
          @for (u of units; track u.id) { <mat-option [value]="u.id">{{ u.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="productStatusId" name="status" required>
          @for (s of statuses; track s.id) { <mat-option [value]="s.id">{{ s.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button class="ff-btn-primary" (click)="save()" [disabled]="!isValid()">Save</button>
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
  productTypeId = '';
  unitId = '';
  productStatusId = '';
  originalStatusId = '';

  types: LookupEntry[] = [];
  units: LookupEntry[] = [];
  statuses: LookupEntry[] = [];

  constructor() {
    this.api.get<{ items: LookupEntry[] }>('/api/product-types?page=0&size=100').subscribe(r => {
      if (r.success) { this.types = r.data.items; if (!this.data && this.types.length) this.productTypeId = this.types[0].id; }
    });
    this.api.get<{ items: LookupEntry[] }>('/api/units-of-measure?page=0&size=100').subscribe(r => {
      if (r.success) { this.units = r.data.items; if (!this.data && this.units.length) this.unitId = this.units[0].id; }
    });
    this.api.get<{ items: LookupEntry[] }>('/api/product-statuses?page=0&size=100').subscribe(r => {
      if (r.success) {
        this.statuses = r.data.items;
        if (!this.data && this.statuses.length) this.productStatusId = this.statuses[0].id;
      }
    });
    if (this.data) {
      const d = this.data as any;
      this.code = this.data.code;
      this.name = this.data.name;
      this.productTypeId = d.productType?.id || this.data.productTypeId || '';
      this.unitId = d.unit?.id || this.data.unitId || '';
      this.productStatusId = d.productStatus?.id || this.data.productStatusId || '';
      this.originalStatusId = this.productStatusId;
    }
  }

  isValid(): boolean {
    if (this.data) return !!this.name && !!this.unitId && !!this.productStatusId;
    return !!this.code && !!this.name && !!this.productTypeId && !!this.unitId && !!this.productStatusId;
  }

  save() {
    const editId = this.data?.id;
    if (editId) {
      this.api.put(`/api/products/${editId}`, { name: this.name, unitId: this.unitId }).subscribe({
        next: () => {
          // Status changed? call activate/deactivate
          if (this.productStatusId !== this.originalStatusId) {
            const newName = this.statuses.find(s => s.id === this.productStatusId)?.name || '';
            const ep = newName === 'ACTIVE' ? 'activate' : 'deactivate';
            this.api.put(`/api/products/${editId}/${ep}`, {}).subscribe({
              next: () => { this.snackBar.open('Updated', 'OK', { duration: 2000 }); this.dialogRef.close(true); },
              error: e => this.snackBar.open(e?.error?.message || 'Error updating status', 'OK', { duration: 4000 })
            });
          } else {
            this.snackBar.open('Updated', 'OK', { duration: 2000 });
            this.dialogRef.close(true);
          }
        },
        error: e => this.snackBar.open(e?.error?.message || 'Error updating product', 'OK', { duration: 4000 })
      });
    } else {
      const createBody = {
        code: this.code,
        name: this.name,
        version: String(Date.now()),
        productTypeId: this.productTypeId,
        unitId: this.unitId,
        productStatusId: this.productStatusId
      };
      this.api.post('/api/products', createBody).subscribe({
        next: r => { if (r.success) { this.snackBar.open('Created', 'OK', { duration: 2000 }); this.dialogRef.close(true); } },
        error: e => this.snackBar.open(e?.error?.message || 'Error creating product', 'OK', { duration: 4000 })
      });
    }
  }
}
