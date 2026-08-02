import { DatePipe, NgClass } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { forkJoin } from 'rxjs';

import { API } from '../../../../configs/api-endpoints';
import type { PageResponse } from '../../../../core/models/api.model';
import type { RoleDto, UserDto } from '../../../../core/models/user.model';
import { ApiService } from '../../../../core/services/api';
import { UserFormComponent } from '../user-form/user-form';

@Component({
  selector: 'app-user-list',
  imports: [
    DatePipe,
    FormsModule,
    NgClass,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTableModule,
    MatTooltipModule,
  ],
  templateUrl: './user-list.html',
})
export class UserListComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly users = signal<UserDto[]>([]);
  readonly total = signal(0);
  readonly page = signal(0);
  readonly size = signal(20);
  readonly keyword = signal('');
  readonly filterStatus = signal('');
  readonly rolesByUser = signal<Record<string, RoleDto[]>>({});
  readonly filteredUsers = computed(() => {
    const keyword = this.keyword().trim().toLowerCase();
    const status = this.filterStatus();
    return this.users().filter((user) => {
      const matchesKeyword =
        !keyword ||
        user.username.toLowerCase().includes(keyword) ||
        (user.fullName ?? '').toLowerCase().includes(keyword);
      const matchesStatus =
        !status ||
        (status === 'ACTIVE' && user.active) ||
        (status === 'INACTIVE' && !user.active);
      return matchesKeyword && matchesStatus;
    });
  });
  readonly displayedColumns = ['username', 'fullName', 'roles', 'status', 'createdAt', 'actions'];

  constructor() {
    this.load();
  }

  load(): void {
    this.api
      .get<PageResponse<UserDto>>(API.users.base, {
        page: this.page(),
        size: this.size(),
      })
      .subscribe((response) => {
        if (response.success && response.data) {
          const users = response.data.items ?? [];
          this.users.set(users);
          this.total.set(response.data.totalElements ?? 0);
          this.loadRoles(users);
        }
      });
  }

  onPage(event: PageEvent): void {
    this.page.set(event.pageIndex);
    this.size.set(event.pageSize);
    this.load();
  }

  search(): void {
    this.page.set(0);
  }

  roleNames(userId: string): string {
    return this.rolesByUser()[userId]?.map((role) => role.name).join(', ') || '-';
  }

  openCreate(): void {
    this.dialog
      .open(UserFormComponent, {
        width: '520px',
        panelClass: 'ff-dialog-panel',
      })
      .afterClosed()
      .subscribe((changed) => {
        if (changed) {
          this.load();
        }
      });
  }

  openEdit(user: UserDto): void {
    this.dialog
      .open(UserFormComponent, {
        width: '520px',
        panelClass: 'ff-dialog-panel',
        data: user,
      })
      .afterClosed()
      .subscribe((changed) => {
        if (changed) {
          this.load();
        }
      });
  }

  activate(user: UserDto): void {
    this.api.patch<void>(API.users.activate(user.id)).subscribe((response) => {
      if (response.success) {
        this.snackBar.open('User activated', 'OK', { duration: 2000 });
        this.load();
      }
    });
  }

  deactivate(user: UserDto): void {
    this.api.patch<void>(API.users.deactivate(user.id)).subscribe((response) => {
      if (response.success) {
        this.snackBar.open('User deactivated', 'OK', { duration: 2000 });
        this.load();
      }
    });
  }

  private loadRoles(users: UserDto[]): void {
    if (users.length === 0) {
      this.rolesByUser.set({});
      return;
    }

    forkJoin(users.map((user) => this.api.get<RoleDto[]>(API.users.roles(user.id)))).subscribe(
      (responses) => {
        const rolesByUser: Record<string, RoleDto[]> = {};
        users.forEach((user, index) => {
          const response = responses[index];
          rolesByUser[user.id] = response.success && response.data ? response.data : [];
        });
        this.rolesByUser.set(rolesByUser);
      },
    );
  }
}
