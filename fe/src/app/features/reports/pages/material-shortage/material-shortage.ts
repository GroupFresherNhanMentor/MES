import { Component, inject, signal, OnInit } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ReportService, MaterialShortageRow } from '../../services/report.service';

@Component({
  selector: 'app-material-shortage',
  standalone: true,
  imports: [MatTableModule, MatButtonModule, MatIconModule],
  template: `
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-2">
        <mat-icon class="text-warning">warning</mat-icon>
        <h2 class="text-lg font-semibold text-text-primary">Work Order Material Shortages</h2>
      </div>
      <button mat-stroked-button (click)="load()">
        <mat-icon>refresh</mat-icon> Refresh
      </button>
    </div>

    @if (items().length > 0) {
      <table mat-table [dataSource]="items()" class="w-full">
        <ng-container matColumnDef="workOrderCode">
          <th mat-header-cell *matHeaderCellDef>Work Order</th>
          <td mat-cell *matCellDef="let r" class="font-medium text-text-primary">{{ r.workOrderCode }}</td>
        </ng-container>
        <ng-container matColumnDef="materialCode">
          <th mat-header-cell *matHeaderCellDef>Material Code</th>
          <td mat-cell *matCellDef="let r">{{ r.materialCode }}</td>
        </ng-container>
        <ng-container matColumnDef="materialName">
          <th mat-header-cell *matHeaderCellDef>Material Name</th>
          <td mat-cell *matCellDef="let r">{{ r.materialName }}</td>
        </ng-container>
        <ng-container matColumnDef="required">
          <th mat-header-cell *matHeaderCellDef class="text-right">Required Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right font-medium">{{ r.requiredQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="reserved">
          <th mat-header-cell *matHeaderCellDef class="text-right">Reserved Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right text-text-muted">{{ r.reservedQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="available">
          <th mat-header-cell *matHeaderCellDef class="text-right">Available Stock</th>
          <td mat-cell *matCellDef="let r" class="text-right text-success">{{ r.availableQuantity }}</td>
        </ng-container>
        <ng-container matColumnDef="shortage">
          <th mat-header-cell *matHeaderCellDef class="text-right font-semibold">Shortage Qty</th>
          <td mat-cell *matCellDef="let r" class="text-right font-bold text-error">
            <span class="ff-badge ff-badge--cancelled">{{ r.shortageQuantity }}</span>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    } @else {
      <div class="flex flex-col items-center justify-center py-16 text-text-muted">
        <mat-icon class="text-5xl mb-2 opacity-40 text-success">check_circle</mat-icon>
        <span class="text-sm font-medium text-success">No material shortages detected! All work orders have sufficient stock.</span>
      </div>
    }
  `,
})
export class MaterialShortageComponent implements OnInit {
  private reportService = inject(ReportService);

  items = signal<MaterialShortageRow[]>([]);
  displayedColumns = [
    'workOrderCode',
    'materialCode',
    'materialName',
    'required',
    'reserved',
    'available',
    'shortage',
  ];

  ngOnInit() {
    this.load();
  }

  load() {
    this.reportService.getMaterialShortages().subscribe((r) => {
      if (r.success && r.data) {
        this.items.set(r.data);
      }
    });
  }
}
