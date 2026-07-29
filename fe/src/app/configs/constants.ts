export const APP_CONSTANTS = {
  appName: 'MES',
  appFullName: 'MES - Manufacturing Operations Platform',
  tokenKey: 'ff_access_token',
  refreshTokenKey: 'ff_refresh_token',
  userKey: 'ff_user',
  defaultPageSize: 20,
  pageSizeOptions: [10, 20, 50, 100],
} as const;

export const PRODUCT_TYPES = ['RAW_MATERIAL', 'SUB_ASSEMBLY', 'FINISHED_GOOD', 'CONSUMABLE', 'PACKAGING'] as const;
export type ProductType = (typeof PRODUCT_TYPES)[number];

export const PRODUCT_STATUSES = ['ACTIVE', 'INACTIVE', 'DISCONTINUED'] as const;
export type ProductStatus = (typeof PRODUCT_STATUSES)[number];

export const WAREHOUSE_STATUSES = ['ACTIVE', 'INACTIVE'] as const;
export type WarehouseStatus = (typeof WAREHOUSE_STATUSES)[number];

export const LOCATION_STATUSES = ['AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'INACTIVE'] as const;
export type LocationStatus = (typeof LOCATION_STATUSES)[number];

export const MACHINE_STATUSES = ['RUNNING', 'IDLE', 'MAINTENANCE', 'BREAKDOWN', 'INACTIVE'] as const;
export type MachineStatus = (typeof MACHINE_STATUSES)[number];

export const PRODUCTION_LINE_STATUSES = ['ACTIVE', 'INACTIVE'] as const;
export type ProductionLineStatus = (typeof PRODUCTION_LINE_STATUSES)[number];

export const BOM_STATUSES = ['DRAFT', 'ACTIVE', 'ARCHIVED'] as const;
export type BomStatus = (typeof BOM_STATUSES)[number];

export const WORK_ORDER_STATUSES = [
  'DRAFT',
  'RELEASED',
  'IN_PROGRESS',
  'PAUSED',
  'COMPLETED',
  'CANCELLED',
] as const;
export type WorkOrderStatus = (typeof WORK_ORDER_STATUSES)[number];

export const MOVEMENT_TYPES = ['RECEIPT', 'ISSUE', 'TRANSFER_IN', 'TRANSFER_OUT', 'ADJUSTMENT'] as const;
export type MovementType = (typeof MOVEMENT_TYPES)[number];

export const INSPECTION_STATUSES = ['PENDING', 'PASSED', 'FAILED', 'ON_HOLD', 'RELEASED', 'SCRAPPED'] as const;
export type InspectionStatus = (typeof INSPECTION_STATUSES)[number];

export const TICKET_STATUSES = ['OPEN', 'IN_PROGRESS', 'CLOSED'] as const;
export type TicketStatus = (typeof TICKET_STATUSES)[number];

export const TICKET_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] as const;
export type TicketPriority = (typeof TICKET_PRIORITIES)[number];

export const SYSTEM_ROLES = ['ADMIN', 'OPERATOR', 'SUPERVISOR', 'QUALITY', 'MAINTENANCE'] as const;
export type SystemRole = (typeof SYSTEM_ROLES)[number];

export const USER_STATUSES = ['ACTIVE', 'INACTIVE'] as const;
export type UserStatus = (typeof USER_STATUSES)[number];
