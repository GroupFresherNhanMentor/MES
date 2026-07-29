import { TestBed, ComponentFixture } from '@angular/core/testing';
import { BomList } from './bom-list';
import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { of } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('BomList', () => {
  let fixture: ComponentFixture<BomList>;
  let component: BomList;
  let api: jasmine.SpyObj<ApiService>;
  let auth: jasmine.SpyObj<AuthService>;

  const mockBoms = {
    success: true,
    data: {
      items: [
        {
          id: 'BOM-001',
          finishedProductId: 'P006',
          finishedProductName: 'Finished Widget A',
          finishedProductCode: 'WIDGET-A',
          version: 1,
          bomStatusId: 'BS-ACTIVE',
          bomStatusName: 'ACTIVE',
          createdBy: 'admin',
          createdAt: '2025-03-01T00:00:00Z',
          items: [],
        },
      ],
      totalElements: 1,
    },
  };

  const mockProducts = {
    success: true,
    data: {
      items: [
        { id: 'P006', code: 'WIDGET-A', name: 'Finished Widget A', productTypeId: 'PT-FIN' },
      ],
    },
  };

  const mockStatuses = {
    success: true,
    data: [
      { id: 'BS-ACTIVE', name: 'ACTIVE' },
      { id: 'BS-DRAFT', name: 'DRAFT' },
    ],
  };

  beforeEach(async () => {
    api = jasmine.createSpyObj('ApiService', ['get']);
    api.get.and.callFake((url: string) => {
      if (url.includes('/boms/statuses')) return of(mockStatuses);
      if (url.includes('/products')) return of(mockProducts);
      return of(mockBoms);
    });

    auth = jasmine.createSpyObj('AuthService', ['getCurrentUser']);
    auth.getCurrentUser.and.returnValue({ role: 'ADMIN' } as any);

    await TestBed.configureTestingModule({
      imports: [BomList],
      providers: [
        { provide: ApiService, useValue: api },
        { provide: AuthService, useValue: auth },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BomList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders heading and filter bar', () => {
    const heading = fixture.debugElement.query(By.css('h1'));
    expect(heading).toBeTruthy();
    expect(heading.nativeElement.textContent).toContain('Bill of Materials');
    const selects = fixture.debugElement.queryAll(By.css('mat-select'));
    expect(selects.length).toBeGreaterThanOrEqual(2);
  });

  it('shows Create BOM for ADMIN role', () => {
    const btn = fixture.debugElement.query(By.css('button[mat-flat-button]'));
    expect(btn).toBeTruthy();
    expect(btn.nativeElement.textContent).toContain('Create BOM');
  });

  it('hides Create BOM for non-privileged role', async () => {
    auth.getCurrentUser.and.returnValue({ role: 'OPERATOR' } as any);
    component = TestBed.createComponent(BomList).componentInstance;
    component.ngOnInit();
    fixture.detectChanges();
    await fixture.whenStable();
    const btn = fixture.debugElement.query(By.css('button[mat-flat-button]'));
    expect(btn).toBeFalsy();
  });

  it('loads BOMs on init and renders table rows', async () => {
    await fixture.whenStable();
    fixture.detectChanges();
    const rows = fixture.debugElement.queryAll(By.css('mat-row'));
    expect(rows.length).toBeGreaterThanOrEqual(1);
  });

  it('calls navigate on row click', () => {
    const router = jasmine.createSpyObj('Router', ['navigate']);
    (component as any).router = router;
    component.onRowClick({ id: 'BOM-001' } as any);
    expect(router.navigate).toHaveBeenCalledWith(['/boms', 'BOM-001']);
  });
});
