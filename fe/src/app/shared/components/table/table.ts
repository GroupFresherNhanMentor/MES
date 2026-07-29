import { Component, computed, input, output, TemplateRef } from '@angular/core';
import { NgTemplateOutlet, NgClass } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';

export interface ColumnDef {
  key: string;
  label: string;
  sortable?: boolean;
  width?: string;
  align?: 'left' | 'center' | 'right';
  cellClass?: string;
}

@Component({
  selector: 'app-table',
  imports: [
    NgTemplateOutlet,
    NgClass,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatProgressBarModule,
    MatIconModule,
  ],
  template: `
    <div class="ff-card" style="overflow: hidden;">
      @if (loading()) {
        <mat-progress-bar mode="indeterminate" color="primary" style="position: absolute; top: 0; left: 0; right: 0;" />
      }
      <div style="overflow-x: auto; position: relative;">
        <table
          mat-table
          [dataSource]="dataSource()"
          matSort
          (matSortChange)="onSort($event)"
          style="width: 100%; min-width: 600px; border-collapse: collapse;"
        >
          @for (col of columns(); track col.key) {
            <ng-container [matColumnDef]="col.key">
              <th mat-header-cell *matHeaderCellDef mat-sort-header [style.width]="col.width" [style.text-align]="col.align ?? 'left'" [disabled]="!col.sortable" style="color: #a8a29e; font-size: 11px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; padding: 12px 16px; border-bottom: 1px solid #44403c;">
                {{ col.label }}
              </th>
              <td mat-cell *matCellDef="let row" [style.text-align]="col.align ?? 'left'" [ngClass]="col.cellClass ?? ''" style="padding: 8px 16px; border-bottom: 1px solid #292524; font-size: 13px; color: #fafaf9;">
                <ng-container [ngTemplateOutlet]="rowTpl()" [ngTemplateOutletContext]="{ $implicit: row, col: col }" />
              </td>
            </ng-container>
          }
          <tr mat-header-row *matHeaderRowDef="displayedColumns()" style="background: #1c1917;"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns();" [style.cursor]="!!rowClick() ? 'pointer' : ''" (click)="rowClick() ? rowClick()!(row) : null" style="transition: background 0.15s;"></tr>
          @if (empty()) {
            <tr>
              <td [attr.colspan]="displayedColumns().length" style="text-align: center; padding: 48px 16px; color: #78716c;">
                <mat-icon style="font-size: 40px; width: 40px; height: 40px; margin-bottom: 8px; opacity: 0.5;">inbox</mat-icon>
                <div style="font-size: 14px;">{{ emptyMessage() }}</div>
              </td>
            </tr>
          }
        </table>
      </div>
      @if (showPaginator()) {
        <mat-paginator
          [length]="totalElements()"
          [pageSize]="pageSize()"
          [pageSizeOptions]="pageSizeOptions()"
          [pageIndex]="pageIndex()"
          (page)="onPage($event)"
          style="border-top: 1px solid #44403c; background: #1c1917;"
        />
      }
    </div>
  `,
})
export class Table {
  readonly columns = input.required<ColumnDef[]>();
  readonly data = input<unknown[]>([]);
  readonly loading = input(false);
  readonly totalElements = input(0);
  readonly pageSize = input(20);
  readonly pageSizeOptions = input<number[]>([10, 20, 50, 100]);
  readonly pageIndex = input(0);
  readonly showPaginator = input(true);
  readonly emptyMessage = input('No data available');
  readonly rowTpl = input.required<TemplateRef<unknown>>();
  readonly rowClick = input<((row: unknown) => void) | undefined>(undefined);

  readonly sortChange = output<Sort>();
  readonly pageChange = output<PageEvent>();

  readonly displayedColumns = computed(() => this.columns().map((c) => c.key));
  readonly dataSource = computed(() => new MatTableDataSource<unknown>(this.data()));
  readonly empty = computed(() => !this.loading() && this.data().length === 0);

  onSort(sort: Sort): void {
    this.sortChange.emit(sort);
  }

  onPage(event: PageEvent): void {
    this.pageChange.emit(event);
  }
}
