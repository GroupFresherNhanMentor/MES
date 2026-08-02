import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { ApiService } from '../../../core/services/api';
import { API } from '../../../configs/api-endpoints';

interface MaintenanceTicketActionDialogData {
  ticketId: string;
  action: 'start' | 'cancel' | 'resolve' | 'close';
}

interface EngineerOption {
  id: string;
  label: string;
  username: string;
}

@Component({
  selector: 'app-maintenance-ticket-action-dialog',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule, MatAutocompleteModule],
  template: `
    <h2 mat-dialog-title>{{ title() }}</h2>
    <mat-dialog-content>
      <form [formGroup]="form" class="flex flex-col gap-4 pt-2">
        <p *ngIf="action === 'cancel'" class="text-gray-300 text-sm m-0 pb-2">
          Are you sure you want to cancel this maintenance ticket? This action cannot be undone.
        </p>

        <mat-form-field *ngIf="action === 'start'" appearance="outline">
          <mat-label>Assigned Engineer</mat-label>
          <input
            matInput
            [formControl]="engineerSearchControl"
            [matAutocomplete]="engineerAuto"
            placeholder="Search engineer..."
            (input)="onEngineerSearch($event)"
          />
          <mat-autocomplete #engineerAuto="matAutocomplete" [displayWith]="displayEngineer" (optionSelected)="onEngineerSelected($event.option.value)">
            <mat-option *ngFor="let engineer of filteredEngineers" [value]="engineer">
              <span>{{ engineer.username }}</span>
            </mat-option>
            <mat-option *ngIf="filteredEngineers.length === 0" disabled>
              No maintenance engineers found
            </mat-option>
          </mat-autocomplete>
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
  readonly engineers = signal<EngineerOption[]>([]);
  readonly engineerSearchControl = this.fb.control('');

  form = this.fb.group({
    assignedEngineerId: ['', Validators.required],
    actionTaken: [''],
    rootCause: [''],
    machineResolutionStatus: ['AVAILABLE'],
    note: [''],
  });

  constructor() {
    if (this.action === 'start') {
      this.loadEngineers();
    }
  }

  get filteredEngineers(): EngineerOption[] {
    const term = (this.engineerSearchControl.value ?? '').trim().toLowerCase();
    if (!term) {
      return this.engineers();
    }

    return this.engineers().filter((engineer) =>
      engineer.label.toLowerCase().includes(term) || engineer.username.toLowerCase().includes(term)
    );
  }

  onSubmit(): void {
    if (this.action === 'start' && this.form.invalid) {
      this.form.markAllAsTouched();
      this.errorMessage.set('Please select an assigned engineer');
      return;
    }

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

  onEngineerSearch(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.engineerSearchControl.setValue(input.value, { emitEvent: false });
  }

  onEngineerSelected(engineer: EngineerOption | null): void {
    if (!engineer) {
      return;
    }

    this.form.get('assignedEngineerId')?.setValue(engineer.id);
    this.engineerSearchControl.setValue(engineer.label, { emitEvent: false });
  }

  displayEngineer(engineer: EngineerOption | string | null): string {
    if (!engineer) {
      return '';
    }

    if (typeof engineer === 'string') {
      return engineer;
    }

    return engineer.label;
  }

  private loadEngineers(): void {
    this.api.get<any>(API.maintenanceTickets.engineers).subscribe({
      next: (response) => {
        const body = this.unwrapBody<any[]>(response);
        const engineers = Array.isArray(body) ? body : [];

        const normalized = engineers
          .filter((engineer: any) => engineer?.id)
          .map((engineer: any) => ({
            id: engineer.id,
            label: engineer.username || engineer.fullName || engineer.id,
            username: engineer.username || '',
          } as EngineerOption))
          .sort((a, b) => a.username.localeCompare(b.username));

        this.engineers.set(normalized);
      },
      error: () => {
        this.engineers.set([]);
      },
    });
  }

  private unwrapBody<T>(response: any): T {
    if (response && typeof response === 'object' && 'data' in response) {
      return response.data as T;
    }
    return response as T;
  }

  private buildPayload(): any {
    if (this.action === 'start') {
      return { assignedEngineerId: this.form.value.assignedEngineerId };
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