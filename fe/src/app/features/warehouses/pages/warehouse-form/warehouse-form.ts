import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';

interface Status { id: string; name: string; }

@Component({
  selector: 'app-warehouse-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatDialogModule, MatSnackBarModule],
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
        <input matInput [(ngModel)]="address" name="address" required>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="statusId" name="status" required>
          @for (s of statuses; track s.id) { <mat-option [value]="s.id">{{ s.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button class="ff-btn-primary" (click)="save()" [disabled]="!code || !name || !address || !statusId">Save</button>
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
  statusId = '';
  originalStatusId = '';
  statuses: Status[] = [];

  constructor() {
    this.api.get<{ items: Status[] }>('/api/warehouse-statuses?page=0&size=50').subscribe(r => {
      if (r.success) {
        this.statuses = r.data.items;
        if (!this.data && this.statuses.length) this.statusId = this.statuses[0].id;
      }
    });
    if (this.data) {
      this.code = this.data.code;
      this.name = this.data.name;
      this.address = this.data.address || '';
      this.statusId = (this.data as any).warehouseStatus?.id || this.data.warehouseStatusId || '';
      this.originalStatusId = this.statusId;
    }
  }

  save() {
    const editId = this.data?.id;
    if (editId) {
      this.api.put(`/api/warehouses/${editId}`, { name: this.name, address: this.address, warehouseStatusId: this.statusId }).subscribe({
        next: () => { this.snackBar.open('Updated', 'OK', { duration: 2000 }); this.dialogRef.close(true); },
        error: e => this.snackBar.open(e?.error?.message || 'Error updating warehouse', 'OK', { duration: 4000 })
      });
    } else {
      this.api.post('/api/warehouses', { code: this.code, name: this.name, address: this.address, warehouseStatusId: this.statusId }).subscribe({
        next: r => { if (r.success) { this.snackBar.open('Created', 'OK', { duration: 2000 }); this.dialogRef.close(true); } },
        error: e => this.snackBar.open(e?.error?.message || 'Error creating warehouse', 'OK', { duration: 4000 })
      });
    }
  }
}
