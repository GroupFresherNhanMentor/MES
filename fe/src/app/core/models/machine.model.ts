import type { MachineStatus } from '../../configs/constants';

export interface MachineDto {
  id: string;
  machineCode: string;
  machineName: string;
  model?: string;
  serialNumber?: string;
  productionLineId?: string;
  productionLineName?: string;
  status: MachineStatus;
  location?: string;
  installationDate?: string;
  lastMaintenanceDate?: string;
  nextMaintenanceDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMachineRequest {
  machineCode: string;
  machineName: string;
  model?: string;
  serialNumber?: string;
  productionLineId?: string;
  location?: string;
  installationDate?: string;
}

export interface UpdateMachineRequest {
  machineName?: string;
  model?: string;
  serialNumber?: string;
  productionLineId?: string;
  location?: string;
}

export interface ChangeMachineStatusRequest {
  status: MachineStatus;
  reason?: string;
}

export interface MachineListParams {
  keyword?: string;
  status?: MachineStatus;
  productionLineId?: string;
  page?: number;
  size?: number;
}
