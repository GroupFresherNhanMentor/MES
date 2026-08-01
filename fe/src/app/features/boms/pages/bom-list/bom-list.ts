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

import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { BomDto } from '../../../../core/models/bom.model';
import type { ProductDto } from '../../../../core/models/product.model';
import { BomCreateDialog } from '../../components/bom-create-dialog/bom-create-dialog';

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
    MatInputModule,
    MatSelectModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatDialogModule,
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

  searchQuery = signal<string>('');
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

  activeCount = computed(() =>
    this.items().filter((i) => {
      const name = (i.bomStatusName || '').trim().toUpperCase();
      const id = (i.bomStatusId || '').trim().toUpperCase();
      return name === 'ACTIVE' || id === 'BS-ACTIVE' || id === 'ACTIVE';
    }).length,
  );
  draftCount = computed(() =>
    this.items().filter((i) => {
      const name = (i.bomStatusName || '').trim().toUpperCase();
      const id = (i.bomStatusId || '').trim().toUpperCase();
      return name === 'DRAFT' || id === 'BS-DRAFT' || id === 'DRAFT';
    }).length,
  );
  totalComponentsCount = computed(() => this.items().reduce((acc, b) => acc + (b.items?.length || 0), 0));

  filteredItems = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.items();
    return this.items().filter(
      (item) =>
        (item.finishedProductCode || '').toLowerCase().includes(query) ||
        (item.finishedProductName || '').toLowerCase().includes(query) ||
        (item.createdBy || '').toLowerCase().includes(query),
    );
  });

  displayedColumns = ['code', 'product', 'version', 'items', 'status', 'createdBy', 'createdAt', 'actions'];

  ngOnInit(): void {
    this.loadDropdowns();
    this.load();
  }

  loadDropdowns(): void {
    this.api.get<any>(`${API.products.base}?size=100`).subscribe((r) => {
      if (r.success && r.data) {
        const rawItems: ProductDto[] = r.data?.items || (Array.isArray(r.data) ? r.data : []);
        const filtered = rawItems.filter((p) => {
          const typeName = (p.productType?.name || p.productTypeName || '').trim().toUpperCase();
          return typeName === 'FINISHED_GOOD' || typeName === 'SEMI_FINISHED';
        });
        this.productsList.set(filtered.length > 0 ? filtered : rawItems);
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
    this.searchQuery.set('');
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

  private dialog = inject(MatDialog);

  onCreateBom(): void {
    const dialogRef = this.dialog.open(BomCreateDialog, {
      width: '560px',
      panelClass: 'ff-dialog-panel',
    });

    dialogRef.afterClosed().subscribe((newBom: BomDto | null) => {
      if (newBom && newBom.id) {
        void this.router.navigate(['/boms', newBom.id]);
      }
    });
  }
}
