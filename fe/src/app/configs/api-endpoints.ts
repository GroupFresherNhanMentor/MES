const BASE = '/api';

export const API = {
  auth: {
    login: `${BASE}/auth/login`,
    refresh: `${BASE}/auth/refresh`,
    me: `${BASE}/auth/me`,
  },
  users: {
    base: `${BASE}/users`,
    byId: (id: string) => `${BASE}/users/${id}`,
    activate: (id: string) => `${BASE}/users/${id}/activate`,
    deactivate: (id: string) => `${BASE}/users/${id}/deactivate`,
    roles: (id: string) => `${BASE}/users/${id}/roles`,
  },
  roles: {
    base: `${BASE}/roles`,
  },
  products: {
    base: `${BASE}/products`,
    byId: (id: string | number) => `${BASE}/products/${id}`,
    types: `${BASE}/product-types`,
    statuses: `${BASE}/product-statuses`,
    unitsOfMeasure: `${BASE}/units-of-measure`,
    productTypes: `${BASE}/product-types`,
    productStatuses: `${BASE}/product-statuses`,
    deactivate: (id: string | number) => `${BASE}/products/${id}/deactivate`,
  },
  warehouses: {
    base: `${BASE}/warehouses`,
    byId: (id: string | number) => `${BASE}/warehouses/${id}`,
    deactivate: (id: string | number) => `${BASE}/warehouses/${id}/deactivate`,
  },
  locations: {
    base: (warehouseId: string | number) => `${BASE}/warehouses/${warehouseId}/locations`,
    byId: (warehouseId: string | number, id: string | number) =>
      `${BASE}/warehouses/${warehouseId}/locations/${id}`,
    deactivate: (warehouseId: string | number, id: string | number) =>
      `${BASE}/warehouses/${warehouseId}/locations/${id}/deactivate`,
  },
  productionLines: {
    base: `${BASE}/lines`,
    byId: (id: string | number) => `${BASE}/lines/${id}`,
    deactivate: (id: string | number) => `${BASE}/production-lines/${id}/deactivate`,
  },
  machines: {
    base: `${BASE}/machines`,
    byId: (id: string | number) => `${BASE}/machines/${id}`,
    deactivate: (id: string | number) => `${BASE}/machines/${id}/deactivate`,
    changeStatus: (id: string | number) => `${BASE}/machines/${id}/status`,
  },
  boms: {
    base: `${BASE}/boms`,
    byId: (id: string | number) => `${BASE}/boms/${id}`,
    activate: (id: string | number) => `${BASE}/boms/${id}/activate`,
    deactivate: (id: string | number) => `${BASE}/boms/${id}/deactivate`,
    newVersion: (id: string | number) => `${BASE}/boms/${id}/new-version`,
    items: (bomId: string | number) => `${BASE}/boms/${bomId}/items`,
    statuses: `${BASE}/bom-statuses`,
  },
  workOrders: {
    base: `${BASE}/work-orders`,
    byId: (id: string | number) => `${BASE}/work-orders/${id}`,
    reserveMaterials: (id: string | number) => `${BASE}/work-orders/${id}/reserve-materials`,
    releaseMaterials: (id: string | number) => `${BASE}/work-orders/${id}/release-materials`,
    start: (id: string | number) => `${BASE}/work-orders/${id}/start`,
    pause: (id: string | number) => `${BASE}/work-orders/${id}/pause`,
    resume: (id: string | number) => `${BASE}/work-orders/${id}/resume`,
    complete: (id: string | number) => `${BASE}/work-orders/${id}/complete`,
    cancel: (id: string | number) => `${BASE}/work-orders/${id}/cancel`,
    statuses: `${BASE}/work-orders/statuses`,
    priorities: `${BASE}/work-orders/priorities`,
    eventTypes: `${BASE}/work-orders/event-types`,
  },
  stockBalances: {
    base: `${BASE}/stock-balances`,
  },
  stockMovements: {
    base: `${BASE}/stock-movements`,
    in: `${BASE}/stock-in`,
  },
  stockTransfers: {
    base: `${BASE}/stock-transfers`,
  },
  stockAdjustments: {
    base: `${BASE}/stock-adjustments`,
    pending: `${BASE}/stock-adjustments/pending`,
    approve: (id: string) => `${BASE}/stock-adjustments/${id}/approve`,
    reject: (id: string) => `${BASE}/stock-adjustments/${id}/reject`,
  },
  stockLots: {
    base: `${BASE}/stock-lots`,
  },
  qualityInspections: {
    base: `${BASE}/quality-inspections`,
    results: (id: string | number) => `${BASE}/quality-inspections/${id}/results`,
    pass: (id: string | number) => `${BASE}/quality-inspections/${id}/pass`,
    fail: (id: string | number) => `${BASE}/quality-inspections/${id}/fail`,
    defectTypes: `${BASE}/defect-types`,
    qcActions: `${BASE}/qc-actions`,
    qcStatuses: `${BASE}/qc-statuses`,
  },
  maintenanceTickets: {
    base: `${BASE}/maintenance`,
    byId: (id: string | number) => `${BASE}/maintenance/${id}`,
    start: (id: string | number) => `${BASE}/maintenance/${id}/start`,
    close: (id: string | number) => `${BASE}/maintenance/${id}/close`,
    resolve: (id: string | number) => `${BASE}/maintenance/${id}/resolve`,
    cancel: (id: string | number) => `${BASE}/maintenance/${id}/cancel`,
    ticketStatuses: `${BASE}/maintenance/ticket-statuses`,
    ticketPriorities: `${BASE}/maintenance/ticket-priorities`,
    engineers: `${BASE}/maintenance/engineers`,
    downtimeByTicket: (id: string | number) => `${BASE}/maintenance/${id}/downtime`,
  },
  reports: {
    inventory: `${BASE}/reports/inventory`,
    materialShortage: `${BASE}/reports/material-shortage`,
    productionOutput: `${BASE}/reports/production-output`,
    defectRate: `${BASE}/reports/defect-rate`,
    machineDowntime: `${BASE}/reports/machine-downtime`,
    stockMovements: `${BASE}/reports/stock-movements`,
  },
} as const;
