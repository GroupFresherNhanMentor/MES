export interface WarehouseDto {
  id: string;
  code: string;
  name: string;
  address?: string;
  warehouseStatusId: string;
  warehouseStatusName: string;
  warehouseStatus?: { id: string; name: string; description?: string } | null;
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
