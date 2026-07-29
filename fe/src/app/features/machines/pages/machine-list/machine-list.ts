import { Component, inject, signal } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { MachineDto } from '../../../../core/models/machine.model';

@Component({
  selector: 'app-machine-list',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Machines</h1>
      <p class="page-subtitle">Manage machine registry and operational status</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let m">{{ m.machineCode }}</td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let m">{{ m.machineName }}</td>
          </ng-container>
          <ng-container matColumnDef="model">
            <th mat-header-cell *matHeaderCellDef>Model</th>
            <td mat-cell *matCellDef="let m">{{ m.model }}</td>
          </ng-container>
          <ng-container matColumnDef="line">
            <th mat-header-cell *matHeaderCellDef>Production Line</th>
            <td mat-cell *matCellDef="let m">{{ m.productionLineName }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let m">
              <span class="ff-badge" [class.ff-badge--active]="m.status === 'RUNNING'" [class.ff-badge--pending]="m.status === 'IDLE'" [class.ff-badge--on-hold]="m.status === 'MAINTENANCE'" [class.ff-badge--cancelled]="m.status === 'BREAKDOWN' || m.status === 'INACTIVE'">{{ m.status }}</span>
            </td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class MachineList {
  private api = inject(ApiService);
  items = signal<MachineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'name', 'model', 'line', 'status'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: MachineDto[]; totalElements: number }>(`/api/machines?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
