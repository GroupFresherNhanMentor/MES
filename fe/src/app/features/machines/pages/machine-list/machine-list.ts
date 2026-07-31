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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import type { MachineDto } from '../../../../core/models/machine.model';
import { MachineFormComponent } from '../machine-form/machine-form';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-machine-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatDialogModule, MatTooltipModule,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<MachineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatusId = signal('');
  statuses = signal<LookupEntry[]>([]);
  retiredId = '';
  availableId = '';

  displayedColumns = ['code', 'name', 'status', 'actions'];

  constructor() {
    this.api.get<{ items: LookupEntry[] }>('/api/machine-statuses?page=0&size=50').subscribe(r => {
      if (r.success) {
        this.statuses.set(r.data.items);
        this.retiredId = r.data.items.find(s => s.name === 'RETIRED')?.id || '';
        this.availableId = r.data.items.find(s => s.name === 'AVAILABLE')?.id || '';
      }
    });
    this.load();
  }

  load() {
    let url = `/api/machines?page=${this.page()}&size=${this.size()}`;
    if (this.filterStatusId()) url += `&statusId=${this.filterStatusId()}`;
    this.api.get<{ items: MachineDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    this.dialog.open(MachineFormComponent, { width: '500px' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(m: MachineDto) {
    this.dialog.open(MachineFormComponent, { width: '500px', data: m }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  isRetired(m: MachineDto): boolean {
    return (m.machineStatus?.name || m.machineStatusName || '').toUpperCase() === 'RETIRED';
  }

  toggleStatus(m: MachineDto) {
    const target = this.isRetired(m) ? this.availableId : this.retiredId;
    this.api.patch(`/api/machines/${m.id}/status`, { statusId: target }).subscribe(r => {
      if (r.success) { this.snackBar.open(this.isRetired(m) ? 'Deactivated' : 'Activated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  statusName(id: string) { return this.statuses().find(s => s.id === id)?.name ?? ''; }
}
