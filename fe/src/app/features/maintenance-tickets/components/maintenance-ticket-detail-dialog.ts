import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { ApiService } from '../../../core/services/api';
import type { MaintenanceTicketDto } from '../../../core/models/maintenance-ticket.model';

interface TicketDetailDialogData { ticket: MaintenanceTicketDto; }
interface DowntimeDetail { id?: string; ticketId?: string; machineId?: string; startTime?: string; endTime?: string; totalDowntimeMinutes?: number; rootCause?: string; actionTaken?: string; }

@Component({
  selector: 'app-maintenance-ticket-detail-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <h2 mat-dialog-title class="border-b border-gray-800 pb-2 style-title" style="color: #fff;">Ticket Details</h2>
    <mat-dialog-content class="flex flex-col gap-4 pt-4 min-w-[550px]">
      
      <!-- SECTION 1: GENERAL INFORMATION -->
      <mat-card class="bg-gray-900 border border-gray-800">
        <mat-card-content class="p-4">
          <!-- Title Section -->
          <div class="border-b border-gray-800 pb-2 mb-3 flex items-center gap-2">
            <mat-icon color="primary" style="font-size: 20px; width: 20px; height: 20px;">info</mat-icon>
            <span class="font-semibold text-gray-200 uppercase tracking-wider text-xs">General Info</span>
          </div>

          <!-- Info List using Flexbox layout with clean ': ' separator -->
          <div class="flex flex-col gap-2.5 text-sm">
            <div class="flex items-start">
              <span class="text-gray-400 font-medium min-w-[130px]">Code:</span>
              <span class="text-gray-100 font-mono bg-gray-950 px-2 py-0.5 rounded border border-gray-800 break-all select-all">{{ ticket.ticketCode }}</span>
            </div>

            <div class="flex items-center">
              <span class="text-gray-400 font-medium min-w-[130px]">Machine:</span>
              <span class="text-gray-200 font-semibold">{{ ticket.machineName || ticket.machineCode || '-' }}</span>
            </div>

            <div class="flex items-center">
              <span class="text-gray-400 font-medium min-w-[130px]">Status:</span>
              <span class="ff-badge mt-0" 
                    [class.ff-badge--active]="$any(ticket.status) === 'IN_PROGRESS'" 
                    [class.ff-badge--completed]="$any(ticket.status) === 'CLOSED' || $any(ticket.status) === 'RESOLVED'" 
                    [class.ff-badge--pending]="$any(ticket.status) === 'OPEN'">
                {{ ticket.status }}
              </span>
            </div>

            <div class="flex items-center">
              <span class="text-gray-400 font-medium min-w-[130px]">Priority:</span>
              <span class="ff-badge mt-0" 
                    [class.ff-badge--cancelled]="$any(ticket.priority) === 'CRITICAL' || $any(ticket.priority) === 'HIGH'" 
                    [class.ff-badge--on-hold]="$any(ticket.priority) === 'MEDIUM'" 
                    [class.ff-badge--completed]="$any(ticket.priority) === 'LOW'">
                {{ ticket.priority }}
              </span>
            </div>

            <div class="flex items-start">
              <span class="text-gray-400 font-medium min-w-[130px]">Title:</span>
              <span class="text-gray-100 font-medium flex-1 break-words">{{ ticket.title }}</span>
            </div>

            <div class="flex items-center">
              <span class="text-gray-400 font-medium min-w-[130px]">Assigned Engineer:</span>
              <span class="text-gray-200">{{ ticket.assignedToName || '-' }}</span>
            </div>

            <div class="flex flex-col gap-1 bg-gray-950 p-2.5 rounded border border-gray-800 mt-1">
              <span class="text-gray-400 font-medium text-xs uppercase tracking-wider">Description:</span>
              <span class="text-gray-300 whitespace-pre-wrap pl-1">{{ ticket.description || '-' }}</span>
            </div>

            <div class="text-right mt-2 pt-2 border-t border-gray-800/50">
              <span class="text-xs text-gray-500">Reported At: {{ ticket.reportedAt | date:'medium' }}</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- SECTION 2: DOWNTIME METRICS -->
      <mat-card class="bg-gray-900 border border-gray-800">
        <mat-card-content class="p-4">
          <div class="flex items-center justify-between border-b border-gray-800 pb-2 mb-3">
            <div class="flex items-center gap-2">
              <mat-icon color="warn" style="font-size: 20px; width: 20px; height: 20px;">history_toggle_off</mat-icon>
              <span class="font-semibold text-gray-200 uppercase tracking-wider text-xs">Downtime Details</span>
            </div>
            <span *ngIf="loading()" class="text-xs text-gray-400 animate-pulse">Loading data...</span>
          </div>

          <div *ngIf="downtime(); else empty">
            <div class="flex flex-col gap-2.5 text-sm">
              <div class="flex items-center">
                <span class="text-gray-400 font-medium min-w-[130px]">Start Time:</span>
                <span class="text-gray-200">{{ downtime()!.startTime | date:'medium' }}</span>
              </div>
              
              <div class="flex items-center">
                <span class="text-gray-400 font-medium min-w-[130px]">End Time:</span>
                <span class="text-gray-200">{{ downtime()!.endTime ? (downtime()!.endTime | date:'medium') : 'Still Down' }}</span>
              </div>
              
              <div class="flex items-center">
                <span class="text-gray-400 font-medium min-w-[130px]">Total Downtime:</span>
                <span class="text-sm font-semibold text-orange-500 bg-orange-950/40 border border-orange-900/60 px-2 py-0.5 rounded inline-block">
                  {{ downtime()!.totalDowntimeMinutes || 0 }} minutes
                </span>
              </div>
              
              <div class="flex flex-col gap-1 bg-gray-950 p-2.5 rounded border border-gray-800 mt-1">
                <span class="text-gray-400 font-medium text-xs uppercase tracking-wider">Root Cause:</span>
                <span class="text-gray-300 block pl-1">{{ downtime()!.rootCause || '-' }}</span>
              </div>
              
              <div class="flex flex-col gap-1 bg-gray-950 p-2.5 rounded border border-gray-800">
                <span class="text-gray-400 font-medium text-xs uppercase tracking-wider">Action Taken:</span>
                <span class="text-gray-300 block pl-1">{{ downtime()!.actionTaken || '-' }}</span>
              </div>
            </div>
          </div>
          
          <ng-template #empty>
            <div class="flex flex-col items-center justify-center py-4 text-center text-gray-500">
              <mat-icon style="font-size: 28px; width: 28px; height: 28px;" class="mb-1 opacity-40">assignment_turned_in</mat-icon>
              <p class="text-xs m-0">No active downtime impact record for this operational ticket.</p>
            </div>
          </ng-template>
        </mat-card-content>
      </mat-card>

    </mat-dialog-content>
    <mat-dialog-actions align="end" class="border-t border-gray-800 mt-2 pt-2">
      <button mat-flat-button color="primary" style="background-color: #e65100;" (click)="dialogRef.close()">Close</button>
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