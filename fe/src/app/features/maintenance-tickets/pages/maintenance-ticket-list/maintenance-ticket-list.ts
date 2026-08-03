import { Component, inject, signal, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { MaintenanceTicketDto } from '../../../../core/models/maintenance-ticket.model';
import { MaintenanceTicketCreateDialog } from '../../components/maintenance-ticket-create-dialog';
import { MaintenanceTicketDetailDialog } from '../../components/maintenance-ticket-detail-dialog';
import { MaintenanceTicketActionDialog } from '../../components/maintenance-ticket-action-dialog';

@Component({
  selector: 'app-maintenance-ticket-list',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule,
    MatTableModule, 
    MatButtonModule, 
    MatIconModule, 
    MatCardModule, 
    MatPaginatorModule, 
    MatDialogModule, 
    MatSnackBarModule, 
    MatTooltipModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule
  ],
  template: `
    <div class="p-4">
      <mat-card class="ff-card">
        <mat-card-content>
          <!-- HEADER ROW: Chứa Title, Nút Create và Thanh tìm kiếm + Bộ lọc -->
          <div class="flex flex-wrap items-center justify-between gap-4 pb-4 border-b border-gray-800 mb-4">
            <div class="flex items-center gap-4">
              <h1 class="text-xl font-semibold m-0 style-title" style="color: #fff;">Maintenance Tickets</h1>
              @if (canManage) {
              <button mat-flat-button color="primary" style="background-color: #e65100;" (click)="openCreateDialog()">
                <mat-icon>add</mat-icon> Create Ticket
              </button>
              }
            </div>

            <!-- SEARCH & FILTERS BLOCK -->
            <div class="flex flex-wrap items-center gap-3">
              <!-- Ô tìm kiếm Input Text -->
              <mat-form-field appearance="outline" class="min-w-[220px] ff-dense-field">
                <mat-label>Search Title or Machine...</mat-label>
                <input matInput [ngModel]="searchQuery()" (ngModelChange)="searchQuery.set($event)" placeholder="Type to search...">
                <mat-icon matSuffix class="text-gray-400">search</mat-icon>
              </mat-form-field>

              <!-- Bộ lọc Priority (Thay thế cho Machine cũ) -->
              <mat-form-field appearance="outline" class="min-w-[150px] ff-dense-field">
                <mat-label>Priority</mat-label>
                <mat-select [ngModel]="selectedPriority()" (ngModelChange)="selectedPriority.set($event)">
                  <mat-option value="ALL">All Priorities</mat-option>
                  <mat-option value="CRITICAL">CRITICAL</mat-option>
                  <mat-option value="HIGH">HIGH</mat-option>
                  <mat-option value="MEDIUM">MEDIUM</mat-option>
                  <mat-option value="LOW">LOW</mat-option>
                </mat-select>
              </mat-form-field>

              <!-- Bộ lọc Status -->
              <mat-form-field appearance="outline" class="min-w-[150px] ff-dense-field">
                <mat-label>Status</mat-label>
                <mat-select [ngModel]="selectedStatus()" (ngModelChange)="selectedStatus.set($event)">
                  <mat-option value="ALL">All Statuses</mat-option>
                  <mat-option value="OPEN">OPEN</mat-option>
                  <mat-option value="IN_PROGRESS">IN_PROGRESS</mat-option>
                  <mat-option value="RESOLVED">RESOLVED</mat-option>
                  <mat-option value="CLOSED">CLOSED</mat-option>
                  <mat-option value="CANCELLED">CANCELLED</mat-option>
                </mat-select>
              </mat-form-field>

              <!-- Nút Xoá nhanh Bộ Lọc -->
              <button mat-icon-button color="warn" matTooltip="Clear all filters" 
                      *ngIf="selectedPriority() !== 'ALL' || selectedStatus() !== 'ALL' || searchQuery()"
                      (click)="clearFilters()">
                <mat-icon>filter_list_off</mat-icon>
              </button>
            </div>
          </div>

          <!-- TABLE: Nguồn dữ liệu lấy từ computed signal filteredItems() -->
          <table mat-table [dataSource]="filteredItems()" class="full-width">
            <ng-container matColumnDef="code">
              <th mat-header-cell *matHeaderCellDef>CODE</th>
              <td mat-cell *matCellDef="let t">{{ t.ticketCode }}</td>
            </ng-container>
            <ng-container matColumnDef="title">
              <th mat-header-cell *matHeaderCellDef>TITLE</th>
              <td mat-cell *matCellDef="let t">{{ t.title }}</td>
            </ng-container>
            <ng-container matColumnDef="machine">
              <th mat-header-cell *matHeaderCellDef>MACHINE</th>
              <td mat-cell *matCellDef="let t">{{ t.machineName || t.machineCode || '-' }}</td>
            </ng-container>
            <ng-container matColumnDef="priority">
              <th mat-header-cell *matHeaderCellDef>PRIORITY</th>
              <td mat-cell *matCellDef="let t">
                <span class="ff-badge" 
                      [class.ff-badge--cancelled]="t.priority === 'CRITICAL' || t.priority === 'HIGH'" 
                      [class.ff-badge--on-hold]="t.priority === 'MEDIUM'" 
                      [class.ff-badge--completed]="t.priority === 'LOW'">{{ t.priority }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>STATUS</th>
              <td mat-cell *matCellDef="let t">
                <span class="ff-badge" 
                      [class.ff-badge--active]="t.status === 'IN_PROGRESS'" 
                      [class.ff-badge--completed]="t.status === 'CLOSED' || t.status === 'RESOLVED'" 
                      [class.ff-badge--pending]="t.status === 'OPEN'">{{ t.status }}</span>
              </td>
            </ng-container>
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>ACTION</th>
              <td mat-cell *matCellDef="let t">
                <div class="flex flex-wrap gap-2">
                  <button mat-icon-button color="primary" matTooltip="View detail" (click)="openDetailDialog(t)">
                    <mat-icon>visibility</mat-icon>
                  </button>
                  @if (canManage) {
                  <button *ngIf="t.status === 'OPEN'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'start')">Start</button>
                  <button *ngIf="t.status === 'OPEN'" mat-stroked-button color="warn" (click)="openActionDialog(t, 'cancel')">Cancel</button>
                  <button *ngIf="t.status === 'IN_PROGRESS'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'resolve')">Resolve</button>
                  <button *ngIf="t.status === 'RESOLVED'" mat-stroked-button color="primary" (click)="openActionDialog(t, 'close')">Close</button>
                  }
                </div>
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>

          <div *ngIf="filteredItems().length === 0" class="text-center py-8 text-gray-400">
            <mat-icon class="text-4xl h-10 w-10 mb-2">build_circle</mat-icon>
            <p>No maintenance tickets match the selected criteria.</p>
          </div>

          <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .ff-dense-field ::v-deep .mat-mdc-form-field-wrapper {
      padding-bottom: 0 !important;
    }
  `]
})
export class MaintenanceTicketList {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  canManage = this.auth.getCurrentUser()?.role === 'MAINTENANCE_ENGINEER';
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  // Core Data State Signals
  items = signal<MaintenanceTicketDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'title', 'machine', 'priority', 'status', 'actions'];

  // Search & Filter State Signals
  searchQuery = signal<string>('');
  selectedPriority = signal<string>('ALL');
  selectedStatus = signal<string>('ALL');

  // Computed Signal: Tự động chạy bộ lọc & Tìm kiếm real-time cực mượt trên Client
  filteredItems = computed(() => {
    let list = this.items();
    const query = this.searchQuery().trim().toLowerCase();
    const priorityFilter = this.selectedPriority();
    const statusFilter = this.selectedStatus();

    // 1. Thực hiện tìm kiếm theo Title hoặc Machine Name / Machine Code
    if (query) {
      list = list.filter(t => 
        (t.title && t.title.toLowerCase().includes(query)) ||
        (t.machineName && t.machineName.toLowerCase().includes(query)) ||
        (t.machineCode && t.machineCode.toLowerCase().includes(query))
      );
    }

    // 2. Lọc theo Priority
    if (priorityFilter !== 'ALL') {
      list = list.filter(t => t.priority === priorityFilter);
    }

    // 3. Lọc theo Status
    if (statusFilter !== 'ALL') {
      list = list.filter(t => t.status === statusFilter);
    }

    return list;
  });

  constructor() {
    // Tự động kích hoạt tải lại dữ liệu tổng khi đổi phân trang
    effect(() => {
      this.load();
    }, { allowSignalWrites: true });
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

  clearFilters() {
    this.searchQuery.set('');
    this.selectedPriority.set('ALL');
    this.selectedStatus.set('ALL');
  }

  onPage(e: PageEvent) {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
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