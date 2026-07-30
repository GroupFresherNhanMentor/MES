export interface LocationDto {
  id: string;
  warehouseId: string;
  code: string;
  name: string;
  locationStatusId: string;
  locationStatusName: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLocationRequest {
  code: string;
  name: string;
}

export interface UpdateLocationRequest {
  name?: string;
}

export interface LocationListParams {
  keyword?: string;
  statusName?: string;
  page?: number;
  size?: number;
}
