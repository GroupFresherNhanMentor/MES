export interface MachineDto {
  id: string;
  productionLineId: string;
  productionLineName?: string;
  productionLine?: { id: string; code: string; name: string } | null;
  code: string;
  name: string;
  machineStatusId: string;
  machineStatusName: string;
  machineStatus?: { id: string; name: string; description?: string } | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMachineRequest {
  code: string;
  name: string;
  productionLineId: string;
}

export interface UpdateMachineRequest {
  name?: string;
  productionLineId?: string;
}

export interface ChangeMachineStatusRequest {
  statusId: string;
}

export interface MachineListParams {
  keyword?: string;
  statusName?: string;
  page?: number;
  size?: number;
}
