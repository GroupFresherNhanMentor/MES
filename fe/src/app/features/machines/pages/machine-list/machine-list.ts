import { Component, inject, signal } from '@angular/core';
import { NgClass } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, type PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';

import { ApiService } from '../../../../core/services/api';
import type { MachineDto } from '../../../../core/models/machine.model';
import { MachineFormComponent } from '../machine-form/machine-form';

@Component({
  selector: 'app-machine-list',
  imports: [
    NgClass, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatPaginatorModule,
    MatSnackBarModule, MatCardModule, MatDialogModule, MatTooltipModule,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private api = inject(ApiService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  items = signal<MachineDto[]>([]);
  total = signal(0);
  page = signal(0);
  size = signal(20);
  keyword = signal('');
  displayedColumns = ['code', 'name', 'machineStatusName', 'actions'];

  constructor() { this.load(); }

  load() {
    let url = `/api/machines?page=${this.page()}&size=${this.size()}`;
    if (this.keyword()) url += `&keyword=${encodeURIComponent(this.keyword())}`;
    this.api.get<{ items: MachineDto[]; totalElements: number }>(url).subscribe(r => {
      if (r.success) { this.items.set(r.data.items); this.total.set(r.data.totalElements); }
    });
  }

  onPage(e: PageEvent) { this.page.set(e.pageIndex); this.size.set(e.pageSize); this.load(); }
  search() { this.page.set(0); this.load(); }

  openCreate() {
    this.dialog.open(MachineFormComponent, { width: '500px', panelClass: 'ff-dialog-panel' }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  openEdit(m: MachineDto) {
    this.dialog.open(MachineFormComponent, { width: '500px', panelClass: 'ff-dialog-panel', data: m }).afterClosed().subscribe(r => { if (r) this.load(); });
  }

  deactivate(m: MachineDto) {
    this.api.put(`/api/machines/${m.id}/deactivate`, {}).subscribe(r => {
      if (r.success) { this.snackBar.open('Deactivated', 'OK', { duration: 2000 }); this.load(); }
    });
  }
}
