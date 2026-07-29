import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { BomDto } from '../../../../core/models/bom.model';
import type { ProductDto } from '../../../../core/models/product.model';

interface BomStatusOption {
  id: string;
  name: string;
  description?: string;
}

@Component({
  selector: 'app-bom-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressBarModule,
    MatTooltipModule,
  ],
  templateUrl: './bom-list.html',
})
export class BomList implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  protected router = inject(Router);

  // State Signals
  items = signal<BomDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  loading = signal(false);

  selectedProductId = signal<string | null>(null);
  selectedStatusId = signal<string | null>(null);

  productsList = signal<ProductDto[]>([]);
  statusesList = signal<BomStatusOption[]>([]);

  // Computed signals
  currentUser = this.auth.getCurrentUser();
  canCreate = computed(() => {
    const role = this.currentUser?.role;
    return role === 'ADMIN' || role === 'PLANNER';
  });

  displayedColumns = ['code', 'product', 'version', 'items', 'status', 'createdBy', 'createdAt', 'actions'];

  ngOnInit(): void {
    this.loadDropdowns();
    this.load();
  }

  loadDropdowns(): void {
    const productTypeIds = ['PT-FIN', 'PT-SUB'];
    this.api.get<{ items: ProductDto[] }>(`${API.products.base}?size=100&productTypeId=${productTypeIds.join(',')}`).subscribe((r) => {
      if (r.success && r.data?.items) {
        this.productsList.set(r.data.items);
      }
    });

    this.api.get<BomStatusOption[]>(API.boms.statuses).subscribe((r) => {
      if (r.success && r.data) {
        this.statusesList.set(r.data);
      }
    });
  }

  load(): void {
    this.loading.set(true);
    let url = `${API.boms.base}?page=${this.page()}&size=${this.size()}`;
    if (this.selectedProductId()) {
      url += `&finishedProductId=${encodeURIComponent(this.selectedProductId()!)}`;
    }
    if (this.selectedStatusId()) {
      url += `&bomStatusId=${encodeURIComponent(this.selectedStatusId()!)}`;
    }

    this.api.get<{ items: BomDto[]; totalElements: number }>(url).subscribe({
      next: (r) => {
        if (r.success && r.data) {
          this.items.set(r.data.items || []);
          this.total.set(r.data.totalElements || 0);
        }
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onProductFilterChange(val: string | null): void {
    this.selectedProductId.set(val);
    this.page.set(0);
    this.load();
  }

  onStatusFilterChange(val: string | null): void {
    this.selectedStatusId.set(val);
    this.page.set(0);
    this.load();
  }

  resetFilters(): void {
    this.selectedProductId.set(null);
    this.selectedStatusId.set(null);
    this.page.set(0);
    this.load();
  }

  onPage(e: PageEvent): void {
    this.page.set(e.pageIndex);
    this.size.set(e.pageSize);
    this.load();
  }

  onRowClick(row: BomDto): void {
    void this.router.navigate(['/boms', row.id]);
  }

  onCreateBom(): void {
    console.log('Create BOM clicked');
  }
}
