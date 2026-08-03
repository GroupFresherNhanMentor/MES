import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';

import { ApiService } from '../../../core/services/api';
import { ApiResponse, PageResponse, PageParams } from '../../../core/models/api.model';
import { API } from '../../../configs/api-endpoints';
import type {
  QualityInspectionDto,
  QualityInspectionResultDto,
  QualityLookupDto,
  CreateQualityInspectionRequest,
  PassQcRequest,
  FailQcRequest,
  CreateQualityLookupRequest,
} from '../../../core/models/quality-inspection.model';

@Injectable({ providedIn: 'root' })
export class QualityInspectionService {
  private api = inject(ApiService);

  getInspections(params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<PageResponse<QualityInspectionDto>>> {
    return this.api.get<PageResponse<QualityInspectionDto>>(API.qualityInspections.base, params);
  }

  createInspection(body: CreateQualityInspectionRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.base, body);
  }

  getInspectionResults(inspectionId: string, params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<PageResponse<QualityInspectionResultDto>>> {
    return this.api.get<PageResponse<QualityInspectionResultDto>>(API.qualityInspections.results(inspectionId), params);
  }

  /** Fetch every result row for an inspection by walking all pages (size=100). */
  getAllInspectionResults(inspectionId: string): Observable<QualityInspectionResultDto[]> {
    return this.getInspectionResults(inspectionId, { page: 0, size: 100 }).pipe(
      switchMap((first) => {
        const total = first.data?.totalElements ?? 0;
        const firstItems = first.data?.items ?? [];
        const pagesNeeded = Math.ceil(total / 100);
        if (pagesNeeded <= 1) return of(firstItems);
        const remaining$: Observable<QualityInspectionResultDto[]>[] = [];
        for (let page = 1; page < pagesNeeded; page++) {
          remaining$.push(
            this.getInspectionResults(inspectionId, { page, size: 100 }).pipe(
              map((r) => r.data?.items ?? []),
            ),
          );
        }
        return forkJoin(remaining$).pipe(
          map((chunks) => firstItems.concat(...chunks)),
        );
      }),
    );
  }

  /** Remaining = inspection quantity minus the sum of all recorded result quantities (pass + fail). */
  getInspectionRemaining(inspectionId: string, quantity: number): Observable<number> {
    return this.getAllInspectionResults(inspectionId).pipe(
      map((results) => {
        const processed = results.reduce((sum, r) => sum + Number(r.quantity || 0), 0);
        return Math.max(0, quantity - processed);
      }),
    );
  }

  passInspection(inspectionId: string, body: PassQcRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.pass(inspectionId), body);
  }

  failInspection(inspectionId: string, body: FailQcRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.fail(inspectionId), body);
  }

  getDefectTypes(params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<PageResponse<QualityLookupDto>>> {
    return this.api.get<PageResponse<QualityLookupDto>>(API.qualityInspections.defectTypes, params);
  }

  createDefectType(body: CreateQualityLookupRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.defectTypes, body);
  }

  getQcActions(params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<PageResponse<QualityLookupDto>>> {
    return this.api.get<PageResponse<QualityLookupDto>>(API.qualityInspections.qcActions, params);
  }

  createQcAction(body: CreateQualityLookupRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.qcActions, body);
  }

  getQcStatuses(params?: PageParams | Record<string, string | number | boolean | undefined | null>): Observable<ApiResponse<PageResponse<QualityLookupDto>>> {
    return this.api.get<PageResponse<QualityLookupDto>>(API.qualityInspections.qcStatuses, params);
  }

  createQcStatus(body: CreateQualityLookupRequest): Observable<ApiResponse<void>> {
    return this.api.post<void>(API.qualityInspections.qcStatuses, body);
  }
}
