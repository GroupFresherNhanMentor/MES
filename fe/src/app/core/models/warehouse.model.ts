export interface WarehouseDto {
  id: string;
  code: string;
  name: string;
  address?: string;
  warehouseStatusId: string;
  warehouseStatusName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWarehouseRequest {
  code: string;
  name: string;
  address?: string;
}

export interface UpdateWarehouseRequest {
  name?: string;
  address?: string;
}

export interface WarehouseListParams {
  keyword?: string;
  statusName?: string;
  page?: number;
  size?: number;
}
