const BASE = '/api';

export const API = {
  auth: {
    login: `${BASE}/auth/login`,
    refresh: `${BASE}/auth/refresh`,
    me: `${BASE}/auth/me`,
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
  },
  stockBalances: {
    base: `${BASE}/stock-balances`,
  },
  stockMovements: {
    base: `${BASE}/stock-movements`,
  },
  qualityInspections: {
    base: `${BASE}/quality-inspections`,
    pass: (id: string | number) => `${BASE}/quality-inspections/${id}/pass`,
    fail: (id: string | number) => `${BASE}/quality-inspections/${id}/fail`,
    hold: (id: string | number) => `${BASE}/quality-inspections/${id}/hold`,
    release: (id: string | number) => `${BASE}/quality-inspections/${id}/release`,
    scrap: (id: string | number) => `${BASE}/quality-inspections/${id}/scrap`,
  },
  maintenanceTickets: {
    base: `${BASE}/maintenance-tickets`,
    start: (id: string | number) => `${BASE}/maintenance-tickets/${id}/start`,
    close: (id: string | number) => `${BASE}/maintenance-tickets/${id}/close`,
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
