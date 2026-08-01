import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { ProductDto } from '../../../../core/models/product.model';
import type { NewBomItemData } from '../../../../core/models/bom.model';

export interface BomAddItemDialogData {
  bomId: string;
  existingMaterialIds?: string[];
}

@Component({
  selector: 'app-bom-add-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
  ],
  templateUrl: './bom-add-item-dialog.html',
})
export class BomAddItemDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<BomAddItemDialog>);
  private data = inject<BomAddItemDialogData>(MAT_DIALOG_DATA);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  loading = signal(true);
  errorMessage = signal<string | null>(null);
  materialsList = signal<ProductDto[]>([]);
  selectedUnitName = signal<string | null>(null);

  form = this.fb.group({
    materialProductId: ['', [Validators.required]],
    quantityPerUnit: [1, [Validators.required, Validators.min(0.0001)]],
    scrapRate: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.api.get<any>(`${API.products.base}?size=100`).subscribe({
      next: r => {
        if (r.success && r.data) {
          const rawItems: ProductDto[] = r.data?.items || [];
          const existingIds = this.data?.existingMaterialIds || [];
          const filtered = rawItems.filter(p => {
            if (p.id && existingIds.includes(p.id)) return false;
            const typeName = (p.productType?.name || (p as any).productTypeName || '').trim().toUpperCase();
            return typeName === 'RAW_MATERIAL' || typeName === 'SEMI_FINISHED' || typeName === 'CONSUMABLE' || typeName === 'SPARE_PART';
          });
          this.materialsList.set(filtered.length > 0 ? filtered : rawItems.filter(p => !existingIds.includes(p.id!)));
        }
        this.loading.set(false);
      },
      error: () => { this.loading.set(false); this.errorMessage.set('Failed to load products.'); },
    });

    this.form.get('materialProductId')?.valueChanges.subscribe(prodId => {
      if (!prodId) { this.selectedUnitName.set(null); return; }
      const prod = this.materialsList().find(p => p.id === prodId);
      const unitName = (prod as any)?.unit?.name || (prod as any)?.unitName || null;
      this.selectedUnitName.set(unitName);
    });
  }

  onCancel(): void { this.dialogRef.close(null); }

  onSubmit(): void {
    if (this.form.invalid) return;
    const result: NewBomItemData = {
      materialProductId: this.form.value.materialProductId!,
      quantityPerUnit: Number(this.form.value.quantityPerUnit ?? 1),
      scrapRate: Number(this.form.value.scrapRate ?? 0),
    };
    this.dialogRef.close(result);
  }
}
