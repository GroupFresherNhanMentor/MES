import type { WarehouseStatus } from '../../configs/constants';

export interface WarehouseDto {
  id: string;
  warehouseCode: string;
  warehouseName: string;
  address?: string;
  city?: string;
  capacity?: number;
  usedCapacity?: number;
  status: WarehouseStatus;
  locationCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWarehouseRequest {
  warehouseCode: string;
  warehouseName: string;
  address?: string;
  city?: string;
  capacity?: number;
}

export interface UpdateWarehouseRequest {
  warehouseName?: string;
  address?: string;
  city?: string;
  capacity?: number;
}

export interface WarehouseListParams {
  keyword?: string;
  status?: WarehouseStatus;
  page?: number;
  size?: number;
}
