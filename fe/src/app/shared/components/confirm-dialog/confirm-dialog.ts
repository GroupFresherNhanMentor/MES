import { Component, inject } from '@angular/core';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmDialogData {
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmColor?: string;
}

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule],
  template: `
    <div style="padding: 24px; min-width: 360px;">
      <h2 mat-dialog-title style="color: #fafaf9; font-size: 18px; font-weight: 600; margin: 0 0 8px 0;">{{ data.title }}</h2>
      <mat-dialog-content style="color: #a8a29e; font-size: 14px; line-height: 1.5; margin: 0; padding: 0;">
        {{ data.message }}
      </mat-dialog-content>
      <mat-dialog-actions align="end" style="padding: 16px 0 0 0; min-height: auto; gap: 8px;">
        <button mat-stroked-button (click)="dialogRef.close(false)" style="color: #a8a29e; border-color: #44403c;">
          {{ data.cancelLabel ?? 'Cancel' }}
        </button>
        <button mat-flat-button [color]="data.confirmColor ?? 'primary'" (click)="dialogRef.close(true)" style="font-weight: 600;">
          {{ data.confirmLabel ?? 'Confirm' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
})
export class ConfirmDialog {
  readonly dialogRef = inject(MatDialogRef<ConfirmDialog>);
  readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
}
