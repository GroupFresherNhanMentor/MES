export interface ProductionLineDto {
  id: string;
  code: string;
  name: string;
  lineStatusId: string;
  lineStatusName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductionLineRequest {
  code: string;
  name: string;
}

export interface UpdateProductionLineRequest {
  name?: string;
}

export interface ProductionLineListParams {
  keyword?: string;
  statusName?: string;
  page?: number;
  size?: number;
}
