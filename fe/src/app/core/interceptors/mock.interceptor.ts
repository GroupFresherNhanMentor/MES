import { HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { delay } from 'rxjs/operators';

// ── Helpers ──
function ts(): string {
  return new Date().toISOString();
}
function ok<T>(data: T) {
  return { success: true, data, message: 'OK', timestamp: ts() };
}
function okPage<T>(items: T[], totalElements = items.length) {
  return {
    success: true,
    data: { items, totalElements, pageNumber: 0, pageSize: 20, totalPages: Math.ceil(totalElements / 20) },
    message: 'OK',
    timestamp: ts(),
  };
}
function matchUrl(url: string, pattern: string): RegExpMatchArray | null {
  const escaped = pattern.replace(/\/:id\b/g, '/([^/]+)').replace(/\/:wId\b/g, '/([^/]+)');
  return url.match(new RegExp(`^${escaped}(\\?.*)?$`));
}
function extractParam(url: string, pattern: string): string | null {
  const m = matchUrl(url, pattern);
  return m ? m[1] : null;
}

// ── IDs ──
const PT_RAW = 'PT-RAW', PT_SUB = 'PT-SUB', PT_FIN = 'PT-FIN', PT_CON = 'PT-CON', PT_PKG = 'PT-PKG';
const PS_ACT = 'PS-ACT', PS_INA = 'PS-INA', PS_DIS = 'PS-DIS';

// ── Mock Products ──
const MOCK_PRODUCTS = [
  { id: 'P001', code: 'STEEL-PLATE', name: 'Steel Plate', productTypeId: PT_RAW, productTypeName: 'Raw Material', unitId: 'KG', unitName: 'Kilogram', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P002', code: 'ALUM-SHEET', name: 'Aluminum Sheet', productTypeId: PT_RAW, productTypeName: 'Raw Material', unitId: 'KG', unitName: 'Kilogram', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P003', code: 'COPPER-WIRE', name: 'Copper Wire', productTypeId: PT_RAW, productTypeName: 'Raw Material', unitId: 'M', unitName: 'Meter', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P004', code: 'CIRCUIT-BOARD', name: 'Circuit Board', productTypeId: PT_SUB, productTypeName: 'Semi-Finished', unitId: 'PCS', unitName: 'Pieces', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P005', code: 'ENGINE-BLOCK', name: 'Engine Block', productTypeId: PT_SUB, productTypeName: 'Semi-Finished', unitId: 'PCS', unitName: 'Pieces', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P006', code: 'WIDGET-A', name: 'Finished Widget A', productTypeId: PT_FIN, productTypeName: 'Finished Good', unitId: 'PCS', unitName: 'Pieces', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P007', code: 'WIDGET-B', name: 'Finished Widget B', productTypeId: PT_FIN, productTypeName: 'Finished Good', unitId: 'PCS', unitName: 'Pieces', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
  { id: 'P008', code: 'LUBE-OIL', name: 'Lubricant Oil', productTypeId: PT_CON, productTypeName: 'Consumable', unitId: 'L', unitName: 'Liter', productStatusId: PS_ACT, productStatusName: 'Active', version: 1, createdAt: '2025-01-15T08:00:00Z', createdBy: 'admin', updatedAt: '2025-01-15T08:00:00Z', updatedBy: null },
];

const MOCK_PRODUCT_TYPES = [
  { id: PT_RAW, name: 'RAW_MATERIAL', description: 'Raw Material' },
  { id: PT_SUB, name: 'SUB_ASSEMBLY', description: 'Sub Assembly' },
  { id: PT_FIN, name: 'FINISHED_GOOD', description: 'Finished Good' },
  { id: PT_CON, name: 'CONSUMABLE', description: 'Consumable' },
  { id: PT_PKG, name: 'PACKAGING', description: 'Packaging' },
];

const MOCK_PRODUCT_STATUSES = [
  { id: PS_ACT, name: 'ACTIVE', description: 'Active' },
  { id: PS_INA, name: 'INACTIVE', description: 'Inactive' },
  { id: PS_DIS, name: 'DISCONTINUED', description: 'Discontinued' },
];

// ── Mock Warehouses ──
const MOCK_WAREHOUSES = [
  { id: 'WH-HN', code: 'WH-HN', name: 'Hanoi Main', address: '100 Nguyen Trai', city: 'Hanoi', capacity: 10000, usedCapacity: 6200, warehouseStatusName: 'ACTIVE', locationCount: 4, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'WH-HCM', code: 'WH-HCM', name: 'HCMC Branch', address: '200 Le Loi', city: 'Ho Chi Minh', capacity: 15000, usedCapacity: 8900, warehouseStatusName: 'ACTIVE', locationCount: 6, createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'WH-DN', code: 'WH-DN', name: 'Da Nang', address: '50 Bach Dang', city: 'Da Nang', capacity: 8000, usedCapacity: 3100, warehouseStatusName: 'ACTIVE', locationCount: 3, createdAt: '2025-02-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
];

// ── Mock Locations ──
const MOCK_LOCATIONS: Record<string, any[]> = {
  'WH-HN': [
    { id: 'LOC-HN-A1', warehouseId: 'WH-HN', code: 'A-01', description: 'Row A, Shelf 1', maxCapacity: 500, currentLoad: 320, locationStatusName: 'ACTIVE', warehouseName: 'Hanoi Main', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
    { id: 'LOC-HN-A2', warehouseId: 'WH-HN', code: 'A-02', description: 'Row A, Shelf 2', maxCapacity: 500, currentLoad: 500, locationStatusName: 'ACTIVE', warehouseName: 'Hanoi Main', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
    { id: 'LOC-HN-B1', warehouseId: 'WH-HN', code: 'B-01', description: 'Row B, Shelf 1', maxCapacity: 300, currentLoad: 150, locationStatusName: 'ACTIVE', warehouseName: 'Hanoi Main', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
    { id: 'LOC-HN-B2', warehouseId: 'WH-HN', code: 'B-02', description: 'Row B, Shelf 2', maxCapacity: 300, currentLoad: 0, status: 'MAINTENANCE', warehouseName: 'Hanoi Main', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  ],
  'WH-HCM': [
    { id: 'LOC-HCM-A1', warehouseId: 'WH-HCM', code: 'A-01', description: 'Section A', maxCapacity: 1000, currentLoad: 780, locationStatusName: 'ACTIVE', warehouseName: 'HCMC Branch', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
    { id: 'LOC-HCM-A2', warehouseId: 'WH-HCM', code: 'A-02', description: 'Section A-2', maxCapacity: 1000, currentLoad: 450, locationStatusName: 'ACTIVE', warehouseName: 'HCMC Branch', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  ],
  'WH-DN': [
    { id: 'LOC-DN-A1', warehouseId: 'WH-DN', code: 'DN-01', description: 'Main Storage', maxCapacity: 800, currentLoad: 310, locationStatusName: 'ACTIVE', warehouseName: 'Da Nang', createdAt: '2025-02-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  ],
};

// ── Mock Production Lines ──
const MOCK_PRODUCTION_LINES = [
  { id: 'L-ASM1', code: 'ASM-01', name: 'Assembly Line 1', description: 'Main widget assembly line', lineStatusName: 'ACTIVE', machineCount: 5, supervisor: 'Nguyen Van A', location: 'Building A, Floor 1', createdAt: '2025-01-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'L-ASM2', code: 'ASM-02', name: 'Assembly Line 2', description: 'Secondary widget assembly line', lineStatusName: 'ACTIVE', machineCount: 3, supervisor: 'Tran Thi B', location: 'Building A, Floor 2', createdAt: '2025-01-15T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'L-PKG', code: 'PKG-01', name: 'Packaging Line', description: 'Finished product packaging line', lineStatusName: 'ACTIVE', machineCount: 2, supervisor: 'Le Van C', location: 'Building B, Floor 1', createdAt: '2025-02-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
];

// ── Mock Machines ──
const MOCK_MACHINES = [
  { id: 'M-001', code: 'CNC-001', name: 'CNC Milling Machine 1', model: 'Haas VF-2', serialNumber: 'HS-2025-001', productionLineId: 'L-ASM1', productionLineName: 'Assembly Line 1', machineStatusName: 'RUNNING', location: 'Building A', installationDate: '2025-01-15', lastMaintenanceDate: '2025-05-15', nextMaintenanceDate: '2025-08-15', createdAt: '2025-01-15T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'M-002', code: 'CNC-002', name: 'CNC Milling Machine 2', model: 'Haas VF-3', serialNumber: 'HS-2025-002', productionLineId: 'L-ASM1', productionLineName: 'Assembly Line 1', machineStatusName: 'RUNNING', location: 'Building A', installationDate: '2025-01-15', lastMaintenanceDate: '2025-05-20', nextMaintenanceDate: '2025-08-20', createdAt: '2025-01-15T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'M-003', code: 'ROBO-001', name: 'Robotic Arm 1', model: 'Fanuc CRX-10', serialNumber: 'FN-2025-001', productionLineId: 'L-ASM1', productionLineName: 'Assembly Line 1', machineStatusName: 'RUNNING', location: 'Building A', installationDate: '2025-02-01', lastMaintenanceDate: '2025-04-01', nextMaintenanceDate: '2025-07-01', createdAt: '2025-02-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'M-004', code: 'CNC-003', name: 'CNC Lathe', model: 'Haas ST-20', serialNumber: 'HS-2025-003', productionLineId: 'L-ASM2', productionLineName: 'Assembly Line 2', machineStatusName: 'AVAILABLE', location: 'Building A', installationDate: '2025-03-01', lastMaintenanceDate: '2025-06-01', nextMaintenanceDate: '2025-09-01', createdAt: '2025-03-01T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
  { id: 'M-005', code: 'PKG-001', name: 'Packaging Machine', model: 'Bosch P-500', serialNumber: 'BS-2025-001', productionLineId: 'L-PKG', productionLineName: 'Packaging Line', machineStatusName: 'RUNNING', location: 'Building B', installationDate: '2025-02-15', lastMaintenanceDate: '2025-05-25', nextMaintenanceDate: '2025-08-25', createdAt: '2025-02-15T00:00:00Z', updatedAt: '2025-06-01T00:00:00Z' },
];

// ── Mock BOMs ──
const MOCK_BOM_STATUSES = [
  { id: 'BS-DRAFT', name: 'DRAFT', description: 'Draft version' },
  { id: 'BS-ACTIVE', name: 'ACTIVE', description: 'Active production BOM' },
  { id: 'BS-INACTIVE', name: 'INACTIVE', description: 'Inactive version' },
];

const MOCK_BOMS: any[] = [
  {
    id: 'BOM-001',
    finishedProductId: 'P006',
    finishedProductName: 'Finished Widget A',
    finishedProductCode: 'WIDGET-A',
    version: 1,
    bomStatusId: 'BS-ACTIVE',
    bomStatusName: 'ACTIVE',
    createdBy: 'admin',
    createdAt: '2025-03-01T00:00:00Z',
    items: [
      { id: 'BMI-001', bomId: 'BOM-001', materialProductId: 'P001', materialProductName: 'Steel Plate', materialProductCode: 'STEEL-PLATE', quantityPerUnit: 2, unit: 'KG', scrapRate: 5 },
      { id: 'BMI-002', bomId: 'BOM-001', materialProductId: 'P003', materialProductName: 'Copper Wire', materialProductCode: 'COPPER-WIRE', quantityPerUnit: 5, unit: 'M', scrapRate: 3 },
      { id: 'BMI-003', bomId: 'BOM-001', materialProductId: 'P004', materialProductName: 'Circuit Board', materialProductCode: 'CIRCUIT-BOARD', quantityPerUnit: 1, unit: 'PCS', scrapRate: 2 },
    ],
  },
  {
    id: 'BOM-002',
    finishedProductId: 'P007',
    finishedProductName: 'Finished Widget B',
    finishedProductCode: 'WIDGET-B',
    version: 1,
    bomStatusId: 'BS-DRAFT',
    bomStatusName: 'DRAFT',
    createdBy: 'admin',
    createdAt: '2025-03-15T00:00:00Z',
    items: [
      { id: 'BMI-004', bomId: 'BOM-002', materialProductId: 'P002', materialProductName: 'Aluminum Sheet', materialProductCode: 'ALUM-SHEET', quantityPerUnit: 3, unit: 'KG', scrapRate: 5 },
      { id: 'BMI-005', bomId: 'BOM-002', materialProductId: 'P005', materialProductName: 'Engine Block', materialProductCode: 'ENGINE-BLOCK', quantityPerUnit: 1, unit: 'PCS', scrapRate: 0 },
    ],
  },
];

// ── Mock Work Orders ──
const MOCK_WORK_ORDERS = [
  { id: 'WO-001', orderCode: 'WO-2025-001', productId: 'P006', productName: 'Finished Widget A', productCode: 'WIDGET-A', bomId: 'BOM-001', bomCode: 'BOM-WA-001', productionLineId: 'L-ASM1', productionLineName: 'Assembly Line 1', quantity: 100, completedQuantity: 45, scrapQuantity: 2, status: 'IN_PROGRESS', priority: 1, dueDate: '2025-07-15T00:00:00Z', startedAt: '2025-07-01T08:00:00Z', completedAt: null, assignedTo: 'U-001', assignedToName: 'Nguyen Van A', notes: null, createdAt: '2025-06-28T00:00:00Z', updatedAt: '2025-07-01T08:00:00Z' },
  { id: 'WO-002', orderCode: 'WO-2025-002', productId: 'P007', productName: 'Finished Widget B', productCode: 'WIDGET-B', bomId: 'BOM-002', bomCode: 'BOM-WB-001', productionLineId: 'L-ASM2', productionLineName: 'Assembly Line 2', quantity: 50, completedQuantity: 0, scrapQuantity: 0, status: 'RELEASED', priority: 2, dueDate: '2025-07-20T00:00:00Z', startedAt: null, completedAt: null, assignedTo: 'U-002', assignedToName: 'Tran Thi B', notes: 'Rush order', createdAt: '2025-06-30T00:00:00Z', updatedAt: '2025-06-30T00:00:00Z' },
  { id: 'WO-003', orderCode: 'WO-2025-003', productId: 'P005', productName: 'Engine Block', productCode: 'ENGINE-BLOCK', bomId: null, bomCode: null, productionLineId: 'L-ASM1', productionLineName: 'Assembly Line 1', quantity: 20, completedQuantity: 20, scrapQuantity: 0, status: 'COMPLETED', priority: 3, dueDate: '2025-06-30T00:00:00Z', startedAt: '2025-06-20T08:00:00Z', completedAt: '2025-06-29T16:00:00Z', assignedTo: 'U-001', assignedToName: 'Nguyen Van A', notes: null, createdAt: '2025-06-18T00:00:00Z', updatedAt: '2025-06-29T16:00:00Z' },
];

// ── Mock Stock Balances ──
const MOCK_STOCK_BALANCES = [
  { id: 'SB-001', productId: 'P001', productName: 'Steel Plate', productCode: 'STEEL-PLATE', warehouseId: 'WH-HN', warehouseName: 'Hanoi Main', locationId: 'LOC-HN-A1', locationCode: 'A-01', quantity: 1500, reservedQuantity: 200, availableQuantity: 1300, unitOfMeasure: 'KG', lastMovementAt: '2025-07-01T10:00:00Z', createdAt: '2025-01-15T08:00:00Z', updatedAt: '2025-07-01T10:00:00Z' },
  { id: 'SB-002', productId: 'P001', productName: 'Steel Plate', productCode: 'STEEL-PLATE', warehouseId: 'WH-HCM', warehouseName: 'HCMC Branch', locationId: 'LOC-HCM-A1', locationCode: 'A-01', quantity: 800, reservedQuantity: 100, availableQuantity: 700, unitOfMeasure: 'KG', lastMovementAt: '2025-06-28T14:00:00Z', createdAt: '2025-01-15T08:00:00Z', updatedAt: '2025-06-28T14:00:00Z' },
  { id: 'SB-003', productId: 'P002', productName: 'Aluminum Sheet', productCode: 'ALUM-SHEET', warehouseId: 'WH-HN', warehouseName: 'Hanoi Main', locationId: 'LOC-HN-A2', locationCode: 'A-02', quantity: 600, reservedQuantity: 300, availableQuantity: 300, unitOfMeasure: 'KG', lastMovementAt: '2025-07-01T09:00:00Z', createdAt: '2025-01-15T08:00:00Z', updatedAt: '2025-07-01T09:00:00Z' },
  { id: 'SB-004', productId: 'P004', productName: 'Circuit Board', productCode: 'CIRCUIT-BOARD', warehouseId: 'WH-HN', warehouseName: 'Hanoi Main', locationId: 'LOC-HN-B1', locationCode: 'B-01', quantity: 500, reservedQuantity: 100, availableQuantity: 400, unitOfMeasure: 'PCS', lastMovementAt: '2025-06-30T11:00:00Z', createdAt: '2025-01-15T08:00:00Z', updatedAt: '2025-06-30T11:00:00Z' },
  { id: 'SB-005', productId: 'P006', productName: 'Finished Widget A', productCode: 'WIDGET-A', warehouseId: 'WH-HCM', warehouseName: 'HCMC Branch', locationId: 'LOC-HCM-A2', locationCode: 'A-02', quantity: 200, reservedQuantity: 45, availableQuantity: 155, unitOfMeasure: 'PCS', lastMovementAt: '2025-07-01T08:30:00Z', createdAt: '2025-03-01T08:00:00Z', updatedAt: '2025-07-01T08:30:00Z' },
  { id: 'SB-006', productId: 'P008', productName: 'Lubricant Oil', productCode: 'LUBE-OIL', warehouseId: 'WH-DN', warehouseName: 'Da Nang', locationId: 'LOC-DN-A1', locationCode: 'DN-01', quantity: 100, reservedQuantity: 20, availableQuantity: 80, unitOfMeasure: 'L', lastMovementAt: '2025-06-25T15:00:00Z', createdAt: '2025-02-01T08:00:00Z', updatedAt: '2025-06-25T15:00:00Z' },
];

// ── Mock Stock Movements ──
const MOCK_STOCK_MOVEMENTS = [
  { id: 'SM-001', movementCode: 'REC-2025-001', productId: 'P001', productName: 'Steel Plate', productCode: 'STEEL-PLATE', movementType: 'RECEIPT', quantity: 500, fromWarehouseId: null, fromWarehouseName: null, fromLocationId: null, fromLocationCode: null, toWarehouseId: 'WH-HN', toWarehouseName: 'Hanoi Main', toLocationId: 'LOC-HN-A1', toLocationCode: 'A-01', referenceType: 'PURCHASE_ORDER', referenceId: 'PO-001', reason: 'Stock replenishment', performedBy: 'U-001', performedByName: 'Nguyen Van A', movementDate: '2025-07-01T10:00:00Z', notes: null, createdAt: '2025-07-01T10:00:00Z' },
  { id: 'SM-002', movementCode: 'ISS-2025-001', productId: 'P001', productName: 'Steel Plate', productCode: 'STEEL-PLATE', movementType: 'ISSUE', quantity: 100, fromWarehouseId: 'WH-HN', fromWarehouseName: 'Hanoi Main', fromLocationId: 'LOC-HN-A1', fromLocationCode: 'A-01', toWarehouseId: null, toWarehouseName: null, toLocationId: null, toLocationCode: null, referenceType: 'WORK_ORDER', referenceId: 'WO-001', reason: 'Production consumption', performedBy: 'U-002', performedByName: 'Tran Thi B', movementDate: '2025-07-01T08:00:00Z', notes: null, createdAt: '2025-07-01T08:00:00Z' },
  { id: 'SM-003', movementCode: 'TRF-2025-001', productId: 'P006', productName: 'Finished Widget A', productCode: 'WIDGET-A', movementType: 'TRANSFER_IN', quantity: 100, fromWarehouseId: null, fromWarehouseName: null, fromLocationId: null, fromLocationCode: null, toWarehouseId: 'WH-HCM', toWarehouseName: 'HCMC Branch', toLocationId: 'LOC-HCM-A2', toLocationCode: 'A-02', referenceType: 'PRODUCTION', referenceId: 'WO-001', reason: 'Completed production transfer', performedBy: 'U-001', performedByName: 'Nguyen Van A', movementDate: '2025-07-01T08:30:00Z', notes: null, createdAt: '2025-07-01T08:30:00Z' },
  { id: 'SM-004', movementCode: 'ADJ-2025-001', productId: 'P003', productName: 'Copper Wire', productCode: 'COPPER-WIRE', movementType: 'ADJUSTMENT', quantity: 50, fromWarehouseId: 'WH-HN', fromWarehouseName: 'Hanoi Main', fromLocationId: 'LOC-HN-A1', fromLocationCode: 'A-01', toWarehouseId: 'WH-HN', toWarehouseName: 'Hanoi Main', toLocationId: 'LOC-HN-A1', toLocationCode: 'A-01', referenceType: null, referenceId: null, reason: 'Inventory adjustment - found surplus', performedBy: 'U-003', performedByName: 'Le Van C', movementDate: '2025-06-30T14:00:00Z', notes: 'Cycle count discrepancy resolved', createdAt: '2025-06-30T14:00:00Z' },
  { id: 'SM-005', movementCode: 'ISS-2025-002', productId: 'P008', productName: 'Lubricant Oil', productCode: 'LUBE-OIL', movementType: 'ISSUE', quantity: 20, fromWarehouseId: 'WH-DN', fromWarehouseName: 'Da Nang', fromLocationId: 'LOC-DN-A1', fromLocationCode: 'DN-01', toWarehouseId: null, toWarehouseName: null, toLocationId: null, toLocationCode: null, referenceType: 'MAINTENANCE', referenceId: 'MT-001', reason: 'Machine lubrication', performedBy: 'U-004', performedByName: 'Pham Thi D', movementDate: '2025-06-25T15:00:00Z', notes: null, createdAt: '2025-06-25T15:00:00Z' },
];

// ── Mock Quality Inspections ──
const MOCK_QUALITY_INSPECTIONS = [
  { id: 'QC-001', inspectionCode: 'QC-2025-001', productId: 'P006', productName: 'Finished Widget A', productCode: 'WIDGET-A', workOrderId: 'WO-001', workOrderCode: 'WO-2025-001', inspector: 'U-005', inspectorName: 'Hoang Van E', inspectionDate: '2025-07-01T09:00:00Z', status: 'PASSED', sampleSize: 10, defectsFound: 0, defectRate: 0, notes: 'All samples passed quality check', remarks: null, createdAt: '2025-07-01T09:00:00Z', updatedAt: '2025-07-01T09:30:00Z' },
  { id: 'QC-002', inspectionCode: 'QC-2025-002', productId: 'P006', productName: 'Finished Widget A', productCode: 'WIDGET-A', workOrderId: 'WO-001', workOrderCode: 'WO-2025-001', inspector: 'U-005', inspectorName: 'Hoang Van E', inspectionDate: '2025-07-02T09:00:00Z', status: 'PENDING', sampleSize: 15, defectsFound: 0, defectRate: 0, notes: null, remarks: null, createdAt: '2025-07-02T09:00:00Z', updatedAt: '2025-07-02T09:00:00Z' },
  { id: 'QC-003', inspectionCode: 'QC-2025-003', productId: 'P007', productName: 'Finished Widget B', productCode: 'WIDGET-B', workOrderId: 'WO-002', workOrderCode: 'WO-2025-002', inspector: 'U-006', inspectorName: 'Nguyen Van F', inspectionDate: '2025-07-03T14:00:00Z', status: 'FAILED', sampleSize: 5, defectsFound: 2, defectRate: 40, notes: '2 units failed tolerance check', remarks: 'Rework required', createdAt: '2025-07-03T14:00:00Z', updatedAt: '2025-07-03T14:30:00Z' },
];

// ── Mock Maintenance Tickets ──
const MOCK_MAINTENANCE_TICKETS = [
  { id: 'MT-001', ticketCode: 'MT-2025-001', machineId: 'M-001', machineName: 'CNC Milling Machine 1', machineCode: 'CNC-001', title: 'Regular lubrication needed', description: 'Machine making unusual noise during operation', priority: 'MEDIUM', status: 'OPEN', reportedBy: 'U-001', reportedByName: 'Nguyen Van A', assignedTo: 'U-004', assignedToName: 'Pham Thi D', reportedAt: '2025-06-24T10:00:00Z', startedAt: null, closedAt: null, resolution: null, downtimeMinutes: null, createdAt: '2025-06-24T10:00:00Z', updatedAt: '2025-06-24T10:00:00Z' },
  { id: 'MT-002', ticketCode: 'MT-2025-002', machineId: 'M-004', machineName: 'CNC Lathe', machineCode: 'CNC-003', title: 'Tool changer malfunction', description: 'Automatic tool changer stuck in position', priority: 'HIGH', status: 'IN_PROGRESS', reportedBy: 'U-002', reportedByName: 'Tran Thi B', assignedTo: 'U-004', assignedToName: 'Pham Thi D', reportedAt: '2025-06-30T08:00:00Z', startedAt: '2025-06-30T09:00:00Z', closedAt: null, resolution: null, downtimeMinutes: 120, createdAt: '2025-06-30T08:00:00Z', updatedAt: '2025-06-30T09:00:00Z' },
  { id: 'MT-003', ticketCode: 'MT-2025-003', machineId: 'M-005', machineName: 'Packaging Machine', machineCode: 'PKG-001', title: 'Conveyor belt alignment', description: 'Belt drifting to the right, needs adjustment', priority: 'LOW', status: 'CLOSED', reportedBy: 'U-003', reportedByName: 'Le Van C', assignedTo: 'U-004', assignedToName: 'Pham Thi D', reportedAt: '2025-06-20T14:00:00Z', startedAt: '2025-06-21T08:00:00Z', closedAt: '2025-06-21T10:00:00Z', resolution: 'Belt tension adjusted and tracking corrected', downtimeMinutes: 120, createdAt: '2025-06-20T14:00:00Z', updatedAt: '2025-06-21T10:00:00Z' },
];

// ── Mock Auth response ──
const MOCK_USER = { id: '00000000-0000-0000-0000-000000000001', employeeId: 'ADMIN001', fullName: 'Factory Admin', username: 'admin', email: 'admin@factory.com', role: 'ADMIN', status: 'ACTIVE' };

export const mockInterceptor: HttpInterceptorFn = (req, next) => {
  const { url, method } = req;
  const base = '/api';

  function respond(data: unknown, status = 200) {
    return of(new HttpResponse({ status, body: data }));
  }

  // ── Warehouses ──
  if (url === `${base}/warehouses` && method === 'POST') {
    const body = req.body as any;
    const newWH = { id: `WH-${String(Date.now())}`, ...body, warehouseStatusName: 'ACTIVE', usedCapacity: 0, locationCount: 0, createdAt: ts(), updatedAt: ts() };
    MOCK_WAREHOUSES.push(newWH);
    return respond(ok(newWH));
  }
  if (matchUrl(url, `${base}/warehouses/:id/deactivate`) && method === 'PUT') {
    return respond(ok({ deactivated: true }));
  }
  if (matchUrl(url, `${base}/warehouses/:id`) && method === 'GET') {
    const id = extractParam(url, `${base}/warehouses/:id`);
    return respond(ok(MOCK_WAREHOUSES.find(w => w.id === id) || null));
  }
  if (url.startsWith(`${base}/warehouses`) && !url.includes('/locations') && method === 'GET') {
    return respond(okPage(MOCK_WAREHOUSES));
  }

  // ── Locations ──
  const locMatch = matchUrl(url, `${base}/warehouses/:wId/locations`);
  if (locMatch && method === 'GET') {
    const wId = locMatch[1];
    const locs = MOCK_LOCATIONS[wId] || [];
    return respond(okPage(locs));
  }
  if (locMatch && method === 'POST') {
    const wId = locMatch[1];
    const body = req.body as any;
    const newLoc = { id: `LOC-${String(Date.now())}`, warehouseId: wId, ...body, locationStatusName: 'ACTIVE', currentLoad: 0, createdAt: ts(), updatedAt: ts() };
    if (!MOCK_LOCATIONS[wId]) MOCK_LOCATIONS[wId] = [];
    MOCK_LOCATIONS[wId].push(newLoc);
    return respond(ok(newLoc));
  }
  const locByIdMatch = matchUrl(url, `${base}/warehouses/:wId/locations/:id`);
  if (locByIdMatch && method === 'GET') {
    const [, wId, id] = locByIdMatch;
    const loc = (MOCK_LOCATIONS[wId] || []).find((l: any) => l.id === id);
    return respond(ok(loc || null));
  }
  if (locByIdMatch && method === 'PUT') {
    const [, wId, id] = locByIdMatch;
    const locs = MOCK_LOCATIONS[wId] || [];
    const idx = locs.findIndex((l: any) => l.id === id);
    if (idx >= 0) locs[idx] = { ...locs[idx], ...req.body as any, updatedAt: ts() };
    return respond(ok(idx >= 0 ? locs[idx] : null));
  }

  // ── Production Lines ──
  if (url === `${base}/production-lines` && method === 'POST') {
    const body = req.body as any;
    const newLine = { id: `L-${String(Date.now())}`, ...body, lineStatusName: 'ACTIVE', machineCount: 0, createdAt: ts(), updatedAt: ts() };
    MOCK_PRODUCTION_LINES.push(newLine);
    return respond(ok(newLine));
  }
  if (matchUrl(url, `${base}/production-lines/:id`) && method === 'GET') {
    const id = extractParam(url, `${base}/production-lines/:id`);
    return respond(ok(MOCK_PRODUCTION_LINES.find(l => l.id === id) || null));
  }
  if (url.startsWith(`${base}/production-lines`) && method === 'GET') {
    return respond(okPage(MOCK_PRODUCTION_LINES));
  }

  // ── Machines ──
  if (url === `${base}/machines` && method === 'POST') {
    const body = req.body as any;
    const newMach = { id: `M-${String(Date.now())}`, ...body, status: 'IDLE', createdAt: ts(), updatedAt: ts() };
    MOCK_MACHINES.push(newMach);
    return respond(ok(newMach));
  }
  if (matchUrl(url, `${base}/machines/:id/status`) && method === 'PUT') {
    return respond(ok({ updated: true }));
  }
  if (matchUrl(url, `${base}/machines/:id`) && method === 'GET') {
    const id = extractParam(url, `${base}/machines/:id`);
    return respond(ok(MOCK_MACHINES.find(m => m.id === id) || null));
  }
  if (url.startsWith(`${base}/machines`) && method === 'GET') {
    return respond(okPage(MOCK_MACHINES));
  }

  // ── Work Orders ──
  if (url === `${base}/work-orders` && method === 'POST') {
    const body = req.body as any;
    const newWO = { id: `WO-${String(Date.now())}`, orderCode: `WO-${String(Date.now())}`, ...body, completedQuantity: 0, scrapQuantity: 0, status: 'DRAFT', createdAt: ts(), updatedAt: ts() };
    MOCK_WORK_ORDERS.push(newWO);
    return respond(ok(newWO));
  }
  if (matchUrl(url, `${base}/work-orders/:id`) && method === 'GET') {
    const id = extractParam(url, `${base}/work-orders/:id`);
    return respond(ok(MOCK_WORK_ORDERS.find(w => w.id === id) || null));
  }
  for (const action of ['reserve-materials', 'release-materials', 'start', 'pause', 'resume', 'complete', 'cancel']) {
    if (matchUrl(url, `${base}/work-orders/:id/${action}`) && method === 'PUT') {
      return respond(ok({ success: true }));
    }
  }
  if (url.startsWith(`${base}/work-orders`) && method === 'GET') {
    return respond(okPage(MOCK_WORK_ORDERS));
  }

  // ── Stock Balances ──
  if (url.startsWith(`${base}/stock-balances`) && method === 'GET') {
    return respond(okPage(MOCK_STOCK_BALANCES));
  }

  // ── Stock Movements ──
  if (url === `${base}/stock-movements` && method === 'POST') {
    const body = req.body as any;
    const newSM = { id: `SM-${String(Date.now())}`, movementCode: `MOV-${String(Date.now())}`, ...body, createdAt: ts() };
    return respond(ok(newSM));
  }
  if (url.startsWith(`${base}/stock-movements`) && method === 'GET') {
    return respond(okPage(MOCK_STOCK_MOVEMENTS));
  }

  // ── Quality Inspections ──
  if (url === `${base}/quality-inspections` && method === 'POST') {
    const body = req.body as any;
    const newQI = { id: `QC-${String(Date.now())}`, inspectionCode: `QC-${String(Date.now())}`, ...body, status: 'PENDING', defectRate: 0, createdAt: ts(), updatedAt: ts() };
    return respond(ok(newQI));
  }
  if (url.startsWith(`${base}/quality-inspections`) && method === 'GET') {
    return respond(okPage(MOCK_QUALITY_INSPECTIONS));
  }

  // ── Maintenance Tickets ──
  if (url === `${base}/maintenance-tickets` && method === 'POST') {
    const body = req.body as any;
    const newMT = { id: `MT-${String(Date.now())}`, ticketCode: `MT-${String(Date.now())}`, ...body, status: 'OPEN', reportedAt: ts(), createdAt: ts(), updatedAt: ts() };
    return respond(ok(newMT));
  }
  if (url.startsWith(`${base}/maintenance-tickets`) && method === 'GET') {
    return respond(okPage(MOCK_MAINTENANCE_TICKETS));
  }

  // ── Reports (mock data, no real API) ──
  const MOCK_REPORTS = [
    { id: 'RPT-001', name: 'Production Output Summary', category: 'Production', description: 'Daily production output by line and product', lastRun: '2025-07-28T08:00:00Z', createdAt: '2025-01-01T00:00:00Z' },
    { id: 'RPT-002', name: 'Quality Defect Analysis', category: 'Quality', description: 'Defect rates by product and inspection type', lastRun: '2025-07-27T10:00:00Z', createdAt: '2025-01-01T00:00:00Z' },
    { id: 'RPT-003', name: 'Inventory Valuation', category: 'Inventory', description: 'Current stock value by warehouse', lastRun: '2025-07-28T06:00:00Z', createdAt: '2025-01-15T00:00:00Z' },
    { id: 'RPT-004', name: 'Machine Utilization', category: 'Production', description: 'Machine runtime vs idle time analysis', lastRun: null, createdAt: '2025-02-01T00:00:00Z' },
    { id: 'RPT-005', name: 'Work Order Completion', category: 'Production', description: 'Work order status and completion rates', lastRun: '2025-07-26T12:00:00Z', createdAt: '2025-01-01T00:00:00Z' },
    { id: 'RPT-006', name: 'Maintenance Cost Report', category: 'Maintenance', description: 'Maintenance costs by machine and ticket', lastRun: null, createdAt: '2025-03-01T00:00:00Z' },
  ];
  if (url.startsWith(`${base}/reports`) && method === 'GET') {
    return respond(okPage(MOCK_REPORTS));
  }

  // ── Pass through if no mock matches ──
  return next(req);
};
