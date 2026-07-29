import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

import { ApiService } from '../../../../core/services/api';
import type { LocationDto } from '../../../../core/models/location.model';

@Component({
  selector: 'app-location-list',
  imports: [DatePipe, MatTableModule, MatButtonModule, MatIconModule, MatCardModule, MatPaginatorModule],
  template: `
    <div class="page-header">
      <h1 class="page-heading">Locations</h1>
      <p class="page-subtitle">Warehouse: {{ warehouseId() }}</p>
    </div>
    <mat-card class="ff-card">
      <mat-card-content>
        <table mat-table [dataSource]="items()" class="full-width">
          <ng-container matColumnDef="code">
            <th mat-header-cell *matHeaderCellDef>Code</th>
            <td mat-cell *matCellDef="let l">{{ l.locationCode }}</td>
          </ng-container>
          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let l">{{ l.description }}</td>
          </ng-container>
          <ng-container matColumnDef="capacity">
            <th mat-header-cell *matHeaderCellDef>Load</th>
            <td mat-cell *matCellDef="let l">{{ l.currentLoad }}/{{ l.maxCapacity }}</td>
          </ng-container>
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let l">
              <span class="ff-badge" [class.ff-badge--active]="l.status === 'AVAILABLE'" [class.ff-badge--on-hold]="l.status === 'MAINTENANCE'" [class.ff-badge--cancelled]="l.status === 'INACTIVE'">{{ l.status }}</span>
            </td>
          </ng-container>
          <ng-container matColumnDef="created">
            <th mat-header-cell *matHeaderCellDef>Created</th>
            <td mat-cell *matCellDef="let l">{{ l.createdAt | date:'shortDate' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        <mat-paginator [length]="total()" [pageSize]="size()" [pageIndex]="page()" (page)="onPage($event)" [pageSizeOptions]="[10,20,50]"></mat-paginator>
      </mat-card-content>
    </mat-card>
  `,
})
export class LocationList {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  warehouseId = signal('');
  items = signal<LocationDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  displayedColumns = ['code', 'description', 'capacity', 'status', 'created'];
  constructor() {
    this.warehouseId.set(this.route.snapshot.params['warehouseId']);
    this.load();
  }
  load() {
    const wid = this.warehouseId();
    this.api.get<{ items: LocationDto[]; totalElements: number }>(`/api/warehouses/${wid}/locations?page=${this.page()}&size=${this.size()}`).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }
  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
}
