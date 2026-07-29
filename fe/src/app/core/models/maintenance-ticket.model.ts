import type { TicketStatus, TicketPriority } from '../../configs/constants';

export interface MaintenanceTicketDto {
  id: string;
  ticketCode: string;
  machineId: string;
  machineName?: string;
  machineCode?: string;
  title: string;
  description?: string;
  priority: TicketPriority;
  status: TicketStatus;
  reportedBy?: string;
  reportedByName?: string;
  assignedTo?: string;
  assignedToName?: string;
  reportedAt: string;
  startedAt?: string;
  closedAt?: string;
  resolution?: string;
  downtimeMinutes?: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateMaintenanceTicketRequest {
  machineId: string;
  title: string;
  description?: string;
  priority?: TicketPriority;
  assignedTo?: string;
}

export interface UpdateMaintenanceTicketRequest {
  title?: string;
  description?: string;
  priority?: TicketPriority;
  assignedTo?: string;
}

export interface CloseTicketRequest {
  resolution: string;
  downtimeMinutes?: number;
}

export interface MaintenanceTicketListParams {
  keyword?: string;
  status?: TicketStatus;
  priority?: TicketPriority;
  machineId?: string;
  assignedTo?: string;
  page?: number;
  size?: number;
}
