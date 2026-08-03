import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { describe, beforeEach, expect, it, vi } from 'vitest';
import { of } from 'rxjs';

import { API } from '../../../../configs/api-endpoints';
import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { WorkOrderList } from './work-order-list';

describe('WorkOrderList', () => {
  let fixture: ComponentFixture<WorkOrderList>;
  let component: WorkOrderList;
  let api: { get: ReturnType<typeof vi.fn> };
  let auth: { getCurrentUser: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    api = {
      get: vi.fn().mockImplementation((url: string) => {
        if (url === API.products.base) {
          return of({ success: true, data: { items: [{ id: 'product-1', code: 'FG-001', name: 'Finished good' }] } });
        }
        if (url === API.workOrders.statuses || url === API.workOrders.priorities) {
          return of({ success: true, data: [] });
        }
        return of({
          success: true,
          data: {
            items: [{
              id: 'work-order-1', code: 'WO-001', finishedProductId: 'product-1', bomId: 'bom-1', plannedQuantity: 10,
              plannedStartDate: null, plannedEndDate: null, priorityId: null, workOrderStatusId: 'status-1', createdAt: '2026-08-01T00:00:00Z',
            }],
            totalElements: 1, totalPages: 1, pageNumber: 0, pageSize: 20,
          },
        });
      }),
    };
    auth = { getCurrentUser: vi.fn().mockReturnValue({ role: 'PLANNER' }) };

    await TestBed.configureTestingModule({
      imports: [WorkOrderList],
      providers: [
        provideRouter([]),
        { provide: ApiService, useValue: api },
        { provide: AuthService, useValue: auth },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(WorkOrderList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('loads the work-order page through the configured endpoint', () => {
    expect(api.get).toHaveBeenCalledWith(API.workOrders.base, expect.objectContaining({ page: 0, size: 20 }));
    expect(component.items()[0].code).toBe('WO-001');
  });

  it('resolves the finished-product label from the loaded products', () => {
    expect(component.productLabel('product-1')).toBe('FG-001 - Finished good');
  });

  it('allows ADMIN and PLANNER to create work orders after statuses load', () => {
    expect(component.canCreate()).toBe(true);
    auth.getCurrentUser.mockReturnValue({ role: 'ADMIN' });
    expect(component.canCreate()).toBe(true);
    auth.getCurrentUser.mockReturnValue({ role: 'OPERATOR' });
    expect(component.canCreate()).toBe(false);
  });
});
