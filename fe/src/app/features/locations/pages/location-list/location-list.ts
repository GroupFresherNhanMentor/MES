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
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import type { LocationDto } from '../../../../core/models/location.model';
import { LOCATION_STATUSES } from '../../../../configs/constants';

@Component({
  selector: 'app-location-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule,
  ],
  templateUrl: './location-list.html',
})
export class LocationList {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  warehouseId = signal('');
  items = signal<LocationDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatus = signal('');

  statuses = LOCATION_STATUSES;
  displayedColumns = ['code', 'name', 'status', 'actions'];

  constructor() {
    const params = this.route.snapshot.params as Record<string, string>;
    this.warehouseId.set(params['warehouseId']);
    this.load();
  }

  load() {
    const wid = this.warehouseId();
    let url = `/api/warehouses/${wid}/locations?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatus()) url += `&statusName=${encodeURIComponent(this.filterStatus())}`;
    this.api.get<{ items: LocationDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  deactivate(l: LocationDto) {
    const wid = this.warehouseId();
    this.api.put(`/api/warehouses/${wid}/locations/${l.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Location deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  isActive(statusName: string) { return statusName === 'AVAILABLE'; }
}
