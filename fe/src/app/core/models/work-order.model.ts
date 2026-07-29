import type { WorkOrderStatus } from '../../configs/constants';

export interface WorkOrderDto {
  id: string;
  orderCode: string;
  productId: string;
  productName?: string;
  productCode?: string;
  bomId?: string;
  bomCode?: string;
  productionLineId?: string;
  productionLineName?: string;
  quantity: number;
  completedQuantity: number;
  scrapQuantity: number;
  status: WorkOrderStatus;
  priority: number;
  dueDate?: string;
  startedAt?: string;
  completedAt?: string;
  assignedTo?: string;
  assignedToName?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateWorkOrderRequest {
  productId: string;
  bomId?: string;
  productionLineId?: string;
  quantity: number;
  priority?: number;
  dueDate?: string;
  assignedTo?: string;
  notes?: string;
}

export interface UpdateWorkOrderRequest {
  quantity?: number;
  priority?: number;
  dueDate?: string;
  assignedTo?: string;
  productionLineId?: string;
  notes?: string;
}

export interface ReserveMaterialsRequest {
  notes?: string;
}

export interface WorkOrderListParams {
  keyword?: string;
  status?: WorkOrderStatus;
  productId?: string;
  productionLineId?: string;
  assignedTo?: string;
  page?: number;
  size?: number;
}
