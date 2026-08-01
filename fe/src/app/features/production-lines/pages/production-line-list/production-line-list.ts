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
import type { ProductionLineDto } from '../../../../core/models/production-line.model';
import { ProductionLineFormComponent } from '../production-line-form/production-line-form';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-production-line-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatDialogModule, MatTooltipModule,
  ],
  templateUrl: './production-line-list.html',
})
export class ProductionLineList {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<ProductionLineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatusId = signal('');
  statuses = signal<LookupEntry[]>([]);

  displayedColumns = ['code', 'name', 'status', 'createdBy', 'createdAt', 'actions'];

  constructor() {
    this.api.get<{ items: LookupEntry[] }>('/api/line-statuses?page=0&size=50').subscribe(r => {
      if (r.success) this.statuses.set(r.data.items);
    });
    this.load();
  }

  load() {
    let url = `/api/lines?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&code=${encodeURIComponent(this.keyword())}&name=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatusId()) url += `&lineStatusId=${this.filterStatusId()}`;
    this.api.get<{ items: ProductionLineDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    this.dialog.open(ProductionLineFormComponent, { width: '500px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(l: ProductionLineDto) {
    this.dialog.open(ProductionLineFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: l }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  deactivate(l: ProductionLineDto) {
    this.api.put(`/api/lines/${l.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  activate(l: ProductionLineDto) {
    this.api.put(`/api/lines/${l.id}/activate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Activated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  statusName(id: string) { return this.statuses().find(s => s.id === id)?.name ?? ''; }
}
