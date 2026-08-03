import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, PageParams } from '../models/api.model';

export interface RequestOptions {
  idempotencyKey?: string;
  headers?: Record<string, string>;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  private buildHeaders(options?: RequestOptions): HttpHeaders {
    let headers = new HttpHeaders(options?.headers || {});
    if (options?.idempotencyKey) {
      headers = headers.set('X-Idempotency-Key', options.idempotencyKey);
    }
    return headers;
  }

  get<T>(url: string, params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<T>> {
    let httpParams = new HttpParams();
    if (params) {
      for (const [key, value] of Object.entries(params)) {
        if (value !== undefined && value !== null && value !== '') {
          httpParams = httpParams.set(key, String(value));
        }
      }
    }
    return this.http.get<ApiResponse<T>>(url, { params: httpParams });
  }

  getById<T>(url: string): Observable<ApiResponse<T>> {
    return this.http.get<ApiResponse<T>>(url);
  }

  post<T>(url: string, body?: unknown, options?: RequestOptions): Observable<ApiResponse<T>> {
    const headers = this.buildHeaders(options);
    return this.http.post<ApiResponse<T>>(url, body ?? {}, { headers });
  }

  put<T>(url: string, body?: unknown, options?: RequestOptions): Observable<ApiResponse<T>> {
    const headers = this.buildHeaders(options);
    return this.http.put<ApiResponse<T>>(url, body ?? {}, { headers });
  }

  patch<T>(url: string, body?: unknown, options?: RequestOptions): Observable<ApiResponse<T>> {
    const headers = this.buildHeaders(options);
    return this.http.patch<ApiResponse<T>>(url, body ?? {}, { headers });
  }

  delete<T>(url: string, options?: RequestOptions): Observable<ApiResponse<T>> {
    const headers = this.buildHeaders(options);
    return this.http.delete<ApiResponse<T>>(url, { headers });
  }
}
