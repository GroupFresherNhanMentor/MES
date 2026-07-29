import type { SystemRole, UserStatus } from '../../configs/constants';

export interface UserDto {
  id: string;
  employeeId: string;
  username: string;
  fullName: string;
  email: string;
  phone?: string;
  role: SystemRole;
  status: UserStatus;
  department?: string;
  avatarUrl?: string;
}

export interface CreateUserRequest {
  employeeId: string;
  username: string;
  fullName: string;
  email: string;
  role: SystemRole;
  department?: string;
}

export interface UpdateUserRequest {
  fullName?: string;
  email?: string;
  phone?: string;
  role?: SystemRole;
  department?: string;
}

export interface UpdateUserStatusRequest {
  status: UserStatus;
}

export interface UserListParams {
  keyword?: string;
  role?: SystemRole;
  status?: UserStatus;
  page?: number;
  size?: number;
}
