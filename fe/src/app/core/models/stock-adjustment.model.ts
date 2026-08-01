export interface StockAdjustmentApprovalDto {
  id: string;
  product: { id: string; code: string; name: string } | null;
  warehouse: { id: string; code: string; name: string } | null;
  location: { id: string; code: string; name: string } | null;
  stockBalanceId: string;
  quantityAdjustment: number;
  reason: string | null;
  referenceNo: string | null;
  creator: { id: string; username: string; fullName: string } | null;
  createdAt: string;
}

export interface StockAdjustmentRequest {
  stockBalanceId: string;
  quantityAdjustment: number;
  reason: string;
  referenceNo?: string;
}
