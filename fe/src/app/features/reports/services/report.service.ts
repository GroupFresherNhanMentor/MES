import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api';
import { ApiResponse, PageResponse } from '../../../core/models/api.model';

export interface InventorySummaryRow {
  productId: string;
  productCode: string;
  productName: string;
  productType: string;
  warehouseId: string;
  warehouseName: string;
  availableQuantity: number;
  reservedQuantity: number;
  qualityInspectionQuantity: number;
  onHoldQuantity: number;
  scrappedQuantity: number;
  totalOnHand: number;
}

export interface MaterialShortageRow {
  workOrderId: string;
  workOrderCode: string;
  materialProductId: string;
  materialCode: string;
  materialName: string;
  requiredQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  shortageQuantity: number;
}

export interface ProductionOutputRow {
  date: string;
  workOrderId: string;
  workOrderCode: string;
  productId: string;
  productCode: string;
  productName: string;
  plannedQuantity: number;
  actualQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  scrapQuantity: number;
  completionRate: number;
}

export interface DefectRateRow {
  productId: string;
  productCode: string;
  productName: string;
  totalInspected: number;
  defectQuantity: number;
  scrapQuantity: number;
  defectRate: number;
  topDefectTypes: string[];
}

export interface MachineDowntimeRow {
  machineId: string;
  machineCode: string;
  machineName: string;
  totalDowntimeMinutes: number;
  maintenanceTicketCount: number;
  lastDowntimeReason?: string;
}

export interface StockMovementHistoryRow {
  movementId: string;
  movementTime: string;
  movementType: string;
  productId: string;
  productCode: string;
  productName: string;
  lotNumber?: string;
  fromWarehouse?: string;
  fromLocation?: string;
  toWarehouse?: string;
  toLocation?: string;
  quantity: number;
  referenceType?: string;
  referenceId?: string;
  createdBy?: string;
  reason?: string;
}

@Injectable({ providedIn: 'root' })
export class ReportService {
  private api = inject(ApiService);

  getInventorySummary(params?: Record<string, any>): Observable<ApiResponse<PageResponse<InventorySummaryRow>>> {
    return this.api.get<PageResponse<InventorySummaryRow>>('/api/reports/inventory-summary', params);
  }

  getMaterialShortages(): Observable<ApiResponse<MaterialShortageRow[]>> {
    return this.api.get<MaterialShortageRow[]>('/api/reports/material-shortage');
  }

  getProductionOutput(params?: Record<string, any>): Observable<ApiResponse<PageResponse<ProductionOutputRow>>> {
    return this.api.get<PageResponse<ProductionOutputRow>>('/api/reports/production-output', params);
  }

  getDefectRates(params?: Record<string, any>): Observable<ApiResponse<DefectRateRow[]>> {
    return this.api.get<DefectRateRow[]>('/api/reports/defect-rate', params);
  }

  getMachineDowntimes(params?: Record<string, any>): Observable<ApiResponse<MachineDowntimeRow[]>> {
    return this.api.get<MachineDowntimeRow[]>('/api/reports/machine-downtime', params);
  }

  getStockMovementHistory(params?: Record<string, any>): Observable<ApiResponse<PageResponse<StockMovementHistoryRow>>> {
    return this.api.get<PageResponse<StockMovementHistoryRow>>('/api/reports/stock-movement-history', params);
  }
}
