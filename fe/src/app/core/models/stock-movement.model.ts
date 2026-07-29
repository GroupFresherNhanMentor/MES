import type { MovementType } from '../../configs/constants';

export interface StockMovementDto {
  id: string;
  movementCode: string;
  productId: string;
  productName?: string;
  productCode?: string;
  movementType: MovementType;
  quantity: number;
  fromWarehouseId?: string;
  fromWarehouseName?: string;
  fromLocationId?: string;
  fromLocationCode?: string;
  toWarehouseId?: string;
  toWarehouseName?: string;
  toLocationId?: string;
  toLocationCode?: string;
  referenceType?: string;
  referenceId?: string;
  reason?: string;
  performedBy?: string;
  performedByName?: string;
  movementDate: string;
  notes?: string;
  createdAt: string;
}

export interface CreateStockMovementRequest {
  productId: string;
  movementType: MovementType;
  quantity: number;
  fromWarehouseId?: string;
  fromLocationId?: string;
  toWarehouseId?: string;
  toLocationId?: string;
  referenceType?: string;
  referenceId?: string;
  reason?: string;
  notes?: string;
}

export interface StockMovementListParams {
  productId?: string;
  movementType?: MovementType;
  warehouseId?: string;
  fromDate?: string;
  toDate?: string;
  keyword?: string;
  page?: number;
  size?: number;
}
