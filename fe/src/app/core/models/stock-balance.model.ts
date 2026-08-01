export interface StockBalanceDto {
  id: string;
  warehouse: { id: string; code: string; name: string } | null;
  location: { id: string; code: string; name: string } | null;
  product: { id: string; code: string; name: string } | null;
  lot: { id: string; lotNumber: string } | null;
  stockStatus: { id: string; name: string } | null;
  quantity: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface StockBalanceSearchParams {
  warehouseId?: string;
  locationId?: string;
  productId?: string;
  lotId?: string;
  stockStatusId?: string;
  page?: number;
  size?: number;
}
