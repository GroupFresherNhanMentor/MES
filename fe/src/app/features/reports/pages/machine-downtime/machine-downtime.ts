import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ReportService, MachineDowntimeRow } from '../../services/report.service';

@Component({
  selector: 'app-machine-downtime',
  standalone: true,
  imports: [
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <div class="flex flex-wrap items-center justify-between gap-4 mb-4">
      <div class="flex items-center gap-3">
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
          <mat-label>From Date</mat-label>
          <input matInput type="date" [(ngModel)]="fromDate" (change)="load()">
        </mat-form-field>
        <mat-form-field appearance="outline" subscriptSizing="dynamic" class="w-44">
          <mat-label>To Date</mat-label>
          <input matInput type="date" [(ngModel)]="toDate" (change)="load()">
        </mat-form-field>
        <button mat-raised-button class="ff-btn-primary" (click)="load()">
          <mat-icon>filter_alt</mat-icon> Filter
        </button>
      </div>
    </div>

    @if (items().length > 0) {
      <table mat-table [dataSource]="items()" class="w-full">
        <ng-container matColumnDef="machineCode">
          <th mat-header-cell *matHeaderCellDef>Machine Code</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">{{ r.machineCode }}</td>
        </ng-container>
        <ng-container matColumnDef="machineName">
          <th mat-header-cell *matHeaderCellDef>Machine Name</th>
          <td mat-cell *matCellDef="let r">{{ r.machineName }}</td>
        </ng-container>
        <ng-container matColumnDef="downtimeMinutes">
          <th mat-header-cell *matHeaderCellDef class="text-right">Total Downtime (Mins)</th>
          <td mat-cell *matCellDef="let r" class="text-right font-bold" [class.text-error]="r.totalDowntimeMinutes > 0" [class.text-success]="r.totalDowntimeMinutes === 0">
            {{ r.totalDowntimeMinutes }} mins
          </td>
        </ng-container>
        <ng-container matColumnDef="ticketCount">
          <th mat-header-cell *matHeaderCellDef class="text-right">Maintenance Tickets</th>
          <td mat-cell *matCellDef="let r" class="text-right font-medium">{{ r.maintenanceTicketCount }}</td>
        </ng-container>
        <ng-container matColumnDef="lastReason">
          <th mat-header-cell *matHeaderCellDef>Last Downtime Reason</th>
          <td mat-cell *matCellDef="let r" class="text-text-secondary italic">
            {{ r.lastDowntimeReason || 'N/A' }}
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40">build_circle</mat-icon>
        <span class="text-sm">No machine downtime records found</span>
      </div>
    }
  `,
})
export class MachineDowntimeComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<MachineDowntimeRow[]>([]);
  fromDate = '';
  toDate = '';

  displayedColumns = [
    'machineCode',
    'machineName',
    'downtimeMinutes',
    'ticketCount',
    'lastReason',
  ];

  ngOnInit() {
    this.load();
  }

  load() {
    const params: Record<string, any> = {};
    if (this.fromDate) params['fromDate'] = this.fromDate;
    if (this.toDate) params['toDate'] = this.toDate;

    this.reportService.getMachineDowntimes(params).subscribe((r) => {
      if (r.success && r.data) {
        this.items.set(r.data);
      }
    });
  }
}
