import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { ProductionLineDto } from '../../../../core/models/production-line.model';

@Component({
  selector: 'app-production-line-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Production Lines</h1>
      <p class="page-subtitle">Manage production lines and workflow routing</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let l">{{ l.lineCode }}</td>
          </ng-container>
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let l">{{ l.lineName }}</td>
          </ng-container>
          <ng-container matColumnDef="machines">
            <th mat-header-cell *matHeaderCellDef>Machines</th>
            <td mat-cell *matCellDef="let l">{{ l.machineCount }}</td>
          </ng-container>
          <ng-container matColumnDef="supervisor">
            <th mat-header-cell *matHeaderCellDef>Supervisor</th>
            <td mat-cell *matCellDef="let l">{{ l.supervisor }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let l">
              <span class="ff-badge" [class.ff-badge--active]="l.status === 'ACTIVE'" [class.ff-badge--cancelled]="l.status !== 'ACTIVE'">{{ l.status }}</span>
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
export class ProductionLineList {
  private api = inject(ApiService);
  items = signal<ProductionLineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'name', 'machines', 'supervisor', 'status'];
  constructor() { this.load(); }
  load() {
    this.api.get<{ items: ProductionLineDto[]; totalElements: number }>(`/api/production-lines?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
