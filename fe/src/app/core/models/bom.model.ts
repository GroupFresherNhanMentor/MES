export interface BomDto {
  id: string;
  finishedProductId: string;
  finishedProductName?: string;
  finishedProductCode?: string;
  version: number;
  bomStatusId: string;
  bomStatusName?: string;
  createdBy?: string;
  createdAt: string;
  items?: BomItemDto[];
}

export interface BomItemDto {
  id: string;
  bomId: string;
  materialProductId: string;
  materialProductName?: string;
  materialProductCode?: string;
  quantityPerUnit: number;
  unit?: string;
  scrapRate?: number;
}

export interface CreateBomRequest {
  finishedProductId: string;
  version?: number;
  bomStatusId?: string;
}

export interface CreateBomItemRequest {
  materialProductId: string;
  quantityPerUnit: number;
  unit?: string;
  scrapRate?: number;
}

export interface BomListParams {
  page?: number;
  size?: number;
  finishedProductId?: string;
  bomStatusId?: string;
}
