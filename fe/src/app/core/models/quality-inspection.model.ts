import type { InspectionStatus } from '../../configs/constants';

export interface QualityInspectionDto {
  id: string;
  inspectionCode: string;
  productId: string;
  productName?: string;
  productCode?: string;
  workOrderId?: string;
  workOrderCode?: string;
  inspector?: string;
  inspectorName?: string;
  inspectionDate: string;
  status: InspectionStatus;
  sampleSize: number;
  defectsFound: number;
  defectRate?: number;
  notes?: string;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateQualityInspectionRequest {
  productId: string;
  workOrderId?: string;
  inspector?: string;
  inspectionDate: string;
  sampleSize: number;
  defectsFound?: number;
  notes?: string;
}

export interface UpdateInspectionDecisionRequest {
  remarks?: string;
}

export interface QualityInspectionListParams {
  keyword?: string;
  status?: InspectionStatus;
  productId?: string;
  workOrderId?: string;
  page?: number;
  size?: number;
}
