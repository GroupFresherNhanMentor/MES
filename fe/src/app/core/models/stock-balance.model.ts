export interface StockBalanceDto {
  id: string;
  productId: string;
  productName?: string;
  productCode?: string;
  warehouseId: string;
  warehouseName?: string;
  locationId?: string;
  locationCode?: string;
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  unitOfMeasure?: string;
  lastMovementAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface StockBalanceListParams {
  productId?: string;
  warehouseId?: string;
  locationId?: string;
  keyword?: string;
  page?: number;
  size?: number;
}
