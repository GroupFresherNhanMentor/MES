import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { BomDto } from '../../../../core/models/bom.model';
import type { ProductDto } from '../../../../core/models/product.model';

@Component({
  selector: 'app-bom-create-dialog',
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
  templateUrl: './bom-create-dialog.html',
})
export class BomCreateDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<BomCreateDialog>);
  private fb = inject(FormBuilder);
  private api = inject(ApiService);

  loading = signal(false);
  errorMessage = signal<string | null>(null);
  productsList = signal<ProductDto[]>([]);

  form = this.fb.group({
    finishedProductId: ['', [Validators.required]],
  });

  ngOnInit(): void {
    this.loadFinishedProducts();
  }

  loadFinishedProducts(): void {
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
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    const payload = {
      finishedProductId: this.form.value.finishedProductId!,
    };

    this.errorMessage.set(null);
    this.api.post<BomDto>(API.boms.base, payload).subscribe({
      next: (r) => {
        this.loading.set(false);
        if (r.success && r.data) {
          this.dialogRef.close(r.data);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.errorMessage.set(err?.error?.message || 'Failed to create BOM. Please try again.');
      },
    });
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
