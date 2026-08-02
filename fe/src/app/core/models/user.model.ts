export interface UserDto {
  id: string;
  username: string;
  fullName: string | null;
  active: boolean;
  createdAt: string;
  role?: string;
  roles?: string[];
  status?: string;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  fullName?: string;
}

export interface UpdateUserRequest {
  fullName?: string;
}

export interface RoleDto {
  id: string;
  name: string;
  description: string | null;
}

export interface ReplaceUserRolesRequest {
  roleIds: string[];
}
