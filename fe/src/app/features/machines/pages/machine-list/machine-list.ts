import { Component, inject, signal } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import type { MachineDto } from '../../../../core/models/machine.model';
import { MACHINE_STATUSES } from '../../../../configs/constants';

@Component({
  selector: 'app-machine-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatMenuModule,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  items = signal<MachineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatus = signal('');

  statuses = MACHINE_STATUSES;
  displayedColumns = ['code', 'name', 'line', 'status', 'actions'];

  constructor() {
    this.load();
  }

  load() {
    let url = `/api/machines?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatus()) url += `&statusName=${encodeURIComponent(this.filterStatus())}`;
    this.api.get<{ items: MachineDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  deactivate(m: MachineDto) {
    this.api.put(`/api/machines/${m.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Machine deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  changeStatus(m: MachineDto, newStatus: string) {
    this.api.patch(`/api/machines/${m.id}/status`, { status: newStatus }).subscribe(r => {
      if (r.success) { this.snackBar.open(`Status changed to ${newStatus}`, 'OK', { duration: 2000 }); this.load(); }
    });
  }

  isActive(statusName: string) { return statusName !== 'INACTIVE'; }

  getStatusClass(name: string): string {
    const map: Record<string, string> = {
      'RUNNING': 'ff-badge--running',
      'IDLE': 'ff-badge--idle',
      'MAINTENANCE': 'ff-badge--onhold',
      'BREAKDOWN': 'ff-badge--cancelled',
      'INACTIVE': 'ff-badge--cancelled',
    };
    return map[name] || '';
  }
}
