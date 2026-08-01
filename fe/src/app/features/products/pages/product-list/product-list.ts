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
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import { ProductDto } from '../../../../core/models/product.model';
import { ProductFormComponent } from '../product-form/product-form';

interface LookupEntry { id: string; name: string; description: string; }

@Component({
  selector: 'app-product-list',
  imports: [
    DatePipe, NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule, MatDialogModule,
    MatSnackBarModule, MatCardModule, MatTooltipModule,
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
  displayedColumns = ['code', 'name', 'type', 'unit', 'version', 'status', 'createdBy', 'createdAt', 'actions'];

  constructor() {
    this.api.get<any>(API.products.productTypes + '?size=100').subscribe(r => {
      if (r.success && r.data) {
        const items = r.data.items || r.data;
        this.types.set(items);
      }
    });
    this.api.get<any>(API.products.productStatuses + '?size=100').subscribe(r => {
      if (r.success && r.data) {
        const items = r.data.items || r.data;
        this.statuses.set(items);
      }
    });
    this.load();
  }

  load() {
    let url = `${API.products.base}?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&code=${encodeURIComponent(this.keyword())}&name=${encodeURIComponent(this.keyword())}`;
    if (this.filterStatusId()) url += `&productStatusId=${encodeURIComponent(this.filterStatusId())}`;
    this.api.get<{ items: ProductDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success && r.data) {
        this.products.set(r.data.items || []);
        this.total.set(r.data.totalElements || 0);
      }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  deactivate(p: ProductDto) {
    this.api.put(`/api/products/${p.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }

  activate(p: ProductDto) {
    this.api.put(`/api/products/${p.id}/activate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Activated', 'OK', { duration: 2000 }); this.load(); }
    });
  }


  openCreate() {
    this.dialog.open(ProductFormComponent, { width: '500px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(p: ProductDto) {
    this.dialog.open(ProductFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: p }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  typeName(id: string) { return this.types().find(t => t.id === id)?.name ?? ''; }
  statusName(id: string) { return this.statuses().find(s => s.id === id)?.name ?? ''; }
}
