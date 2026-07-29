import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';
import { AppShell } from './shared/components/app-shell/app-shell';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login').then((m) => m.Login),
  },
  {
    path: '',
    component: AppShell,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard-home/dashboard-home').then((m) => m.DashboardHome),
        data: { title: 'Dashboard' },
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/products/pages/product-list/product-list').then((m) => m.ProductListComponent),
        data: { title: 'Products' },
      },
      {
        path: 'warehouses',
        loadComponent: () =>
          import('./features/warehouses/pages/warehouse-list/warehouse-list').then((m) => m.WarehouseList),
        data: { title: 'Warehouses' },
      },
      {
        path: 'warehouses/:warehouseId/locations',
        loadComponent: () =>
          import('./features/locations/pages/location-list/location-list').then((m) => m.LocationList),
        data: { title: 'Locations' },
      },
      {
        path: 'production-lines',
        loadComponent: () =>
          import('./features/production-lines/pages/production-line-list/production-line-list').then(
            (m) => m.ProductionLineList,
          ),
        data: { title: 'Production Lines' },
      },
      {
        path: 'machines',
        loadComponent: () =>
          import('./features/machines/pages/machine-list/machine-list').then((m) => m.MachineList),
        data: { title: 'Machines' },
      },
      {
        path: 'boms',
        loadComponent: () => import('./features/boms/pages/bom-list/bom-list').then((m) => m.BomList),
        data: { title: 'Bill of Materials' },
      },
      {
        path: 'work-orders',
        loadComponent: () =>
          import('./features/work-orders/pages/work-order-list/work-order-list').then((m) => m.WorkOrderList),
        data: { title: 'Work Orders' },
      },
      {
        path: 'stock-balances',
        loadComponent: () =>
          import('./features/stock-balances/pages/stock-balance-list/stock-balance-list').then(
            (m) => m.StockBalanceList,
          ),
        data: { title: 'Stock Balances' },
      },
      {
        path: 'stock-movements',
        loadComponent: () =>
          import('./features/stock-movements/pages/stock-movement-list/stock-movement-list').then(
            (m) => m.StockMovementList,
          ),
        data: { title: 'Stock Movements' },
      },
      {
        path: 'quality-inspections',
        loadComponent: () =>
          import('./features/quality-inspections/pages/quality-inspection-list/quality-inspection-list').then(
            (m) => m.QualityInspectionList,
          ),
        data: { title: 'Quality Inspections' },
      },
      {
        path: 'maintenance-tickets',
        loadComponent: () =>
          import('./features/maintenance-tickets/pages/maintenance-ticket-list/maintenance-ticket-list').then(
            (m) => m.MaintenanceTicketList,
          ),
        data: { title: 'Maintenance Tickets' },
      },
      {
        path: 'reports',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/reports/pages/report-list/report-list').then((m) => m.ReportList),
        data: { title: 'Reports' },
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '/dashboard' },
];
