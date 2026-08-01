export interface StockMovementDto {
  id: string;
  movementType: { id: string; name: string } | null;
  product: { id: string; code: string; name: string } | null;
  lot: { id: string; lotNumber: string } | null;
  fromWarehouse: { id: string; code: string; name: string } | null;
  toWarehouse: { id: string; code: string; name: string } | null;
  fromLocation: { id: string; code: string; name: string } | null;
  toLocation: { id: string; code: string; name: string } | null;
  quantity: number;
  fromStatus: { id: string; name: string } | null;
  toStatus: { id: string; name: string } | null;
  referenceNo: string | null;
  reason: string | null;
  createdBy: { id: string; username: string; fullName: string } | null;
  createdAt: string;
}

export interface StockTransferRequest {
  fromWarehouseId: string;
  fromLocationId: string;
  toWarehouseId: string;
  toLocationId: string;
  productId: string;
  lotId: string;
  quantity: number;
}

export interface StockTransferResponse {
  transferOutMovement: StockMovementDto | null;
  transferInMovement: StockMovementDto | null;
  sourceBalance: {
    id: string;
    warehouse: { id: string; code: string; name: string } | null;
    location: { id: string; code: string; name: string } | null;
    product: { id: string; code: string; name: string } | null;
    quantity: number;
  } | null;
  destinationBalance: {
    id: string;
    warehouse: { id: string; code: string; name: string } | null;
    location: { id: string; code: string; name: string } | null;
    product: { id: string; code: string; name: string } | null;
    quantity: number;
  } | null;
}

export interface StockMovementSearchParams {
  movementTypeId?: string;
  productId?: string;
  lotId?: string;
  warehouseId?: string;
  locationId?: string;
  referenceNo?: string;
  page?: number;
  size?: number;
}
