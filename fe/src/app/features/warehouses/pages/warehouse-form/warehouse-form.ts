import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';

@Component({
  selector: 'app-warehouse-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatDialogModule, MatSnackBarModule],
  template: `
  <h2 mat-dialog-title>{{ data ? 'Edit' : 'Add' }} Warehouse</h2>
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
        <mat-label>Address</mat-label>
        <input matInput [(ngModel)]="address" name="address">
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button color="primary" (click)="save()" [disabled]="!code || !name">Save</button>
  </mat-dialog-actions>
  `
})
export class WarehouseFormComponent {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<WarehouseFormComponent>);
  data = inject(MAT_DIALOG_DATA, { optional: true }) as WarehouseDto | null;

  code = '';
  name = '';
  address = '';

  constructor() {
    if (this.data) {
      this.code = this.data.code;
      this.name = this.data.name;
      this.address = this.data.address || '';
    }
  }

  save() {
    const body = { code: this.code, name: this.name, address: this.address };
    const req = this.data
      ? this.api.put(`/api/warehouses/${this.data.id}`, { name: this.name, address: this.address })
      : this.api.post('/api/warehouses', body);
    req.subscribe(r => {
      if (r.success) {
        this.snackBar.open(this.data ? 'Updated' : 'Created', 'OK', { duration: 2000 });
        this.dialogRef.close(true);
      }
    });
  }
}
