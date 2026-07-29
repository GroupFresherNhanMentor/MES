import type { BomStatus } from '../../configs/constants';

export interface BomDto {
  id: string;
  bomCode: string;
  productId: string;
  productName?: string;
  productCode?: string;
  version: number;
  description?: string;
  status: BomStatus;
  quantity: number;
  unitOfMeasure?: string;
  totalCost?: number;
  items?: BomItemDto[];
  createdAt: string;
  updatedAt: string;
}

export interface BomItemDto {
  id: string;
  bomId: string;
  productId: string;
  productName?: string;
  productCode?: string;
  quantity: number;
  unitOfMeasure?: string;
  scrapRate?: number;
  cost?: number;
  sequence?: number;
  notes?: string;
}

export interface CreateBomRequest {
  productId: string;
  description?: string;
  quantity: number;
  items: CreateBomItemRequest[];
}

export interface CreateBomItemRequest {
  productId: string;
  quantity: number;
  unitOfMeasure?: string;
  scrapRate?: number;
  sequence?: number;
  notes?: string;
}

export interface UpdateBomRequest {
  description?: string;
  quantity?: number;
  items?: CreateBomItemRequest[];
}

export interface BomListParams {
  keyword?: string;
  productId?: string;
  status?: BomStatus;
  page?: number;
  size?: number;
}
