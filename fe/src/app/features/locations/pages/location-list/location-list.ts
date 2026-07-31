import { Component, inject, signal } from '@angular/core';
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
import type { LocationDto } from "../../../../core/models/location.model";
import { LOCATION_STATUSES } from '../../../../configs/constants';
import { LocationFormComponent } from '../location-form/location-form';

import { DatePipe, NgClass } from '@angular/common';

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
  filterStatus = signal('');

  statuses = LOCATION_STATUSES;
  displayedColumns = ['code', 'name', 'warehouse', 'status', 'createdAt', 'actions'];

  constructor() {
    this.warehouseId = this.route.snapshot.paramMap.get('warehouseId') || '';
    this.load();
  }

  load() {
    let url = `/api/warehouses/${this.warehouseId}/locations?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    this.api.get<{ items: LocationDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    const ref = this.dialog.open(LocationFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: { warehouseId: this.warehouseId } });
    ref.afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(l: LocationDto) {
    this.dialog.open(LocationFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: l }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  deactivate(l: LocationDto) {
    this.api.put(`/api/warehouses/${this.warehouseId}/locations/${l.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  isActive(s: string) { return s === 'ACTIVE'; }
}
