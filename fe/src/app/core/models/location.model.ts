import type { LocationStatus } from '../../configs/constants';

export interface LocationDto {
  id: string;
  warehouseId: string;
  locationCode: string;
  description?: string;
  maxCapacity?: number;
  currentLoad?: number;
  status: LocationStatus;
  warehouseName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLocationRequest {
  locationCode: string;
  description?: string;
  maxCapacity?: number;
}

export interface UpdateLocationRequest {
  description?: string;
  maxCapacity?: number;
  status?: LocationStatus;
}

export interface LocationListParams {
  keyword?: string;
  status?: LocationStatus;
  page?: number;
  size?: number;
}
