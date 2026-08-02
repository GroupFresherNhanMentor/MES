import { Component, computed, input } from '@angular/core';
import { NgClass } from '@angular/common';

const STATUS_MAP: Record<string, string> = {
  ACTIVE: 'ff-badge--active',
  INACTIVE: 'ff-badge--pending',
  DISCONTINUED: 'ff-badge--cancelled',
  PENDING: 'ff-badge--pending',
  IN_PROGRESS: 'ff-badge--in-progress',
  COMPLETED: 'ff-badge--completed',
  ON_HOLD: 'ff-badge--on-hold',
  CANCELLED: 'ff-badge--cancelled',
  PAUSED: 'ff-badge--paused',
  DRAFT: 'ff-badge--draft',
  PLANNED: 'ff-badge--pending',
  MATERIAL_SHORTAGE: 'ff-badge--cancelled',
  READY_TO_PRODUCE: 'ff-badge--active',
  AVAILABLE: 'ff-badge--active',
  OCCUPIED: 'ff-badge--on-hold',
  MAINTENANCE: 'ff-badge--paused',
  RUNNING: 'ff-badge--active',
  IDLE: 'ff-badge--pending',
  BREAKDOWN: 'ff-badge--cancelled',
  ARCHIVED: 'ff-badge--paused',
  PASSED: 'ff-badge--completed',
  FAILED: 'ff-badge--cancelled',
  SCRAPPED: 'ff-badge--cancelled',
  RELEASED_INSP: 'ff-badge--active',
  OPEN: 'ff-badge--pending',
  CLOSED: 'ff-badge--completed',
  LOW: 'ff-badge--pending',
  MEDIUM: 'ff-badge--on-hold',
  HIGH: 'ff-badge--active',
  CRITICAL: 'ff-badge--cancelled',
  ADMIN: 'ff-badge--active',
  OPERATOR: 'ff-badge--pending',
  SUPERVISOR: 'ff-badge--on-hold',
  QUALITY: 'ff-badge--active',
};

@Component({
  selector: 'app-status-badge',
  imports: [NgClass],
  template: `
    <span class="ff-badge" [ngClass]="badgeClass()">
      {{ label() }}
    </span>
  `,
})
export class StatusBadge {
  readonly value = input.required<string>();
  readonly label = input<string>('');

  readonly badgeClass = computed(() => {
    const v = this.value();
    return STATUS_MAP[v] || STATUS_MAP[v.toUpperCase()] || 'ff-badge--pending';
  });
}
