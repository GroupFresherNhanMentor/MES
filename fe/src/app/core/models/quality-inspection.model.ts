// FE models for the Quality module — aligned with the BE response/request DTOs.

export interface WorkOrderInfo {
  workOrderId: string;
  workOrderCode: string;
}

export interface ProductInfo {
  productId: string;
  productCode: string;
  productName: string;
}

export interface StockLotInfo {
  lotId: string;
  lotNumber: string;
  lotType: string;
}

export interface QualityInspectionDto {
  id: string;
  workOrder?: WorkOrderInfo;
  product?: ProductInfo;
  lot?: StockLotInfo;
  quantity: number;
  remainingQuantity: number;
  qcStatusName: string;
  createdAt: string;
}

export interface InspectorInfo {
  userId: string;
  username: string;
  fullName: string;
}

export interface QualityInspectionResultDto {
  id: string;
  inspectionId: string;
  isPass: boolean;
  quantity: number;
  defectTypeName: string;
  reason: string;
  actionName: string;
  inspector?: InspectorInfo;
  inspectedAt: string;
  note?: string;
}

export interface CreateQualityInspectionRequest {
  workOrderId: string;
  productId: string;
  lotId: string;
  quantity: number;
  qcStatusId: string;
}

export interface PassQcRequest {
  passedQuantity: number;
  note?: string;
}

export interface FailQcRequest {
  failedQuantity: number;
  actionId: string;
  defectTypeId: string;
  reason: string;
  note?: string;
}

export interface QualityLookupDto {
  id: string;
  name: string;
  description?: string;
}

export interface CreateQualityLookupRequest {
  name: string;
  description?: string;
}
