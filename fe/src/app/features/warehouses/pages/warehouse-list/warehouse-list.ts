import { Component, inject, signal } from '@angular/core';
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
import type { WarehouseDto } from '../../../../core/models/warehouse.model';
import { WAREHOUSE_STATUSES } from '../../../../configs/constants';
import { WarehouseFormComponent } from '../warehouse-form/warehouse-form';

import { DatePipe, NgClass } from '@angular/common';

@Component({
  selector: 'app-warehouse-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatDialogModule, MatTooltipModule,
  ],
  templateUrl: './warehouse-list.html',
})
export class WarehouseList {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<WarehouseDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatus = signal('');

  statuses = WAREHOUSE_STATUSES;
  displayedColumns = ['code', 'name', 'address', 'status', 'createdAt', 'actions'];

  constructor() { this.load(); }

  load() {
    let url = `/api/warehouses?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatus()) url += `&statusName=${encodeURIComponent(this.filterStatus())}`;
    this.api.get<{ items: WarehouseDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    this.dialog.open(WarehouseFormComponent, { width: '500px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(w: WarehouseDto) {
    this.dialog.open(WarehouseFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: w }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  deactivate(w: WarehouseDto) {
    this.api.put(`/api/warehouses/${w.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Warehouse deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  isActive(s: string) { return s === 'ACTIVE'; }
}
