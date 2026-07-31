import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { ProductDto } from '../../../../core/models/product.model';

interface LookupEntry { id: string; name: string; description: string; }

@Component({
  selector: 'app-product-form',
  imports: [
    FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, 
    MatSelectModule, MatDialogModule, MatSnackBarModule
  ],
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

      @if (!data) {
        <mat-form-field appearance="outline">
          <mat-label>Product Type</mat-label>
          <mat-select [(ngModel)]="productTypeId" name="productTypeId" required>
            @for (t of types; track t.id) {
              <mat-option [value]="t.id">{{ t.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      }

      <mat-form-field appearance="outline">
        <mat-label>Unit of Measure</mat-label>
        <mat-select [(ngModel)]="unitId" name="unitId" required>
          @for (u of units; track u.id) {
            <mat-option [value]="u.id">{{ u.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="productStatusId" name="productStatusId" required>
          @for (s of statuses; track s.id) {
            <mat-option [value]="s.id">{{ s.name }}</mat-option>
          }
        </mat-select>
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button color="primary" (click)="save()" [disabled]="!isValid()">Save</button>
  </mat-dialog-actions>
  `
})
export class ProductFormComponent implements OnInit {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<ProductFormComponent>);
  data = inject(MAT_DIALOG_DATA, { optional: true }) as ProductDto | null;

  code = '';
  name = '';
  productTypeId = '';
  unitId = '';
  productStatusId = '';

  types: LookupEntry[] = [];
  units: LookupEntry[] = [];
  statuses: LookupEntry[] = [];

  ngOnInit() {
    this.api.get<any>(API.products.productTypes + '?size=100').subscribe(r => {
      if (r.success && r.data) {
        this.types = r.data.items || r.data;
        if (!this.data && this.types.length > 0) this.productTypeId = this.types[0].id;
      }
    });

    this.api.get<any>(API.products.unitsOfMeasure + '?size=100').subscribe(r => {
      if (r.success && r.data) {
        this.units = r.data.items || r.data;
        if (!this.data && this.units.length > 0) this.unitId = this.units[0].id;
      }
    });

    this.api.get<any>(API.products.productStatuses + '?size=100').subscribe(r => {
      if (r.success && r.data) {
        this.statuses = r.data.items || r.data;
        if (!this.data && this.statuses.length > 0) this.productStatusId = this.statuses[0].id;
      }
    });

    if (this.data) {
      this.code = this.data.code;
      this.name = this.data.name;
      this.productTypeId = this.data.productType?.id || this.data.productTypeId || '';
      this.unitId = this.data.unit?.id || this.data.unitId || '';
      this.productStatusId = this.data.productStatus?.id || this.data.productStatusId || '';
    }
  }

  isValid(): boolean {
    if (this.data) {
      return !!this.name && !!this.unitId && !!this.productStatusId;
    }
    return !!this.code && !!this.name && !!this.productTypeId && !!this.unitId && !!this.productStatusId;
  }

  save() {
    if (this.data) {
      const updateBody = {
        name: this.name,
        unitId: this.unitId,
        productStatusId: this.productStatusId
      };
      this.api.put(`${API.products.base}/${this.data.id}`, updateBody).subscribe(r => {
        if (r.success) {
          this.snackBar.open('Updated', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
        }
      });
    } else {
      const createBody = {
        code: this.code,
        name: this.name,
        productTypeId: this.productTypeId,
        unitId: this.unitId,
        productStatusId: this.productStatusId
      };
      this.api.post(API.products.base, createBody).subscribe(r => {
        if (r.success) {
          this.snackBar.open('Created', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
        }
      });
    }
  }
}
