import { Injectable, inject, PLATFORM_ID, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

import { API } from '../../configs/api-endpoints';
import { APP_CONSTANTS } from '../../configs/constants';
import { ApiResponse } from '../models/api.model';
import { LoginRequest, LoginResponse, RefreshTokenRequest, RefreshTokenResponse } from '../models/auth.model';
import { UserDto } from '../models/user.model';

function parseJwtPayload(token: string): any {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);

  readonly isLoggedIn = signal<boolean>(this.hasToken());

  constructor() {
    this.isLoggedIn.set(this.hasToken());
  }

  private hasToken(): boolean {
    return !!this.getToken();
  }

  login(payload: LoginRequest): Observable<LoginResponse> {
    return this.http.post<ApiResponse<LoginResponse>>(API.auth.login, payload).pipe(
      map((r) => r.data),
      tap((data) => {
        this.store(APP_CONSTANTS.tokenKey, data.accessToken);
        this.store(APP_CONSTANTS.refreshTokenKey, data.refreshToken);
        const decoded = parseJwtPayload(data.accessToken);
        const roles: string[] = decoded?.roles || [];
        const user = {
          id: decoded?.sub || '00000000-0000-0000-0000-000000000001',
          username: decoded?.username || payload.username,
          fullName: decoded?.username || payload.username,
          role: roles[0] || 'ADMIN',
          roles: roles,
          status: 'ACTIVE',
        };
        this.store(APP_CONSTANTS.userKey, JSON.stringify(user));
        this.isLoggedIn.set(true);
      }),
    );
  }

  refresh(refreshToken: string): Observable<RefreshTokenResponse> {
    const payload: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResponse<RefreshTokenResponse>>(API.auth.refresh, payload).pipe(
      map((r) => r.data),
      tap((data) => {
        this.store(APP_CONSTANTS.tokenKey, data.accessToken);
        this.store(APP_CONSTANTS.refreshTokenKey, data.refreshToken);
      }),
    );
  }

  logout(): void {
    this.clearAll();
    this.isLoggedIn.set(false);
    void this.router.navigateByUrl('/login');
  }

  getToken(): string | null {
    return this.load(APP_CONSTANTS.tokenKey);
  }

  getRefreshToken(): string | null {
    return this.load(APP_CONSTANTS.refreshTokenKey);
  }

  getCurrentUser(): UserDto | null {
    const raw = this.load(APP_CONSTANTS.userKey);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserDto;
    } catch {
      return null;
    }
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasAnyRole(...roles: string[]): boolean {
    const user = this.getCurrentUser();
    if (!user) return false;
    const userRoles = user.roles && user.roles.length > 0 ? user.roles : (user.role ? [user.role] : []);
    return roles.some((r) => userRoles.includes(r));
  }

  clearAll(): void {
    this.remove(APP_CONSTANTS.tokenKey);
    this.remove(APP_CONSTANTS.refreshTokenKey);
    this.remove(APP_CONSTANTS.userKey);
  }

  private store(key: string, value: string): void {
    if (this.isBrowser()) globalThis.localStorage.setItem(key, value);
  }

  private load(key: string): string | null {
    return this.isBrowser() ? globalThis.localStorage.getItem(key) : null;
  }

  private remove(key: string): void {
    if (this.isBrowser()) globalThis.localStorage.removeItem(key);
  }

  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }
}
