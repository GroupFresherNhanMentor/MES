import { TestBed, ComponentFixture } from '@angular/core/testing';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

import { BomCreateDialog } from './bom-create-dialog';
import { ApiService } from '../../../../core/services/api';
import { MatDialogRef } from '@angular/material/dialog';

describe('BomCreateDialog', () => {
  let fixture: ComponentFixture<BomCreateDialog>;
  let component: BomCreateDialog;
  let api: { get: ReturnType<typeof vi.fn>; post: ReturnType<typeof vi.fn> };
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  const mockProducts = {
    success: true,
    data: {
      items: [
        { id: 'P006', code: 'WIDGET-A', name: 'Finished Widget A', productTypeId: 'PT-FIN', productTypeName: 'FINISHED_GOOD' },
        { id: 'P004', code: 'CIRCUIT-BOARD', name: 'Circuit Board', productTypeId: 'PT-SUB', productTypeName: 'SUB_ASSEMBLY' },
      ],
    },
  };

  beforeEach(async () => {
    api = {
      get: vi.fn().mockReturnValue(of(mockProducts)),
      post: vi.fn(),
    };
    dialogRef = {
      close: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [BomCreateDialog],
      providers: [
        { provide: ApiService, useValue: api },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BomCreateDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('renders dialog title and form fields', () => {
    const title = fixture.debugElement.query(By.css('h2'));
    expect(title).toBeTruthy();
    expect(title.nativeElement.textContent).toContain('Create Bill of Materials');
    const versionInput = fixture.debugElement.query(By.css('input[type="number"]'));
    expect(versionInput).toBeTruthy();
  });

  it('loads products filtered by FINISHED_GOOD / SEMI_FINISHED', () => {
    expect(component.productsList().length).toBeGreaterThanOrEqual(1);
    expect(api.get).toHaveBeenCalledWith(expect.stringMatching(/productTypeId=PT-FIN,PT-SUB/));
  });

  it('prevents submit when form is invalid', () => {
    component.form.get('finishedProductId')?.setValue(null);
    component.form.get('finishedProductId')?.markAsTouched();
    fixture.detectChanges();
    expect(component.form.invalid).toBe(true);
    const submitBtn = fixture.debugElement.query(By.css('button[type="submit"]'));
    expect(submitBtn.nativeElement.disabled).toBe(true);
  });

  it('calls API and closes dialog on successful submit', () => {
    const mockBom = { id: 'BOM-NEW', finishedProductId: 'P006', version: 1, bomStatusName: 'DRAFT' };
    api.post.mockReturnValue(of({ success: true, data: mockBom }));

    component.form.patchValue({ finishedProductId: 'P006', version: 1 });
    component.onSubmit();

    expect(api.post).toHaveBeenCalledWith(expect.stringMatching(/\/boms$/), {
      finishedProductId: 'P006',
      version: 1,
    });
    expect(dialogRef.close).toHaveBeenCalledWith(mockBom);
  });

  it('shows error message on API failure', () => {
    api.post.mockReturnValue(throwError(() => ({ error: { message: 'Server error' } })));

    component.form.patchValue({ finishedProductId: 'P006', version: 1 });
    component.onSubmit();
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('Server error');
    const errorEl = fixture.debugElement.query(By.css('.text-error'));
    expect(errorEl).toBeTruthy();
    expect(errorEl.nativeElement.textContent).toContain('Server error');
  });

  it('closes dialog on cancel', () => {
    component.onCancel();
    expect(dialogRef.close).toHaveBeenCalledWith(null);
  });
});
