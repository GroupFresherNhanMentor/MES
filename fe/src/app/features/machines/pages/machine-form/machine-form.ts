import { Component, inject } from '@angular/core';
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
import type { MachineDto } from '../../../../core/models/machine.model';

@Component({
  selector: 'app-machine-form',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule, MatTooltipModule, MatDialogModule, MatSnackBarModule],
  template: `
  <h2 mat-dialog-title>{{ data ? 'Edit' : 'Add' }} Machine</h2>
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
        <mat-label>Production Line</mat-label>
        <mat-select [(ngModel)]="productionLineId" name="lineId" required [disabled]="!!data">
          @for (l of lines; track l.id) { <mat-option [value]="l.id">{{ l.code }} - {{ l.name }}</mat-option> }
        </mat-select>
      </mat-form-field>
    </div>
  </mat-dialog-content>
  <mat-dialog-actions align="end">
    <button mat-button mat-dialog-close>Cancel</button>
    <button mat-raised-button class="ff-btn-primary" (click)="save()" [disabled]="loading || !code || !name || !productionLineId">Save</button>
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
  lines: { id: string; code: string; name: string }[] = [];
  loading = true;

  genCode() {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    const rand = Array.from({ length: 8 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
    this.code = `MCH-${rand}`;
  }

  constructor() {
    this.api.get<{ items: { id: string; code: string; name: string }[] }>('/api/lines?page=0&size=50').subscribe({
      next: r => {
        if (r.success) {
          this.lines = r.data.items;
          if (this.lines.length > 0 && !this.productionLineId) this.productionLineId = this.lines[0].id;
        }
      },
      complete: () => { this.loading = false; }
    });
    if (this.data) {
      this.code = this.data.code;
      this.name = this.data.name;
      const line = (this.data as any).productionLine;
      this.productionLineId = line?.id || this.data.productionLineId || '';
    }
  }

  save() {
    if (this.data) {
      this.api.put(`/api/machines/${this.data.id}`, { name: this.name }).subscribe({
        next: (r: any) => { if (r.success) { this.snackBar.open('Updated', 'OK', { duration: 2000 }); this.dialogRef.close(true); } },
        error: (e: any) => this.snackBar.open(e?.error?.message || 'Error updating machine', 'OK', { duration: 4000 })
      });
    } else {
      this.api.post('/api/machines', { code: this.code, name: this.name, productionLineId: this.productionLineId }).subscribe({
        next: (r: any) => { if (r.success) { this.snackBar.open('Created', 'OK', { duration: 2000 }); this.dialogRef.close(true); } },
        error: (e: any) => this.snackBar.open(e?.error?.message || 'Error creating machine', 'OK', { duration: 4000 })
      });
    }
  }
}
