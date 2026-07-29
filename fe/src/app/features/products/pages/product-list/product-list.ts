import { Component, inject, signal } from '@angular/core';
import { DatePipe, NgClass, SlicePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import { ProductDto } from '../../../../core/models/product.model';

interface LookupEntry { id: string; name: string; description: string; }

@Component({
  selector: 'app-product-list',
  imports: [
    DatePipe, SlicePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule, MatDialogModule,
    MatSnackBarModule, MatCardModule,
  ],
  templateUrl: './product-list.html',
})
export class ProductListComponent {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  products = signal<ProductDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  filterStatusId = signal('');

  types = signal<LookupEntry[]>([]);
  statuses = signal<LookupEntry[]>([]);
  displayedColumns = ['code', 'name', 'type', 'unit', 'status', 'actions'];

  constructor() {
    this.api.get<LookupEntry[]>('/api/products/types').subscribe(r => { if (r.success) this.types.set(r.data); });
    this.api.get<LookupEntry[]>('/api/products/statuses').subscribe(r => { if (r.success) this.statuses.set(r.data); });
    this.load();
  }

  load() {
    let url = `/api/products?page=${this.page()}&size=${this.size()}`;
    if (this.filterStatusId()) url += `&statusId=${this.filterStatusId()}`;
    this.api.get<{ items: ProductDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.products.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  deactivate(p: ProductDto) {
    this.api.put(`/api/products/${p.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  typeName(id: string) { return this.types().find(t => t.id === id)?.name ?? ''; }
  statusName(id: string) { return this.statuses().find(s => s.id === id)?.name ?? ''; }
}
