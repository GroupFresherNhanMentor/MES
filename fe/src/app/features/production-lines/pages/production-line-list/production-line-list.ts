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
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import type { ProductionLineDto } from '../../../../core/models/production-line.model';
import { PRODUCTION_LINE_STATUSES } from '../../../../configs/constants';

@Component({
  selector: 'app-production-line-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule,
  ],
  templateUrl: './production-line-list.html',
})
export class ProductionLineList {
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  items = signal<ProductionLineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatus = signal('');

  statuses = PRODUCTION_LINE_STATUSES;
  displayedColumns = ['code', 'name', 'status', 'actions'];

  constructor() {
    this.load();
  }

  load() {
    let url = `/api/production-lines?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatus()) url += `&statusName=${encodeURIComponent(this.filterStatus())}`;
    this.api.get<{ items: ProductionLineDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  deactivate(pl: ProductionLineDto) {
    this.api.put(`/api/production-lines/${pl.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Production line deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  isActive(statusName: string) { return statusName === 'ACTIVE'; }
}
