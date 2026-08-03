import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import { API } from '../../../../configs/api-endpoints';
import type { PageResponse } from '../../../../core/models/api.model';
import type { LocationDto } from '../../../../core/models/location.model';
import type { MachineDto } from '../../../../core/models/machine.model';
import type { ProductDto } from '../../../../core/models/product.model';
import type { WarehouseDto } from '../../../../core/models/warehouse.model';
import type {
  CompleteWorkOrderRequest,
  ReserveWorkOrderMaterialsResponse,
  StartWorkOrderRequest,
  UpdateWorkOrderRequest,
  WorkOrderDto,
  WorkOrderLookupDto,
  WorkOrderMaterialShortageDto,
  ReservedMaterialAllocationDto,
} from '../../../../core/models/work-order.model';
import { ApiService } from '../../../../core/services/api';
import { AuthService } from '../../../../core/services/auth';
import { ConfirmDialog } from '../../../../shared/components/confirm-dialog/confirm-dialog';
import { StatusBadge } from '../../../../shared/components/status-badge/status-badge';

interface ProductionLineOption {
  id: string;
  code: string;
  name: string;
}

type ActionPanel = 'edit' | 'start' | 'complete' | null;

@Component({
  selector: 'app-work-order-detail',
  imports: [
    DatePipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    StatusBadge,
  ],
  template: `
    <div class="mb-6 flex flex-wrap items-start justify-between gap-4">
      <div>
        <button mat-button class="!mb-2 !px-0 text-text-secondary" (click)="backToList()"><mat-icon>arrow_back</mat-icon> Back to work orders</button>
        <h1 class="m-0 text-2xl font-semibold text-text-primary">{{ workOrder()?.code ?? 'Work order details' }}</h1>
        <p class="mb-0 mt-1 text-sm text-text-secondary">Execute production operations using the current backend lifecycle.</p>
      </div>
      @if (workOrder()) {
        <app-status-badge [value]="statusName()" [label]="statusName()"></app-status-badge>
      }
    </div>

    @if (loading()) {
      <mat-progress-bar mode="indeterminate"></mat-progress-bar>
    }
    @if (error()) {
      <div class="mb-4 rounded-md border border-error/40 bg-error/10 px-4 py-3 text-sm text-error">{{ error() }}</div>
    }

    @if (workOrder(); as order) {
      <div class="mb-4 rounded-md border border-warning/40 bg-warning/10 px-4 py-3 text-sm text-text-secondary">
        Lifecycle actions are available only when the current status and your role permit them. The backend remains the final authority.
      </div>

      <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div class="space-y-4">
          <mat-card>
            <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-text-primary">Work order</mat-card-title></mat-card-header>
            <mat-card-content class="grid grid-cols-1 gap-x-6 gap-y-4 sm:grid-cols-2">
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">Finished product</p><p class="mt-1 text-text-primary">{{ productLabel(order.finishedProductId) }}</p></div>
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">Planned quantity</p><p class="mt-1 text-text-primary">{{ order.plannedQuantity }}</p></div>
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">Planned start</p><p class="mt-1 text-text-primary">{{ order.plannedStartDate ? (order.plannedStartDate | date:'medium') : '-' }}</p></div>
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">Planned end</p><p class="mt-1 text-text-primary">{{ order.plannedEndDate ? (order.plannedEndDate | date:'medium') : '-' }}</p></div>
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">BOM reference</p><p class="mt-1 font-mono text-sm text-text-secondary">{{ order.bomId }}</p></div>
              <div><p class="m-0 text-xs uppercase tracking-wide text-text-muted">Priority</p><p class="mt-1 text-text-primary">{{ priorityLabel(order.priorityId) }}</p></div>
            </mat-card-content>
          </mat-card>

          <mat-card>
            <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-text-primary">Material requirements</mat-card-title></mat-card-header>
            <mat-card-content>
              @if (!order.materials?.length) {
                <p class="text-sm text-text-muted">No material requirements returned for this work order.</p>
              } @else {
                <div class="overflow-x-auto">
                  <table class="w-full min-w-[620px] text-left text-sm">
                    <thead class="border-b border-border-default text-text-secondary"><tr><th class="px-3 py-2">Material</th><th class="px-3 py-2">Required</th><th class="px-3 py-2">Reserved</th><th class="px-3 py-2">Consumed</th></tr></thead>
                    <tbody>
                      @for (material of order.materials; track material.id) {
                        <tr class="border-b border-border-light"><td class="px-3 py-3 text-text-primary">{{ productLabel(material.materialProductId) }}</td><td class="px-3 py-3">{{ material.requiredQuantity }}</td><td class="px-3 py-3">{{ material.reservedQuantity }}</td><td class="px-3 py-3">{{ material.consumedQuantity }}</td></tr>
                      }
                    </tbody>
                  </table>
                </div>
              }
            </mat-card-content>
          </mat-card>

          @if (reservationAllocations().length) {
            <mat-card>
              <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-text-primary">Latest reservation allocation</mat-card-title></mat-card-header>
              <mat-card-content>
                <p class="mt-0 text-sm text-text-secondary">FIFO allocations created from the configured raw-material warehouse.</p>
                <div class="overflow-x-auto"><table class="w-full min-w-[700px] text-left text-sm"><thead class="border-b border-border-default text-text-secondary"><tr><th class="px-3 py-2">Material</th><th class="px-3 py-2">Lot</th><th class="px-3 py-2">Warehouse</th><th class="px-3 py-2">Location</th><th class="px-3 py-2">Reserved qty</th></tr></thead><tbody>@for (allocation of reservationAllocations(); track allocation.lotId + allocation.materialProductId) { <tr class="border-b border-border-light"><td class="px-3 py-3 text-text-primary">{{ productLabel(allocation.materialProductId) }}</td><td class="px-3 py-3 font-mono text-xs">{{ allocation.lotId }}</td><td class="px-3 py-3 font-mono text-xs">{{ allocation.warehouseId }}</td><td class="px-3 py-3 font-mono text-xs">{{ allocation.locationId }}</td><td class="px-3 py-3">{{ allocation.reservedQuantity }}</td></tr> }</tbody></table></div>
              </mat-card-content>
            </mat-card>
          }

          @if (reservationShortages().length) {
            <mat-card>
              <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-error">Material shortages</mat-card-title></mat-card-header>
              <mat-card-content>
                <p class="mt-0 text-sm text-text-secondary">No material was reserved. Replenish stock, then reserve again.</p>
                <div class="overflow-x-auto"><table class="w-full min-w-[600px] text-left text-sm"><thead class="border-b border-border-default text-text-secondary"><tr><th class="px-3 py-2">Material</th><th class="px-3 py-2">Required</th><th class="px-3 py-2">Available</th><th class="px-3 py-2">Shortage</th></tr></thead><tbody>@for (shortage of reservationShortages(); track shortage.materialProductId) { <tr class="border-b border-border-light"><td class="px-3 py-3 text-text-primary">{{ productLabel(shortage.materialProductId) }}</td><td class="px-3 py-3">{{ shortage.requiredQuantity }}</td><td class="px-3 py-3">{{ shortage.availableQuantity }}</td><td class="px-3 py-3 text-error">{{ shortage.shortageQuantity }}</td></tr> }</tbody></table></div>
              </mat-card-content>
            </mat-card>
          }

          <mat-card>
            <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-text-primary">Event history</mat-card-title></mat-card-header>
            <mat-card-content>
              @if (!order.events?.length) {
                <p class="text-sm text-text-muted">No events returned for this work order.</p>
              } @else {
                <div class="space-y-3">
                  @for (event of order.events; track event.id) {
                    <div class="border-l-2 border-primary pl-4"><p class="m-0 text-sm text-text-primary">{{ eventTypeLabel(event.eventTypeId) }}</p><p class="mb-0 mt-1 text-xs text-text-muted">{{ event.eventTimestamp | date:'medium' }} · Operator {{ shortId(event.operatorId) }}{{ event.note ? ' · ' + event.note : '' }}</p></div>
                  }
                </div>
              }
            </mat-card-content>
          </mat-card>
        </div>

        <div class="space-y-4">
          <mat-card>
            <mat-card-header class="!pb-4"><mat-card-title class="!text-xl !font-semibold !text-text-primary">Available actions</mat-card-title></mat-card-header>
            <mat-card-content class="flex flex-col gap-2">
              @if (canEdit()) { <button mat-stroked-button (click)="openPanel('edit')"><mat-icon>edit</mat-icon> Update plan</button> }
              @if (canReserve()) { <button mat-stroked-button (click)="confirmAction('reserve')"><mat-icon>inventory</mat-icon> Reserve materials</button> }
              @if (canRelease()) { <button mat-stroked-button (click)="confirmAction('release')"><mat-icon>undo</mat-icon> Release materials</button> }
              @if (canStart()) { <button mat-stroked-button (click)="openPanel('start')"><mat-icon>play_arrow</mat-icon> Start production</button> }
              @if (canPause()) { <button mat-stroked-button (click)="confirmAction('pause')"><mat-icon>pause</mat-icon> Pause production</button> }
              @if (canResume()) { <button mat-stroked-button (click)="confirmAction('resume')"><mat-icon>play_arrow</mat-icon> Resume production</button> }
              @if (canComplete()) { <button mat-stroked-button (click)="openPanel('complete')"><mat-icon>task_alt</mat-icon> Complete production</button> }
              @if (canCancel()) { <button mat-stroked-button class="!text-error" (click)="confirmAction('cancel')"><mat-icon>cancel</mat-icon> Cancel work order</button> }
            </mat-card-content>
          </mat-card>

          @if (panel() === 'edit') {
            <mat-card><mat-card-header class="!pb-4"><mat-card-title class="!text-lg !text-text-primary">Update plan</mat-card-title></mat-card-header><mat-card-content class="flex flex-col gap-3">
              <mat-form-field appearance="outline"><mat-label>Code</mat-label><input matInput [(ngModel)]="editCode"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Planned quantity</mat-label><input matInput type="number" min="0.0001" [(ngModel)]="editQuantity"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Planned start</mat-label><input matInput type="datetime-local" [(ngModel)]="editStart"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Planned end</mat-label><input matInput type="datetime-local" [(ngModel)]="editEnd"></mat-form-field>
              <div class="flex justify-end gap-2"><button mat-button (click)="closePanel()">Cancel</button><button mat-raised-button class="ff-btn-primary" [disabled]="submitting()" (click)="update()">Save</button></div>
            </mat-card-content></mat-card>
          }

          @if (panel() === 'start') {
            <mat-card><mat-card-header class="!pb-4"><mat-card-title class="!text-lg !text-text-primary">Start production</mat-card-title></mat-card-header><mat-card-content class="flex flex-col gap-3">
              <mat-form-field appearance="outline"><mat-label>Machine</mat-label><mat-select [(ngModel)]="startMachineId">@for (machine of machines(); track machine.id) { <mat-option [value]="machine.id">{{ machine.code }} - {{ machine.name }}</mat-option> }</mat-select></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Production line (optional)</mat-label><mat-select [(ngModel)]="productionLineId"><mat-option value="">Use machine line</mat-option>@for (line of lines(); track line.id) { <mat-option [value]="line.id">{{ line.code }} - {{ line.name }}</mat-option> }</mat-select></mat-form-field>
              <div class="flex justify-end gap-2"><button mat-button (click)="closePanel()">Cancel</button><button mat-raised-button class="ff-btn-primary" [disabled]="!startMachineId || submitting()" (click)="start()">Start</button></div>
            </mat-card-content></mat-card>
          }

          @if (panel() === 'complete') {
            <mat-card><mat-card-header class="!pb-4"><mat-card-title class="!text-lg !text-text-primary">Complete production</mat-card-title></mat-card-header><mat-card-content class="flex flex-col gap-3">
              <mat-form-field appearance="outline"><mat-label>Actual quantity</mat-label><input matInput type="number" min="0" [(ngModel)]="actualQuantity"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Good quantity</mat-label><input matInput type="number" min="0" [(ngModel)]="goodQuantity"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Defect quantity</mat-label><input matInput type="number" min="0" [(ngModel)]="defectQuantity"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Scrap quantity</mat-label><input matInput type="number" min="0" [(ngModel)]="scrapQuantity"></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Output warehouse</mat-label><mat-select [(ngModel)]="outputWarehouseId" (selectionChange)="loadLocations()">@for (warehouse of warehouses(); track warehouse.id) { <mat-option [value]="warehouse.id">{{ warehouse.code }} - {{ warehouse.name }}</mat-option> }</mat-select></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Output location</mat-label><mat-select [(ngModel)]="outputLocationId" [disabled]="!outputWarehouseId">@for (location of locations(); track location.id) { <mat-option [value]="location.id">{{ location.code }} - {{ location.name }}</mat-option> }</mat-select></mat-form-field>
              <mat-form-field appearance="outline"><mat-label>Note</mat-label><textarea matInput maxlength="500" [(ngModel)]="completionNote"></textarea></mat-form-field>
              <div class="flex justify-end gap-2"><button mat-button (click)="closePanel()">Cancel</button><button mat-raised-button class="ff-btn-primary" [disabled]="!canSubmitCompletion() || submitting()" (click)="complete()">Complete</button></div>
            </mat-card-content></mat-card>
          }
        </div>
      </div>
    }
  `,
})
export class WorkOrderDetail implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly workOrder = signal<WorkOrderDto | null>(null);
  readonly products = signal<ProductDto[]>([]);
  readonly machines = signal<MachineDto[]>([]);
  readonly lines = signal<ProductionLineOption[]>([]);
  readonly warehouses = signal<WarehouseDto[]>([]);
  readonly locations = signal<LocationDto[]>([]);
  readonly statuses = signal<WorkOrderLookupDto[]>([]);
  readonly priorities = signal<WorkOrderLookupDto[]>([]);
  readonly eventTypes = signal<WorkOrderLookupDto[]>([]);
  readonly reservationAllocations = signal<ReservedMaterialAllocationDto[]>([]);
  readonly reservationShortages = signal<WorkOrderMaterialShortageDto[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly error = signal('');
  readonly panel = signal<ActionPanel>(null);

  editCode = '';
  editQuantity = 0;
  editStart = '';
  editEnd = '';
  startMachineId = '';
  productionLineId = '';
  actualQuantity = 0;
  goodQuantity = 0;
  defectQuantity = 0;
  scrapQuantity = 0;
  outputWarehouseId = '';
  outputLocationId = '';
  completionNote = '';

  ngOnInit(): void {
    this.loadReferences();
    this.load();
  }

  backToList(): void {
    void this.router.navigate(['/work-orders']);
  }

  openPanel(panel: ActionPanel): void {
    this.panel.set(panel);
    if (panel === 'edit') this.populateEditForm();
  }

  closePanel(): void {
    this.panel.set(null);
  }

  productLabel(id: string): string {
    const product = this.products().find((item) => item.id === id);
    return product ? `${product.code} - ${product.name}` : this.shortId(id);
  }

  priorityLabel(id?: string | null): string {
    return this.priorities().find((priority) => priority.id === id)?.name ?? this.shortId(id);
  }

  eventTypeLabel(id: string): string {
    return this.eventTypes().find((eventType) => eventType.id === id)?.name ?? this.shortId(id);
  }

  statusName(): string {
    const statusId = this.workOrder()?.workOrderStatusId;
    return this.statuses().find((status) => status.id === statusId)?.name ?? 'PENDING';
  }

  shortId(id?: string | null): string {
    return id ? `${id.slice(0, 8)}...` : '-';
  }

  canEdit(): boolean { return this.hasRole('ADMIN', 'PLANNER') && ['DRAFT', 'PLANNED'].includes(this.statusName()); }
  canReserve(): boolean { return this.hasRole('PLANNER') && ['PLANNED', 'MATERIAL_SHORTAGE'].includes(this.statusName()); }
  canRelease(): boolean { return this.hasRole('ADMIN', 'PLANNER') && ['PLANNED', 'READY_TO_PRODUCE'].includes(this.statusName()); }
  canStart(): boolean { return this.hasRole('ADMIN', 'PLANNER', 'OPERATOR') && this.statusName() === 'READY_TO_PRODUCE'; }
  canPause(): boolean { return this.hasRole('ADMIN', 'PLANNER', 'OPERATOR') && this.statusName() === 'IN_PROGRESS'; }
  canResume(): boolean { return this.hasRole('ADMIN', 'PLANNER', 'OPERATOR') && this.statusName() === 'PAUSED'; }
  canComplete(): boolean { return this.hasRole('OPERATOR') && this.statusName() === 'IN_PROGRESS'; }
  canCancel(): boolean { return this.hasRole('ADMIN', 'PLANNER') && ['DRAFT', 'PLANNED', 'MATERIAL_SHORTAGE', 'READY_TO_PRODUCE'].includes(this.statusName()); }

  update(): void {
    const order = this.workOrder();
    if (!order || this.editQuantity <= 0) return;
    const payload: UpdateWorkOrderRequest = {
      code: this.editCode.trim() || undefined,
      plannedQuantity: Number(this.editQuantity),
      plannedStartDate: this.toIso(this.editStart),
      plannedEndDate: this.toIso(this.editEnd),
    };
    this.submit(API.workOrders.byId(order.id), payload, 'Work order updated.', 'put');
  }

  start(): void {
    const order = this.workOrder();
    if (!order || !this.startMachineId) return;
    const payload: StartWorkOrderRequest = { machineId: this.startMachineId, productionLineId: this.productionLineId || undefined };
    this.submit(API.workOrders.start(order.id), payload, 'Production started.');
  }

  complete(): void {
    const order = this.workOrder();
    if (!order || !this.canSubmitCompletion()) return;
    const payload: CompleteWorkOrderRequest = {
      actualQuantity: Number(this.actualQuantity),
      goodQuantity: Number(this.goodQuantity),
      defectQuantity: Number(this.defectQuantity),
      scrapQuantity: Number(this.scrapQuantity),
      outputWarehouseId: this.outputWarehouseId,
      outputLocationId: this.outputLocationId,
      note: this.completionNote.trim() || undefined,
    };
    this.submit(API.workOrders.complete(order.id), payload, 'Production completed.');
  }

  confirmAction(action: 'reserve' | 'release' | 'pause' | 'resume' | 'cancel'): void {
    const order = this.workOrder();
    if (!order) return;
    const label = action === 'reserve' ? 'Reserve materials' : action === 'release' ? 'Release materials' : `${action[0].toUpperCase()}${action.slice(1)} work order`;
    this.dialog.open(ConfirmDialog, {
      data: { title: label, message: `Confirm ${label.toLowerCase()} for ${order.code}?`, confirmLabel: 'Confirm' },
      panelClass: 'ff-dialog-panel',
    }).afterClosed().subscribe((confirmed) => {
      if (!confirmed) return;
      if (action === 'reserve') {
        this.reserveMaterials(order.id);
        return;
      }
      const endpoints = {
        release: API.workOrders.releaseMaterials(order.id),
        pause: API.workOrders.pause(order.id),
        resume: API.workOrders.resume(order.id),
        cancel: API.workOrders.cancel(order.id),
      };
      this.submit(endpoints[action], {}, `${label} submitted.`);
    });
  }

  private reserveMaterials(workOrderId: string): void {
    this.submitting.set(true);
    this.reservationShortages.set([]);
    this.api.post<ReserveWorkOrderMaterialsResponse>(API.workOrders.reserveMaterials(workOrderId)).subscribe({
      next: (response) => {
        this.submitting.set(false);
        if (response.success) {
          this.reservationAllocations.set(response.data.allocations ?? []);
          this.snackBar.open('Materials reserved successfully.', 'OK', { duration: 3000 });
          this.load();
        }
      },
      error: (response) => {
        this.submitting.set(false);
        const apiError = response?.error as { errorCode?: string; message?: string; details?: WorkOrderMaterialShortageDto[] } | undefined;
        if (apiError?.errorCode === 'INSUFFICIENT_STOCK' && Array.isArray(apiError.details)) {
          this.reservationShortages.set(apiError.details);
          this.reservationAllocations.set([]);
          this.load();
        }
        this.snackBar.open(apiError?.message ?? 'Material reservation failed.', 'OK', { duration: 5000 });
      },
    });
  }

  loadLocations(): void {
    this.outputLocationId = '';
    if (!this.outputWarehouseId) {
      this.locations.set([]);
      return;
    }
    this.api.get<PageResponse<LocationDto>>(API.locations.base(this.outputWarehouseId), { page: 0, size: 100 }).subscribe({
      next: (response) => { if (response.success) this.locations.set(response.data.items); },
      error: () => this.locations.set([]),
    });
  }

  canSubmitCompletion(): boolean {
    return this.actualQuantity >= 0
      && this.goodQuantity >= 0
      && this.defectQuantity >= 0
      && this.scrapQuantity >= 0
      && this.actualQuantity === this.goodQuantity + this.defectQuantity + this.scrapQuantity
      && !!this.outputWarehouseId
      && !!this.outputLocationId;
  }

  private load(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Work order ID is missing.');
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.api.getById<WorkOrderDto>(API.workOrders.byId(id)).subscribe({
      next: (response) => {
        if (response.success) this.workOrder.set(response.data);
        this.loading.set(false);
      },
      error: (response) => {
        this.error.set(this.errorMessage(response, 'Unable to load work order.'));
        this.loading.set(false);
      },
    });
  }

  private loadReferences(): void {
    this.api.get<PageResponse<ProductDto>>(API.products.base, { page: 0, size: 100 }).subscribe({ next: (response) => { if (response.success) this.products.set(response.data.items); } });
    this.api.get<PageResponse<MachineDto>>(API.machines.base, { page: 0, size: 100 }).subscribe({ next: (response) => { if (response.success) this.machines.set(response.data.items); } });
    this.api.get<PageResponse<ProductionLineOption>>(API.productionLines.base, { page: 0, size: 100 }).subscribe({ next: (response) => { if (response.success) this.lines.set(response.data.items); } });
    this.api.get<PageResponse<WarehouseDto>>(API.warehouses.base, { page: 0, size: 100 }).subscribe({ next: (response) => { if (response.success) this.warehouses.set(response.data.items); } });
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.statuses).subscribe({ next: (response) => { if (response.success) this.statuses.set(response.data); } });
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.priorities).subscribe({ next: (response) => { if (response.success) this.priorities.set(response.data); } });
    this.api.get<WorkOrderLookupDto[]>(API.workOrders.eventTypes).subscribe({ next: (response) => { if (response.success) this.eventTypes.set(response.data); } });
  }

  private populateEditForm(): void {
    const order = this.workOrder();
    if (!order) return;
    this.editCode = order.code;
    this.editQuantity = order.plannedQuantity;
    this.editStart = this.toDateTimeLocal(order.plannedStartDate);
    this.editEnd = this.toDateTimeLocal(order.plannedEndDate);
  }

  private submit(url: string, body: unknown, successMessage: string, method: 'post' | 'put' = 'post'): void {
    this.submitting.set(true);
    const request = method === 'put' ? this.api.put<unknown>(url, body) : this.api.post<unknown>(url, body);
    request.subscribe({
      next: (response) => {
        this.submitting.set(false);
        if (response.success) {
          this.snackBar.open(successMessage, 'OK', { duration: 3000 });
          this.closePanel();
          this.load();
        }
      },
      error: (response) => {
        this.submitting.set(false);
        this.snackBar.open(this.errorMessage(response, 'The action was rejected by the backend.'), 'OK', { duration: 5000 });
      },
    });
  }

  private hasRole(...roles: string[]): boolean {
    const user = this.auth.getCurrentUser();
    return roles.includes(user?.role ?? '');
  }

  private toDateTimeLocal(value?: string | null): string {
    return value ? value.slice(0, 16) : '';
  }

  private toIso(value: string): string | undefined {
    return value ? new Date(value).toISOString() : undefined;
  }

  private errorMessage(response: unknown, fallback: string): string {
    const error = response as { error?: { message?: string } };
    return error.error?.message ?? fallback;
  }
}
