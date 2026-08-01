import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../core/services/api';

interface MaintenanceTicketActionDialogData {
  ticketId: string;
  action: 'start' | 'cancel' | 'resolve' | 'close';
}

@Component({
  selector: 'app-maintenance-ticket-action-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title>{{ title() }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 pt-2">
        <mat-form-field *ngIf="action === 'start'" appearance="outline">
          <mat-label>Assigned Engineer</mat-label>
          <input matInput formControlName="assignedEngineerId" />
        </mat-form-field>

        <mat-form-field *ngIf="action === 'close'" appearance="outline">
          <mat-label>Action Taken</mat-label>
          <textarea matInput rows="3" formControlName="actionTaken"></textarea>
        </mat-form-field>

        <mat-form-field *ngIf="action === 'close'" appearance="outline">
          <mat-label>Root Cause</mat-label>
          <textarea matInput rows="3" formControlName="rootCause"></textarea>
        </mat-form-field>

        <mat-form-field *ngIf="action === 'close'" appearance="outline">
          <mat-label>Machine Resolution Status</mat-label>
          <mat-select formControlName="machineResolutionStatus">
            <mat-option value="AVAILABLE">AVAILABLE</mat-option>
            <mat-option value="DOWN">DOWN</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field *ngIf="action === 'close'" appearance="outline">
          <mat-label>Note</mat-label>
          <textarea matInput rows="2" formControlName="note"></textarea>
        </mat-form-field>

        <p *ngIf="errorMessage()" class="text-sm text-red-500">{{ errorMessage() }}</p>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="onSubmit()" [disabled]="loading()">Confirm</button>
    </mat-dialog-actions>
  `,
})
export class MaintenanceTicketActionDialog {
  private dialogRef = inject(MatDialogRef<MaintenanceTicketActionDialog>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);
  private data = inject<MaintenanceTicketActionDialogData>(MAT_DIALOG_DATA);

  readonly action = this.data.action;
  readonly title = signal(this.action === 'start' ? 'Start Ticket' : this.action === 'cancel' ? 'Cancel Ticket' : this.action === 'resolve' ? 'Resolve Ticket' : 'Close Ticket');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  form = this.fb.group({
    assignedEngineerId: [''],
    actionTaken: [''],
    rootCause: [''],
    machineResolutionStatus: ['AVAILABLE'],
    note: [''],
  });

  onSubmit(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    const url = `/api/maintenance/${this.data.ticketId}/${this.action}`;
    const payload = this.buildPayload();

    const request$ = this.action === 'close'
      ? this.api.post<any>(url, payload)
      : this.action === 'start'
        ? this.api.post<any>(url, payload)
        : this.api.post<any>(url);

    request$.subscribe({
      next: (res: { success: boolean; data?: unknown; message?: string }) => {
        this.loading.set(false);
        if (res.success) {
          this.snackBar.open('Action completed', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
          return;
        }
        this.errorMessage.set(res.message || 'Action failed');
      },
      error: (err: any) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Unable to complete action');
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  private buildPayload(): any {
    if (this.action === 'start') {
      return { assignedEngineerId: this.form.value.assignedEngineerId || '00000000-0000-0000-0000-000000000001' };
    }

    if (this.action === 'close') {
      return {
        actionTaken: this.form.value.actionTaken || '',
        rootCause: this.form.value.rootCause || '',
        machineResolutionStatus: this.form.value.machineResolutionStatus || 'AVAILABLE',
        note: this.form.value.note || '',
      };
    }

    return {};
  }
}
