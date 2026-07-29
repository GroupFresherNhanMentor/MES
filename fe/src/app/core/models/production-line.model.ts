import type { ProductionLineStatus } from '../../configs/constants';

export interface ProductionLineDto {
  id: string;
  lineCode: string;
  lineName: string;
  description?: string;
  status: ProductionLineStatus;
  machineCount?: number;
  supervisor?: string;
  location?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductionLineRequest {
  lineCode: string;
  lineName: string;
  description?: string;
  supervisor?: string;
  location?: string;
}

export interface UpdateProductionLineRequest {
  lineName?: string;
  description?: string;
  supervisor?: string;
  location?: string;
}

export interface ProductionLineListParams {
  keyword?: string;
  status?: ProductionLineStatus;
  page?: number;
  size?: number;
}
