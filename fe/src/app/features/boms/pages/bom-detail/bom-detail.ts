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
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { API } from '../../../../configs/api-endpoints';
import type { BomDto, BomItemDto, NewBomItemData } from '../../../../core/models/bom.model';
import { BomAddItemDialog } from '../../components/bom-add-item-dialog/bom-add-item-dialog';

interface ItemPayload {
  materialProductId: string;
  quantityPerUnit: number;
  scrapRate: number;
}

@Component({
  selector: 'app-bom-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
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

  editingItemId = signal<string | null>(null);
  editQty = signal<number>(1);
  editScrapRate = signal<number>(0);

  currentUser = this.auth.getCurrentUser();
  canWrite = computed(() => {
    const role = this.currentUser?.role;
    return role === 'ADMIN' || role === 'PLANNER';
  });

  isDraft = computed(() => this.bom()?.bomStatus?.name === 'DRAFT');
  isActive = computed(() => this.bom()?.bomStatus?.name === 'ACTIVE');
  isInactive = computed(() => this.bom()?.bomStatus?.name === 'INACTIVE');

  itemsCount = computed(() => this.bom()?.items?.length ?? 0);
  scrapRateAvg = computed(() => {
    const items = this.bom()?.items;
    if (!items || items.length === 0) return '0.0';
    const sum = items.reduce((acc, i) => acc + (i.scrapRate || 0), 0);
    return (sum / items.length).toFixed(1);
  });

  canActivate = computed(() => this.canWrite() && this.isDraft() && this.itemsCount() > 0);
  canDeactivate = computed(() => this.canWrite() && this.isActive());
  canNewVersion = computed(() => this.canWrite() && !this.isDraft());

  displayedColumns = computed(() => {
    const base = ['materialProductCode', 'materialProductName', 'quantityPerUnit', 'unit', 'scrapRate'];
    return this.canWrite() && this.isDraft() ? [...base, 'actions'] : base;
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
    this.api.get<BomDto>(`${API.boms.base}/${id}`).subscribe({
      next: r => {
        this.loading.set(false);
        if (r.data) this.bom.set({ ...r.data, items: [...(r.data.items || [])] });
        else this.errorMessage.set('BOM not found');
      },
      error: err => { this.loading.set(false); this.errorMessage.set(err?.error?.message || 'Failed to load BOM details.'); },
    });
  }

  onBack(): void { void this.router.navigate(['/boms']); }

  onActivate(): void {
    const id = this.bomId();
    if (!id || !this.canActivate()) return;
    this.actionLoading.set(true);
    this.api.post<void>(`${API.boms.activate(id)}`, {}).subscribe({
      next: () => { this.actionLoading.set(false); this.load(); },
      error: err => { this.actionLoading.set(false); this.errorMessage.set(err?.error?.message || 'Failed to activate BOM.'); },
    });
  }

  onDeactivate(): void {
    const id = this.bomId();
    if (!id || !this.canDeactivate()) return;
    this.actionLoading.set(true);
    this.api.post<void>(`${API.boms.deactivate(id)}`, {}).subscribe({
      next: () => { this.actionLoading.set(false); this.load(); },
      error: err => { this.actionLoading.set(false); this.errorMessage.set(err?.error?.message || 'Failed to deactivate BOM.'); },
    });
  }

  onNewVersion(): void {
    const id = this.bomId();
    if (!id || !this.canNewVersion()) return;
    this.actionLoading.set(true);
    this.api.post<BomDto>(`${API.boms.newVersion(id)}`, {}).subscribe({
      next: r => {
        this.actionLoading.set(false);
        if (r.data?.id) void this.router.navigate(['/boms', r.data.id]);
        else this.load();
      },
      error: err => { this.actionLoading.set(false); this.errorMessage.set(err?.error?.message || 'Failed to create new BOM version.'); },
    });
  }

  onAddItem(): void {
    const id = this.bomId();
    if (!id || !this.canWrite() || !this.isDraft()) return;

    const existingMaterialIds = (this.bom()?.items || [])
      .map(i => i.materialProductId)
      .filter((mId): mId is string => !!mId);

    this.dialog.open(BomAddItemDialog, {
      width: '560px',
      panelClass: 'ff-dialog-panel',
      data: { bomId: id, existingMaterialIds },
    }).afterClosed().subscribe((newItem: NewBomItemData | null) => {
      if (!newItem) return;
      const current = this.bom()?.items ?? [];
      this.submitItems([
        ...current.map(i => this.toPayload(i)),
        { materialProductId: newItem.materialProductId, quantityPerUnit: newItem.quantityPerUnit, scrapRate: newItem.scrapRate },
      ]);
    });
  }

  onEditItem(item: BomItemDto): void {
    this.editingItemId.set(item.id);
    this.editQty.set(item.quantityPerUnit ?? 1);
    this.editScrapRate.set(item.scrapRate ?? 0);
  }

  onCancelEdit(): void { this.editingItemId.set(null); }

  onSaveEdit(item: BomItemDto): void {
    const newQty = Number(this.editQty());
    const newScrap = Number(this.editScrapRate());
    if (isNaN(newQty) || newQty <= 0) { this.errorMessage.set('Quantity must be greater than 0.'); return; }
    if (isNaN(newScrap) || newScrap < 0) { this.errorMessage.set('Scrap rate cannot be negative.'); return; }
    this.errorMessage.set(null);
    const payload = (this.bom()?.items ?? []).map(i =>
      i.id === item.id
        ? { materialProductId: i.materialProductId, quantityPerUnit: newQty, scrapRate: newScrap }
        : this.toPayload(i)
    );
    this.submitItems(payload);
  }

  onRemoveItem(item: BomItemDto): void {
    const payload = (this.bom()?.items ?? [])
      .filter(i => i.id !== item.id)
      .map(i => this.toPayload(i));
    this.submitItems(payload);
  }

  private toPayload(i: BomItemDto): ItemPayload {
    return { materialProductId: i.materialProductId, quantityPerUnit: i.quantityPerUnit, scrapRate: i.scrapRate ?? 0 };
  }

  private submitItems(payload: ItemPayload[]): void {
    const bomId = this.bomId();
    if (!bomId) return;
    this.actionLoading.set(true);
    this.api.put<void>(API.boms.items(bomId), payload).subscribe({
      next: () => { this.actionLoading.set(false); this.editingItemId.set(null); this.load(); },
      error: err => { this.actionLoading.set(false); this.errorMessage.set(err?.error?.message || 'Failed to update items.'); },
    });
  }
}
