import { Component, inject, signal } from '@angular/core';
import { DatePipe, NgClass } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
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
import type { LocationDto } from '../../../../core/models/location.model';
import { LocationFormComponent } from '../location-form/location-form';

interface LookupEntry { id: string; name: string; }

@Component({
  selector: 'app-location-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatDialogModule, MatTooltipModule,
  ],
  templateUrl: './location-list.html',
})
export class LocationList {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  warehouseId = '';
  items = signal<LocationDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatusId = signal('');
  statuses = signal<LookupEntry[]>([]);

  displayedColumns = ['code', 'name', 'status', 'actions'];

  constructor() {
    this.warehouseId = this.route.snapshot.paramMap.get('warehouseId') || '';
    this.api.get<{ items: LookupEntry[] }>('/api/location-statuses?page=0&size=50').subscribe(r => {
      if (r.success) this.statuses.set(r.data.items);
    });
    this.load();
  }

  load() {
    let url = `/api/warehouses/${this.warehouseId}/locations?page=${this.page()}&size=${this.size()}`;
    if (this.filterStatusId()) url += `&statusId=${this.filterStatusId()}`;
    this.api.get<{ items: LocationDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    this.dialog.open(LocationFormComponent, { width: '500px', data: { warehouseId: this.warehouseId } }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(l: LocationDto) {
    this.dialog.open(LocationFormComponent, { width: '500px', data: { ...l, warehouseId: this.warehouseId } }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  deactivate(l: LocationDto) {
    this.api.put(`/api/warehouses/${this.warehouseId}/locations/${l.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  activate(l: LocationDto) {
    this.api.put(`/api/warehouses/${this.warehouseId}/locations/${l.id}/activate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Activated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  statusName(id: string) { return this.statuses().find(s => s.id === id)?.name ?? ''; }
}
