import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { API } from '../../../../configs/api-endpoints';
import type {
  CreateUserRequest,
  ReplaceUserRolesRequest,
  RoleDto,
  UpdateUserRequest,
  UserDto,
} from '../../../../core/models/user.model';
import { ApiService } from '../../../../core/services/api';

@Component({
  selector: 'app-user-form',
  imports: [
    FormsModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './user-form.html',
})
export class UserFormComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialogRef = inject(MatDialogRef<UserFormComponent>);
  private readonly snackBar = inject(MatSnackBar);

  readonly data = inject(MAT_DIALOG_DATA, { optional: true }) as UserDto | null;
  readonly saving = signal(false);
  readonly rolesLoading = signal(true);

  username = '';
  password = '';
  fullName = '';
  selectedRoleIds: string[] = [];
  roles: RoleDto[] = [];

  ngOnInit(): void {
    if (this.data) {
      this.username = this.data.username;
      this.fullName = this.data.fullName ?? '';
      this.loadAssignedRoles();
    }

    this.api.get<RoleDto[]>(API.roles.base).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.roles = response.data;
        }
        this.rolesLoading.set(false);
      },
      error: () => this.rolesLoading.set(false),
    });
  }

  isValid(): boolean {
    const usernameValid = this.data !== null || this.username.trim().length >= 3;
    const passwordValid = this.data !== null || this.password.length >= 8;
    const fullNameValid = this.fullName.trim().length <= 255;
    return (
      usernameValid &&
      passwordValid &&
      fullNameValid &&
      this.selectedRoleIds.length > 0 &&
      !this.rolesLoading() &&
      !this.saving()
    );
  }

  save(): void {
    if (!this.isValid()) {
      return;
    }

    this.saving.set(true);
    if (this.data) {
      const request: UpdateUserRequest = {
        fullName: this.fullName.trim(),
      };
      this.api.put<UserDto>(API.users.byId(this.data.id), request).subscribe({
        next: (response) => {
          if (response.success && response.data) {
            this.saveRoles(response.data.id, 'User updated');
          } else {
            this.saving.set(false);
          }
        },
        error: () => this.saving.set(false),
      });
      return;
    }

    const request: CreateUserRequest = {
      username: this.username.trim(),
      password: this.password,
      fullName: this.fullName.trim() || undefined,
    };
    this.api.post<UserDto>(API.users.base, request).subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.saveRoles(response.data.id, 'User created');
        } else {
          this.saving.set(false);
        }
      },
      error: () => this.saving.set(false),
    });
  }

  private loadAssignedRoles(): void {
    if (!this.data) {
      return;
    }

    this.api.get<RoleDto[]>(API.users.roles(this.data.id)).subscribe((response) => {
      if (response.success && response.data) {
        this.selectedRoleIds = response.data.map((role) => role.id);
      }
    });
  }

  private saveRoles(userId: string, successMessage: string): void {
    const request: ReplaceUserRolesRequest = {
      roleIds: this.selectedRoleIds,
    };
    this.api.put<RoleDto[]>(API.users.roles(userId), request).subscribe({
      next: (response) => {
        this.saving.set(false);
        if (response.success) {
          this.snackBar.open(successMessage, 'OK', { duration: 2000 });
          this.dialogRef.close(true);
        }
      },
      error: () => this.saving.set(false),
    });
  }
}
