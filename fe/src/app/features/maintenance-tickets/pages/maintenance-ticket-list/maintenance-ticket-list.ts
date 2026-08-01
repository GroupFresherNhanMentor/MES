import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { MaintenanceTicketDto } from '../../../../core/models/maintenance-ticket.model';
import { MaintenanceTicketCreateDialog } from '../../components/maintenance-ticket-create-dialog';
import { MaintenanceTicketDetailDialog } from '../../components/maintenance-ticket-detail-dialog';
import { MaintenanceTicketActionDialog } from '../../components/maintenance-ticket-action-dialog';

@Component({
  selector: 'app-maintenance-ticket-list',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule, MatDialogModule, MatSnackBarModule, MatTooltipModule],
  template: `
    <div class="page-header">
      <div>
        <h1 class="page-heading">Maintenance Tickets</h1>
        <p class="page-subtitle">Latest tickets are shown first</p>
      </div>
      <button mat-flat-button color="primary" (click)="openCreateDialog()">Create Ticket</button>
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
            <td mat-cell *matCellDef="let t">{{ t.machineName || t.machineCode || '-' }}</td>
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
              <span class="ff-badge" [class.ff-badge--active]="t.status === 'IN_PROGRESS'" [class.ff-badge--completed]="t.status === 'CLOSED' || t.status === 'RESOLVED'" [class.ff-badge--pending]="t.status === 'OPEN'">{{ t.status }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let t">
              <div class="flex flex-wrap gap-2">
                <button mat-icon-button color="primary" matTooltip="View detail" (click)="openDetailDialog(t)">
                  <mat-icon>visibility</mat-icon>
                </button>
                <button *ngIf="t.status === 'OPEN'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'start')">Start</button>
                <button *ngIf="t.status === 'OPEN'" mat-stroked-button color="warn" (click)="openActionDialog(t, 'cancel')">Cancel</button>
                <button *ngIf="t.status === 'IN_PROGRESS'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'resolve')">Resolve</button>
                <button *ngIf="t.status === 'RESOLVED'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'close')">Close</button>
              </div>
            </td>
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
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<MaintenanceTicketDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'title', 'machine', 'priority', 'status', 'actions'];

  constructor() {
    this.load();
  }

  load() {
    const url = `${API.maintenanceTickets.base}?page=${this.page() + 1}&size=${this.size()}`;
    this.api.get<any>(url).subscribe((ticketsRes) => {
      const pagePayload = this.unwrapPage<any>(ticketsRes);
      const rawItems = pagePayload.items || [];

      this.api.get<any>(API.maintenanceTickets.ticketStatuses).subscribe((statusesRes) => {
        const statuses = this.toNameMap(statusesRes);
        this.api.get<any>(API.maintenanceTickets.ticketPriorities).subscribe((prioritiesRes) => {
          const priorities = this.toNameMap(prioritiesRes);
          this.api.get<any>(`${API.machines.base}?page=0&size=100`).subscribe((machinesRes) => {
            const machines = this.toMachineNameMap(machinesRes);
            const normalized = rawItems.map((item: any) => ({
              id: item.id,
              ticketCode: item.ticketCode || item.id,
              machineId: item.machineId || '',
              machineName: machines.get(item.machineId) || item.machineName || '',
              machineCode: item.machineCode || '',
              title: item.title || item.description || 'Maintenance Ticket',
              description: item.description || '',
              priority: (priorities.get(item.priorityId || item.priority) || item.priority || 'MEDIUM') as any,
              status: (statuses.get(item.ticketStatusId || item.status) || item.status || 'OPEN') as any,
              reportedBy: item.reportedBy || item.createdBy || '',
              reportedByName: item.reportedByName || '',
              assignedTo: item.assignedTo || '',
              assignedToName: item.assignedToName || '',
              reportedAt: item.reportedAt || item.createdAt || '',
              startedAt: item.startedAt || null,
              closedAt: item.closedAt || null,
              resolution: item.resolution || '',
              downtimeMinutes: item.downtimeMinutes ?? null,
              createdAt: item.createdAt || item.reportedAt || '',
              updatedAt: item.updatedAt || item.createdAt || '',
            }));

            this.items.set(normalized.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
            this.total.set(pagePayload.totalElements || 0);
          });
        });
      });
    });
  }

  onPage(e: PageEvent) {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }

  openCreateDialog(): void {
    this.dialog.open(MaintenanceTicketCreateDialog, { width: '560px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe((success) => {
      if (success) this.load();
    });
  }

  openDetailDialog(ticket: MaintenanceTicketDto): void {
    this.dialog.open(MaintenanceTicketDetailDialog, { width: '720px', panelClass: 'ff-dialog-panel', data: { ticket } });
  }

  openActionDialog(ticket: MaintenanceTicketDto, action: 'start' | 'cancel' | 'resolve' | 'close'): void {
    this.dialog.open(MaintenanceTicketActionDialog, { width: '460px', panelClass: 'ff-dialog-panel', data: { ticketId: ticket.id, action } }).afterClosed().subscribe((success) => {
      if (success) {
        this.snackBar.open('Ticket updated', 'OK', { duration: 2000 });
        this.load();
      }
    });
  }

  private unwrapPage<T>(response: any): { items: T[]; totalElements: number } {
    const body = this.unwrapBody<any>(response);
    const items = Array.isArray(body) ? body : (body?.items || []);
    return { items, totalElements: Number(body?.totalElements ?? 0) };
  }

  private unwrapBody<T>(response: any): T {
    if (response && typeof response === 'object' && 'data' in response) {
      return response.data as T;
    }
    return response as T;
  }

  private toNameMap(response: any): Map<string, string> {
    const body = this.unwrapBody<any>(response);
    const list = Array.isArray(body) ? body : (body?.items || []);
    const map = new Map<string, string>();
    list.forEach((item: any) => {
      if (item?.id) map.set(item.id, item.name || item.code || item.id);
    });
    return map;
  }

  private toMachineNameMap(response: any): Map<string, string> {
    const body = this.unwrapBody<any>(response);
    const list = Array.isArray(body) ? body : (body?.items || []);
    const map = new Map<string, string>();
    list.forEach((item: any) => {
      if (item?.id) map.set(item.id, item.name || item.code || item.id);
    });
    return map;
  }
}
