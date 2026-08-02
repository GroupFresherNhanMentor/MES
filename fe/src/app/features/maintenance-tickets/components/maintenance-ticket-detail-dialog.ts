import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { ApiService } from '../../../core/services/api';
import type { MaintenanceTicketDto } from '../../../core/models/maintenance-ticket.model';

interface TicketDetailDialogData { ticket: MaintenanceTicketDto; }
interface DowntimeDetail { id?: string; ticketId?: string; machineId?: string; startTime?: string; endTime?: string; totalDowntimeMinutes?: number; rootCause?: string; actionTaken?: string; }

@Component({
  selector: 'app-maintenance-ticket-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatCardModule],
  template: `
    <h2 mat-dialog-title>Ticket Details</h2>
    <mat-dialog-content class="flex flex-col gap-4">
      <mat-card>
        <mat-card-content>
          <div class="grid grid-cols-2 gap-3 text-sm">
            <div><strong>Code</strong><div>{{ ticket.ticketCode }}</div></div>
            <div><strong>Status</strong><div>{{ ticket.status }}</div></div>
            <div><strong>Machine</strong><div>{{ ticket.machineName || ticket.machineCode || '-' }}</div></div>
            <div><strong>Priority</strong><div>{{ ticket.priority }}</div></div>
            <div><strong>Title</strong><div>{{ ticket.title }}</div></div>
            <div><strong>Assigned</strong><div>{{ ticket.assignedToName || '-' }}</div></div>
            <div><strong>Description</strong><div>{{ ticket.description || '-' }}</div></div>
            <div><strong>Reported At</strong><div>{{ ticket.reportedAt | date:'medium' }}</div></div>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-content>
          <div class="flex items-center justify-between mb-2">
            <h3 class="m-0">Downtime</h3>
            <span *ngIf="loading()" class="text-sm text-gray-400">Loading...</span>
          </div>
          <div *ngIf="downtime(); else empty">
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div><strong>Start</strong><div>{{ downtime()!.startTime | date:'medium' }}</div></div>
              <div><strong>End</strong><div>{{ downtime()!.endTime | date:'medium' }}</div></div>
              <div><strong>Minutes</strong><div>{{ downtime()!.totalDowntimeMinutes || 0 }}</div></div>
              <div><strong>Root Cause</strong><div>{{ downtime()!.rootCause || '-' }}</div></div>
              <div><strong>Action Taken</strong><div>{{ downtime()!.actionTaken || '-' }}</div></div>
            </div>
          </div>
          <ng-template #empty>
            <div class="text-sm text-gray-400">No downtime record found for this ticket.</div>
          </ng-template>
        </mat-card-content>
      </mat-card>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="dialogRef.close()">Close</button>
    </mat-dialog-actions>
  `,
})
export class MaintenanceTicketDetailDialog {
  private api = inject(ApiService);
  readonly dialogRef = inject(MatDialogRef<MaintenanceTicketDetailDialog>);
  readonly data = inject<TicketDetailDialogData>(MAT_DIALOG_DATA);
  readonly ticket = this.data.ticket;
  readonly downtime = signal<DowntimeDetail | null>(null);
  readonly loading = signal(false);

  constructor() {
    this.loadDowntime();
  }

  private loadDowntime(): void {
    this.loading.set(true);
    this.api.get<any>(`/api/maintenance/${this.ticket.id}/downtime`).subscribe({
      next: (res) => {
        this.loading.set(false);
        const payload = res?.data && typeof res.data === 'object' ? res.data : null;
        this.downtime.set(payload);
      },
      error: () => {
        this.loading.set(false);
        this.downtime.set(null);
      },
    });
  }
}
