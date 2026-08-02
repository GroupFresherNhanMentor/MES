export interface WorkOrderDto {
  id: string;
  code: string;
  finishedProductId: string;
  bomId: string;
  plannedQuantity: number;
  plannedStartDate?: string | null;
  plannedEndDate?: string | null;
  priorityId?: string | null;
  workOrderStatusId: string;
  createdBy?: string | null;
  createdAt: string;
  materials?: WorkOrderMaterialDto[] | null;
  events?: WorkOrderEventDto[] | null;
}

export interface WorkOrderLookupDto {
  id: string;
  name: string;
  description?: string | null;
}

export interface WorkOrderMaterialDto {
  id: string;
  workOrderId: string;
  materialProductId: string;
  requiredQuantity: number;
  reservedQuantity: number;
  consumedQuantity: number;
}

export interface WorkOrderEventDto {
  id: string;
  workOrderId: string;
  productionRunId?: string | null;
  eventTypeId: string;
  operatorId?: string | null;
  eventTimestamp: string;
  note?: string | null;
}

export interface CreateWorkOrderRequest {
  code: string;
  finishedProductId: string;
  plannedQuantity: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  priorityId?: string;
  workOrderStatusId: string;
}

export interface UpdateWorkOrderRequest {
  code?: string;
  plannedQuantity?: number;
  plannedStartDate?: string;
  plannedEndDate?: string;
  priorityId?: string;
  workOrderStatusId?: string;
}

export interface ReservedMaterialAllocationDto {
  materialProductId: string;
  lotId: string;
  warehouseId: string;
  locationId: string;
  reservedQuantity: number;
}

export interface ReserveWorkOrderMaterialsResponse {
  workOrderId: string;
  status: string;
  allocations: ReservedMaterialAllocationDto[];
}

export interface WorkOrderMaterialShortageDto {
  materialProductId: string;
  requiredQuantity: number;
  availableQuantity: number;
  shortageQuantity: number;
}

export interface StartWorkOrderRequest {
  machineId: string;
  productionLineId?: string;
}

export interface CompleteWorkOrderRequest {
  actualQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  scrapQuantity: number;
  outputWarehouseId: string;
  outputLocationId: string;
  note?: string;
}

export interface WorkOrderListParams {
  code?: string;
  finishedProductId?: string;
  statusId?: string;
  page?: number;
  size?: number;
}
