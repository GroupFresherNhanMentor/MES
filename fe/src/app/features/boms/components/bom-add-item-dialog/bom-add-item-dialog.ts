import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
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
import type { BomItemDto, CreateBomItemRequest } from '../../../../core/models/bom.model';

export interface BomAddItemDialogData {
  bomId: string;
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

  loading = signal(false);
  errorMessage = signal<string | null>(null);
  materialsList = signal<ProductDto[]>([]);

  form = this.fb.group({
    materialProductId: ['', [Validators.required]],
    quantityPerUnit: [1, [Validators.required, Validators.min(0.0001)]],
    unit: ['PCS'],
    scrapRate: [0, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.loadMaterials();

    // Auto-update unit when material product changes
    this.form.get('materialProductId')?.valueChanges.subscribe((prodId) => {
      if (prodId) {
        const prod = this.materialsList().find((p) => p.id === prodId);
        if (prod && prod.unitName) {
          this.form.patchValue({ unit: prod.unitName });
        }
      }
    });
  }

  private loadMaterials(): void {
    this.loading.set(true);
    const materialTypeIds = ['PT-RAW', 'PT-SUB', 'PT-CON'];
    this.api.get<{ items: ProductDto[] }>(`${API.products.base}?size=100&productTypeId=${materialTypeIds.join(',')}`).subscribe({
      next: (r) => {
        this.loading.set(false);
        if (r.data?.items) {
          const filtered = r.data.items.filter((p) => {
            const typeName = (p.productTypeName || '').trim().toUpperCase();
            const typeId = (p.productTypeId || '').trim().toUpperCase();
            return (
              typeName === 'RAW_MATERIAL' ||
              typeName === 'SEMI_FINISHED' ||
              typeName === 'SUB_ASSEMBLY' ||
              typeName === 'CONSUMABLE' ||
              typeId === 'PT-RAW' ||
              typeId === 'PT-SUB' ||
              typeId === 'PT-CON'
            );
          });
          this.materialsList.set(filtered);
        }
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }

  onSubmit(): void {
    if (this.form.invalid || !this.data?.bomId) return;

    this.loading.set(true);
    this.errorMessage.set(null);

    const payload: CreateBomItemRequest = {
      materialProductId: this.form.value.materialProductId!,
      quantityPerUnit: Number(this.form.value.quantityPerUnit ?? 1),
      unit: this.form.value.unit || 'PCS',
      scrapRate: Number(this.form.value.scrapRate ?? 0),
    };

    const url = `${API.boms.base}/${this.data.bomId}/items`;
    this.api.post<BomItemDto>(url, payload).subscribe({
      next: (r) => {
        this.loading.set(false);
        if (r.data) {
          this.dialogRef.close(r.data);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to add item to BOM.');
      },
    });
  }
}
