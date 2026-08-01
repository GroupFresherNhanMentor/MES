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

interface MachineOption { id: string; name?: string; code?: string; }
interface MetadataOption { id: string; name: string; description?: string; }

@Component({
  selector: 'app-maintenance-ticket-create-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule],
  template: `
    <h2 mat-dialog-title>Create Maintenance Ticket</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 pt-2">
        <mat-form-field appearance="outline">
          <mat-label>Machine</mat-label>
          <mat-select formControlName="machineId">
            <mat-option *ngFor="let machine of machines()" [value]="machine.id">{{ machine.name || machine.code || machine.id }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Ticket Type</mat-label>
          <mat-select formControlName="ticketTypeId">
            <mat-option *ngFor="let item of ticketTypes()" [value]="item.id">{{ item.name }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Priority</mat-label>
          <mat-select formControlName="priorityId">
            <mat-option *ngFor="let item of priorities()" [value]="item.id">{{ item.name }}</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Description</mat-label>
          <textarea matInput rows="4" formControlName="description"></textarea>
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
export class MaintenanceTicketCreateDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<MaintenanceTicketCreateDialog>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  machines = signal<MachineOption[]>([]);
  ticketTypes = signal<MetadataOption[]>([]);
  priorities = signal<MetadataOption[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    machineId: ['', Validators.required],
    ticketTypeId: ['', Validators.required],
    priorityId: ['', Validators.required],
    description: [''],
  });

  ngOnInit(): void {
    this.loadOptions();
  }

  private loadOptions(): void {
    this.api.get<any>(`${API.machines.base}?page=0&size=100`).subscribe({
      next: (res) => {
        const body = this.unwrapBody<any>(res);
        const items = Array.isArray(body) ? body : body?.items || [];
        this.machines.set(items.filter((item: any) => item && item.id));
      },
    });

    this.api.get<any>(`${API.maintenanceTickets.base}/ticket-types`).subscribe({
      next: (res) => {
        const body = this.unwrapBody<any>(res);
        const items = Array.isArray(body) ? body : body?.items || [];
        this.ticketTypes.set(items.filter((item: any) => item && item.id));
      },
    });

    this.api.get<any>(`${API.maintenanceTickets.base}/ticket-priorities`).subscribe({
      next: (res) => {
        const body = this.unwrapBody<any>(res);
        const items = Array.isArray(body) ? body : body?.items || [];
        this.priorities.set(items.filter((item: any) => item && item.id));
      },
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    this.api.post<any>(API.maintenanceTickets.base, this.form.getRawValue()).subscribe({
      next: (res) => {
        this.loading.set(false);
        if (res.success) {
          this.snackBar.open('Ticket created successfully', 'OK', { duration: 2000 });
          this.dialogRef.close(true);
          return;
        }
        this.errorMessage.set(res.message || 'Failed to create ticket');
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Unable to create ticket');
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  private unwrapBody<T>(response: any): T {
    if (response && typeof response === 'object' && 'data' in response) {
      return response.data as T;
    }
    return response as T;
  }
}
