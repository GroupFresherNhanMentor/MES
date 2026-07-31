import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import type { MachineDto } from '../../../../core/models/machine.model';

interface Status { id: string; name: string; }

@Component({
  selector: 'app-machine-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatDialogModule, MatSnackBarModule],
  template: `
  <h2 mat-dialog-title>{{ data ? 'Edit' : 'Add' }} Machine</h2>
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
        <mat-label>Production Line</mat-label>
        <mat-select [(ngModel)]="productionLineId" name="lineId" required>
          @for (l of lines; track l.id) { <mat-option [value]="l.id">{{ l.code }} - {{ l.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select [(ngModel)]="statusId" name="status" required>
          @for (s of statuses; track s.id) { <mat-option [value]="s.id">{{ s.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
      @if (!data) {
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
    <button mat-raised-button class="ff-btn-primary" (click)="save()" [disabled]="!code || !name || !statusId || !productionLineId">Save</button>
  </mat-dialog-actions>
  `
})
export class MachineFormComponent {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private dialogRef = inject(MatDialogRef<MachineFormComponent>);
  data = inject(MAT_DIALOG_DATA, { optional: true }) as MachineDto | null;

  code = '';
  name = '';
  productionLineId = '';
  statusId = '';
  originalStatusId = '';
  statuses: Status[] = [];
  lines: { id: string; code: string; name: string }[] = [];

  constructor() {
    this.api.get<{ items: Status[] }>('/api/machine-statuses?page=0&size=50').subscribe(r => {
      if (r.success) {
        this.statuses = r.data.items;
        if (this.statuses.length > 0 && !this.statusId) this.statusId = this.statuses[0].id;
      }
    });
    this.api.get<{ items: { id: string; code: string; name: string }[] }>('/api/lines?page=0&size=50').subscribe(r => {
      if (r.success) {
        this.lines = r.data.items;
        if (this.lines.length > 0 && !this.productionLineId) this.productionLineId = this.lines[0].id;
      }
    });
    if (this.data) {
      this.code = this.data.code;
      this.name = this.data.name;
      const line = (this.data as any).productionLine;
      this.productionLineId = line?.id || this.data.productionLineId || '';
      this.statusId = (this.data as any).machineStatus?.id || this.data.machineStatusId || '';
      this.originalStatusId = this.statusId;
    }
  }

  save() {
    const body = { code: this.code, name: this.name, productionLineId: this.productionLineId, machineStatusId: this.statusId };
    const req = this.data
      ? this.api.put(`/api/machines/${this.data.id}`, { name: this.name, productionLineId: this.productionLineId })
      : this.api.post('/api/machines', body);
    req.subscribe({
      next: (r: any) => {
        if (r.success) {
          if (this.data && this.statusId !== this.originalStatusId) {
            this.api.patch(`/api/machines/${this.data.id}/status`, { statusId: this.statusId }).subscribe({
              next: () => { this.snackBar.open('Updated', 'OK', { duration: 2000 }); this.dialogRef.close(true); },
              error: (e: any) => this.snackBar.open(e?.error?.message || 'Error updating status', 'OK', { duration: 4000 })
            });
          } else {
            this.snackBar.open(this.data ? 'Updated' : 'Created', 'OK', { duration: 2000 });
            this.dialogRef.close(true);
          }
        }
      },
      error: (e: any) => this.snackBar.open(e?.error?.message || 'Error saving Machine', 'OK', { duration: 4000 })
    });
  }
}
