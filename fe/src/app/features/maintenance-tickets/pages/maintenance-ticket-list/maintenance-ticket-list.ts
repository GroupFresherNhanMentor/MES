import { Component, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { MaintenanceTicketDto } from '../../../../core/models/maintenance-ticket.model';

@Component({
  selector: 'app-maintenance-ticket-list',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Maintenance Tickets</h1>
      <p class="page-subtitle">Track machine maintenance requests and work orders</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let t">{{ t.ticketCode }}</td>
          </ng-container>
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef>Title</th>
            <td mat-cell *matCellDef="let t">{{ t.title }}</td>
          </ng-container>
          <ng-container matColumnDef="machine">
            <th mat-header-cell *matHeaderCellDef>Machine</th>
            <td mat-cell *matCellDef="let t">{{ t.machineName }}</td>
          </ng-container>
          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef>Priority</th>
            <td mat-cell *matCellDef="let t">
              <span class="ff-badge" [class.ff-badge--cancelled]="t.priority === 'CRITICAL' || t.priority === 'HIGH'" [class.ff-badge--on-hold]="t.priority === 'MEDIUM'" [class.ff-badge--completed]="t.priority === 'LOW'">{{ t.priority }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let t">
              <span class="ff-badge" [class.ff-badge--active]="t.status === 'IN_PROGRESS'" [class.ff-badge--completed]="t.status === 'CLOSED'" [class.ff-badge--pending]="t.status === 'OPEN'">{{ t.status }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="assigned">
            <th mat-header-cell *matHeaderCellDef>Assigned To</th>
            <td mat-cell *matCellDef="let t">{{ t.assignedToName || '-' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class MaintenanceTicketList {
  private api = inject(ApiService);
  items = signal<MaintenanceTicketDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'title', 'machine', 'priority', 'status', 'assigned'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: MaintenanceTicketDto[]; totalElements: number }>(`/api/maintenance-tickets?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
