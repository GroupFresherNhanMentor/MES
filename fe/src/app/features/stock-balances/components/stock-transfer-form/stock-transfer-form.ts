import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ApiService } from '../../../../core/services/api';
import { API } from '../../../../configs/api-endpoints';
import type { ProductDto } from '../../../../core/models/product.model';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';
import type { LocationDto } from '../../../../core/models/location.model';
import type { StockBalanceDto } from '../../../../core/models/stock-balance.model';

export interface StockTransferDialogData {
  stockBalance?: StockBalanceDto;
}

@Component({
  selector: 'app-stock-transfer-form',
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
    MatSnackBarModule,
  ],
  template: `
    <h2 mat-dialog-title class="!text-lg !font-bold">Transfer Stock</h2>
    <mat-dialog-content>
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" style="margin-bottom: 16px;"></mat-progress-bar>
      }

      <form [formGroup]="form" style="display:flex; flex-direction:column; gap:12px; padding-top:8px;">

        <!-- 1. Choose Product -->
        <mat-form-field appearance="outline">
          <mat-label>Product</mat-label>
          <mat-select formControlName="productId" required>
            @for (p of productsList(); track p.id) {
              <mat-option [value]="p.id">{{ p.name }} ({{ p.code }})</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <!-- Product Unit Badge -->
        @if (selectedUnitName()) {
          <div style="font-size: 12px; font-weight: 500; color: #475569; display: flex; align-items: center; gap: 4px; padding: 6px 10px; background: #f1f5f9; border-radius: 6px; border: 1px solid #e2e8f0; margin-top: -4px;">
            <mat-icon style="font-size: 14px; width: 14px; height: 14px; color: #3b82f6;">straighten</mat-icon>
            <span>Unit: <strong>{{ selectedUnitName() }}</strong></span>
            @if (selectedUnitDescription()) {
              <span style="color: #64748b; font-weight: normal;">— {{ selectedUnitDescription() }}</span>
            }
          </div>
        }

        <!-- 2. Choose Lot -->
        <mat-form-field appearance="outline">
          <mat-label>Lot Number (Available Status)</mat-label>
          <mat-select formControlName="lotId" required [disabled]="!form.get('productId')?.value">
            @for (lot of lotsList(); track lot.id) {
              <mat-option [value]="lot.id">{{ lot.lotNumber }}</mat-option>
            }
          </mat-select>
          @if (form.get('productId')?.value && lotsList().length === 0) {
            <mat-hint class="!text-amber-600">No active lots found for this product</mat-hint>
          }
        </mat-form-field>

        <!-- 3. Choose From Location (Source Stock Balance) -->
        <mat-form-field appearance="outline">
          <mat-label>From Warehouse & Location</mat-label>
          <mat-select formControlName="sourceBalanceId" required [disabled]="!form.get('lotId')?.value" (selectionChange)="onSourceBalanceChange($event.value)">
            @for (sb of sourceBalancesList(); track sb.id) {
              <mat-option [value]="sb.id">
                {{ sb.warehouse?.name }} ({{ sb.warehouse?.code }}) / {{ sb.location?.name || sb.location?.code }} — Available: {{ sb.quantity }} {{ selectedUnitName() || '' }}
              </mat-option>
            }
          </mat-select>
        </mat-form-field>

        <!-- Current Available Quantity Display -->
        @if (selectedSourceBalance(); as sb) {
          <div style="padding: 8px 12px; background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; font-size: 13px; color: #1e40af; display: flex; justify-content: space-between; align-items: center;">
            <span>Current Available Qty:</span>
            <span style="font-family: monospace; font-size: 15px; font-weight: 700;">
              {{ sb.quantity | number:'1.0-4' }} {{ selectedUnitName() || '' }}
            </span>
          </div>
        }

        <!-- 4. Destination Warehouse & Location -->
        <div style="display:flex; gap:12px;">
          <mat-form-field appearance="outline" style="flex:1;">
            <mat-label>To Warehouse</mat-label>
            <mat-select formControlName="toWarehouseId" required (selectionChange)="onToWarehouseChange($event.value)">
              @for (wh of warehousesList(); track wh.id) {
                <mat-option [value]="wh.id">{{ wh.name }} ({{ wh.code }})</mat-option>
              }
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline" style="flex:1;">
            <mat-label>To Location</mat-label>
            <mat-select formControlName="toLocationId" required [disabled]="!form.get('toWarehouseId')?.value">
              @for (loc of toLocationsList(); track loc.id) {
                <mat-option [value]="loc.id">{{ loc.name || loc.code }} ({{ loc.code }})</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </div>

        <!-- 5. Quantity -->
        <mat-form-field appearance="outline">
          <mat-label>Transfer Quantity</mat-label>
          <input matInput type="number" formControlName="quantity" required step="0.01" min="0.0001">
          @if (selectedSourceBalance(); as sb) {
            <mat-hint>Max available: {{ sb.quantity }} {{ selectedUnitName() || '' }}</mat-hint>
          }
        </mat-form-field>

      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button class="ff-btn-primary" (click)="onSubmit()" [disabled]="form.invalid || submitting()">Approve Transfer</button>
    </mat-dialog-actions>
  `
})
export class StockTransferFormComponent implements OnInit {
  private dialogRef = inject(MatDialogRef<StockTransferFormComponent>);
  data = inject<StockTransferDialogData>(MAT_DIALOG_DATA, { optional: true });
  private fb = inject(FormBuilder);
  private api = inject(ApiService);
  private snackBar = inject(MatSnackBar);

  loading = signal(true);
  submitting = signal(false);

  productsList = signal<ProductDto[]>([]);
  lotsList = signal<any[]>([]);
  sourceBalancesList = signal<StockBalanceDto[]>([]);
  selectedSourceBalance = signal<StockBalanceDto | null>(null);
  warehousesList = signal<WarehouseDto[]>([]);
  toLocationsList = signal<LocationDto[]>([]);
  availableLotTypeId = signal<string | null>(null);

  selectedUnitName = signal<string | null>(null);
  selectedUnitDescription = signal<string | null>(null);

  form = this.fb.group({
    productId: ['', [Validators.required]],
    lotId: ['', [Validators.required]],
    sourceBalanceId: ['', [Validators.required]],
    toWarehouseId: ['', [Validators.required]],
    toLocationId: ['', [Validators.required]],
    quantity: [0, [Validators.required, Validators.min(0.0001)]],
  });

  ngOnInit(): void {
    // 1. Load initial lookups (products, warehouses, and available lotTypeId)
    let loadedCount = 0;
    const checkLoading = () => {
      loadedCount++;
      if (loadedCount >= 2) {
        this.loading.set(false);
        this.applyPreFill();
      }
    };

    this.api.get<any>('/api/lot-types?size=100').subscribe({
      next: r => {
        if (r.success && r.data) {
          const items: any[] = r.data.items || [];
          const found = items.find(lt => (lt.name || '').toUpperCase().includes('AVAILABLE'));
          if (found) {
            this.availableLotTypeId.set(found.id);
          }
        }
      }
    });

    this.api.get<any>(`${API.products.base}?size=100`).subscribe({
      next: r => {
        if (r.success && r.data) this.productsList.set(r.data.items || []);
        checkLoading();
      },
      error: () => checkLoading()
    });

    this.api.get<any>(`${API.warehouses.base}?size=100`).subscribe({
      next: r => {
        if (r.success && r.data) this.warehousesList.set(r.data.items || []);
        checkLoading();
      },
      error: () => checkLoading()
    });

    // 2. Product change listener: update unit and fetch lots with lotTypeId
    this.form.get('productId')?.valueChanges.subscribe(prodId => {
      this.form.get('lotId')?.setValue('', { emitEvent: false });
      this.form.get('sourceBalanceId')?.setValue('', { emitEvent: false });
      this.selectedSourceBalance.set(null);
      this.sourceBalancesList.set([]);

      this.updateSelectedUnit(prodId);
      if (prodId) {
        this.loadLotsForProduct(prodId);
      } else {
        this.lotsList.set([]);
      }
    });

    // 3. Lot change listener: fetch available source balances
    this.form.get('lotId')?.valueChanges.subscribe(lotId => {
      this.form.get('sourceBalanceId')?.setValue('', { emitEvent: false });
      this.selectedSourceBalance.set(null);

      const prodId = this.form.get('productId')?.value;
      if (prodId && lotId) {
        this.loadSourceBalances(prodId, lotId);
      } else {
        this.sourceBalancesList.set([]);
      }
    });
  }

  private applyPreFill() {
    if (this.data?.stockBalance) {
      const sb = this.data.stockBalance;
      if (sb.product?.id) {
        this.form.get('productId')?.setValue(sb.product.id);
      }
    }
  }

  updateSelectedUnit(prodId: string | null | undefined) {
    if (!prodId) {
      this.selectedUnitName.set(null);
      this.selectedUnitDescription.set(null);
      return;
    }
    const prod = this.productsList().find(p => p.id === prodId);
    const unitObj = (prod as any)?.unit;
    const unitName = unitObj?.name || (prod as any)?.unitName || null;
    const unitDesc = unitObj?.description || null;
    this.selectedUnitName.set(unitName);
    this.selectedUnitDescription.set(unitDesc);
  }

  loadLotsForProduct(productId: string) {
    let url = `${(API as any).stockLots.base}?productId=${encodeURIComponent(productId)}&size=100`;
    const typeId = this.availableLotTypeId();
    if (typeId) {
      url += `&lotTypeId=${encodeURIComponent(typeId)}`;
    }

    this.api.get<any>(url).subscribe({
      next: r => {
        if (r.success && r.data) {
          const lots = r.data.items || [];
          this.lotsList.set(lots);

          // Auto-select lot if pre-filled from stock balance
          if (this.data?.stockBalance?.lot?.id) {
            const preLotId = this.data.stockBalance.lot.id;
            if (lots.some((l: any) => l.id === preLotId)) {
              this.form.get('lotId')?.setValue(preLotId);
            }
          }
        }
      }
    });
  }

  loadSourceBalances(productId: string, lotId: string) {
    this.api.get<any>(`${API.stockBalances.base}?productId=${encodeURIComponent(productId)}&lotId=${encodeURIComponent(lotId)}&size=100`).subscribe({
      next: r => {
        if (r.success && r.data) {
          // Filter only AVAILABLE stock balances
          const items: StockBalanceDto[] = (r.data.items || []).filter(
            (b: StockBalanceDto) => (b.stockStatus?.name || '').toUpperCase() === 'AVAILABLE' && b.quantity > 0
          );
          this.sourceBalancesList.set(items);

          // Auto-select source balance if pre-filled
          if (this.data?.stockBalance?.id) {
            const preSbId = this.data.stockBalance.id;
            const targetSb = items.find(b => b.id === preSbId);
            if (targetSb) {
              this.form.get('sourceBalanceId')?.setValue(targetSb.id);
              this.onSourceBalanceChange(targetSb.id);
            }
          }
        }
      }
    });
  }

  onSourceBalanceChange(sourceBalanceId: string) {
    const found = this.sourceBalancesList().find(b => b.id === sourceBalanceId) || null;
    this.selectedSourceBalance.set(found);
    if (found) {
      this.form.get('quantity')?.setValue(found.quantity);
    }
  }

  onToWarehouseChange(warehouseId: string) {
    this.form.get('toLocationId')?.setValue('');
    if (!warehouseId) {
      this.toLocationsList.set([]);
      return;
    }
    this.api.get<any>(`${API.locations.base(warehouseId)}?size=100`).subscribe({
      next: r => {
        if (r.success && r.data) {
          this.toLocationsList.set(r.data.items || []);
        }
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    const sourceSb = this.selectedSourceBalance();
    if (!sourceSb || !sourceSb.warehouse || !sourceSb.location) {
      this.snackBar.open('Please select a valid source warehouse and location', 'OK', { duration: 3000 });
      return;
    }

    const qty = Number(this.form.value.quantity);
    if (qty > sourceSb.quantity) {
      this.snackBar.open(`Transfer quantity (${qty}) exceeds available balance (${sourceSb.quantity})`, 'OK', { duration: 4000 });
      return;
    }

    if (!this.submissionIdempotencyKey) {
      this.submissionIdempotencyKey = crypto.randomUUID();
    }
    this.submitting.set(true);

    const payload = {
      fromWarehouseId: sourceSb.warehouse.id,
      fromLocationId: sourceSb.location.id,
      toWarehouseId: this.form.value.toWarehouseId,
      toLocationId: this.form.value.toLocationId,
      productId: this.form.value.productId,
      lotId: this.form.value.lotId,
      quantity: qty,
    };

    this.api.post((API as any).stockTransfers.base || '/api/stock-transfers', payload, { idempotencyKey: this.submissionIdempotencyKey }).subscribe({
      next: (r: any) => {
        this.submitting.set(false);
        const message = r?.message || 'Stock transfer completed successfully';
        this.snackBar.open(message, 'OK', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (e: any) => {
        this.submitting.set(false);
        const errorMessage = e?.error?.message || e?.message || 'Failed to complete stock transfer';
        this.snackBar.open(errorMessage, 'Dismiss', { duration: 4000 });
      }
    });
  }

  private submissionIdempotencyKey: string | null = null;
}
