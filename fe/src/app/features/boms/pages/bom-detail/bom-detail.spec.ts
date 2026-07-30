import { TestBed, ComponentFixture } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';

import { BomDetail } from './bom-detail';
import { ApiService } from '../../../../core/services/api';

describe('BomDetail', () => {
  let fixture: ComponentFixture<BomDetail>;
  let component: BomDetail;
  let api: { get: ReturnType<typeof vi.fn>; post: ReturnType<typeof vi.fn>; delete: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  const mockBomDraft = {
    id: 'BOM-001',
    finishedProductId: 'P006',
    finishedProductName: 'Finished Widget A',
    finishedProductCode: 'WIDGET-A',
    version: 1,
    bomStatusId: 'BS-DRAFT',
    bomStatusName: 'DRAFT',
    createdBy: 'admin',
    createdAt: '2025-03-01T00:00:00Z',
    items: [
      { id: 'BMI-001', bomId: 'BOM-001', materialProductId: 'P001', materialProductName: 'Steel Plate', materialProductCode: 'STEEL-PLATE', quantityPerUnit: 2, unit: 'KG', scrapRate: 5 },
    ],
  };

  const mockBomActive = {
    ...mockBomDraft,
    id: 'BOM-002',
    bomStatusId: 'BS-ACTIVE',
    bomStatusName: 'ACTIVE',
  };

  beforeEach(async () => {
    api = {
      get: vi.fn().mockReturnValue(of({ success: true, data: mockBomDraft })),
      post: vi.fn(),
      delete: vi.fn(),
    };
    router = { navigate: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [BomDetail],
      providers: [
        { provide: ApiService, useValue: api },
        { provide: Router, useValue: router },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => 'BOM-001' } },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BomDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders BOM details and items for DRAFT BOM', () => {
    expect(component.bom()?.finishedProductName).toBe('Finished Widget A');
    expect(component.isDraft()).toBe(true);
    expect(component.canActivate()).toBe(true);
    expect(component.canNewVersion()).toBe(false);

    const activateBtn = fixture.debugElement.query(By.css('button.bg-success'));
    expect(activateBtn).toBeTruthy();
  });

  it('shows New Version button and hides Add/Remove for ACTIVE BOM (FR-BOM-003)', async () => {
    api.get.mockReturnValue(of({ success: true, data: mockBomActive }));
    component.load();
    fixture.detectChanges();

    expect(component.isDraft()).toBe(false);
    expect(component.canActivate()).toBe(false);
    expect(component.canNewVersion()).toBe(true);

    const addBtn = fixture.debugElement.query(By.css('button.bg-primary'));
    // Add Component button should not exist in table header when ACTIVE
    expect(addBtn).toBeFalsy();
  });

  it('calls activate API when onActivate is triggered', () => {
    api.post.mockReturnValue(of({ success: true, data: { ...mockBomDraft, bomStatusName: 'ACTIVE' } }));
    component.onActivate();

    expect(api.post).toHaveBeenCalledWith(expect.stringMatching(/\/boms\/BOM-001\/activate$/), {});
  });
});
