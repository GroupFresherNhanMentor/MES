import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { BomDto, BomItemDto } from '../../../../core/models/bom.model';
import { BomAddItemDialog } from '../../components/bom-add-item-dialog/bom-add-item-dialog';

@Component({
  selector: 'app-bom-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressBarModule,
    MatTooltipModule,
    MatDialogModule,
  ],
  templateUrl: './bom-detail.html',
})
export class BomDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(ApiService);
  private dialog = inject(MatDialog);

  private auth = inject(AuthService);

  bomId = signal<string | null>(null);
  bom = signal<BomDto | null>(null);
  loading = signal(true);
  actionLoading = signal(false);
  errorMessage = signal<string | null>(null);

  // Computed business rules & Role Matrix (FR-BOM-003, Role Matrix)
  currentUser = this.auth.getCurrentUser();
  canWrite = computed(() => {
    const role = this.currentUser?.role;
    return role === 'ADMIN' || role === 'PLANNER';
  });

  isDraft = computed(() => {
    const s = this.bom();
    if (!s) return false;
    const name = (s.bomStatusName || '').trim().toUpperCase();
    const id = (s.bomStatusId || '').trim().toUpperCase();
    return name === 'DRAFT' || id === 'BS-DRAFT' || id === 'DRAFT';
  });

  isActive = computed(() => {
    const s = this.bom();
    if (!s) return false;
    const name = (s.bomStatusName || '').trim().toUpperCase();
    const id = (s.bomStatusId || '').trim().toUpperCase();
    return name === 'ACTIVE' || id === 'BS-ACTIVE' || id === 'ACTIVE';
  });

  isInactive = computed(() => {
    const s = this.bom();
    if (!s) return false;
    const name = (s.bomStatusName || '').trim().toUpperCase();
    const id = (s.bomStatusId || '').trim().toUpperCase();
    return name === 'INACTIVE' || id === 'BS-INACTIVE' || id === 'INACTIVE';
  });

  itemsCount = computed(() => this.bom()?.items?.length ?? 0);
  scrapRateAvg = computed(() => {
    const items = this.bom()?.items;
    if (!items || items.length === 0) return '0.0';
    const sum = items.reduce((acc, i) => acc + (i.scrapRate || 0), 0);
    return (sum / items.length).toFixed(1);
  });

  canActivate = computed(() => this.canWrite() && this.isDraft() && this.itemsCount() > 0);
  canNewVersion = computed(() => this.canWrite() && !this.isDraft());

  displayedColumns = computed(() => {
    const base = ['materialProductCode', 'materialProductName', 'quantityPerUnit', 'unit', 'scrapRate'];
    if (this.canWrite() && this.isDraft()) {
      return [...base, 'actions'];
    }
    return base;
  });

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id && (id !== this.bomId() || !this.bom())) {
        this.bomId.set(id);
        this.load();
      }
    });
  }

  load(): void {
    const id = this.bomId();
    if (!id) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const url = `${API.boms.base}/${id}`;
    this.api.get<BomDto>(url).subscribe({
      next: (r) => {
        this.loading.set(false);
        if (r.data) {
          this.bom.set({ ...r.data, items: [...(r.data.items || [])] });
        } else {
          this.errorMessage.set('BOM not found');
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to load BOM details.');
      },
    });
  }

  onBack(): void {
    void this.router.navigate(['/boms']);
  }

  onActivate(): void {
    const id = this.bomId();
    if (!id || !this.canActivate()) return;

    this.actionLoading.set(true);
    const url = `${API.boms.base}/${id}/activate`;
    this.api.post<BomDto>(url, {}).subscribe({
      next: (r) => {
        this.actionLoading.set(false);
        if (r.data && r.data.id) {
          this.bom.set({ ...r.data, bomStatusName: 'ACTIVE', bomStatusId: 'BS-ACTIVE', items: [...(r.data.items || [])] });
        } else {
          this.load();
        }
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to activate BOM.');
      },
    });
  }

  onNewVersion(): void {
    const id = this.bomId();
    if (!id || !this.canNewVersion()) return;

    this.actionLoading.set(true);
    const url = `${API.boms.base}/${id}/new-version`;
    this.api.post<BomDto>(url, {}).subscribe({
      next: (r) => {
        this.actionLoading.set(false);
        if (r.data?.id) {
          this.bomId.set(r.data.id);
          this.bom.set({ ...r.data, items: [...(r.data.items || [])] });
          void this.router.navigate(['/boms', r.data.id]);
        } else {
          this.load();
        }
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to create new BOM version.');
      },
    });
  }

  onAddItem(): void {
    const id = this.bomId();
    if (!id || !this.canWrite() || !this.isDraft()) return;

    const dialogRef = this.dialog.open(BomAddItemDialog, {
      width: '560px',
      panelClass: 'ff-dialog-panel',
      data: { bomId: id },
    });

    dialogRef.afterClosed().subscribe((result: BomItemDto | null) => {
      if (result) {
        const current = this.bom();
        if (current) {
          const updatedItems = [...(current.items || []), result];
          this.bom.set({
            ...current,
            items: updatedItems,
          });
        }
        this.load();
      }
    });
  }

  onRemoveItem(item: BomItemDto): void {
    const id = this.bomId();
    if (!id || !item.id || !this.canWrite() || !this.isDraft()) return;

    this.actionLoading.set(true);
    const url = `${API.boms.base}/${id}/items/${item.id}`;
    this.api.delete<void>(url).subscribe({
      next: () => {
        this.actionLoading.set(false);
        const current = this.bom();
        if (current) {
          const updatedItems = (current.items || []).filter((i) => i.id !== item.id);
          this.bom.set({
            ...current,
            items: updatedItems,
          });
        }
        this.load();
      },
      error: (err) => {
        this.actionLoading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to remove item.');
      },
    });
  }
}
