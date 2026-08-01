import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';
import { reportGuard } from './core/guards/report-guard';
import { MainLayout } from './layout/main-layout/main-layout';
import { AuthLayout } from './layout/auth-layout/auth-layout';

export const routes: Routes = [
  {
    path: '',
    component: AuthLayout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'login' },
      {
        path: 'login',
        loadComponent: () => import('./features/auth/pages/login/login').then((m) => m.Login),
      },
    ],
  },
  {
    path: '',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/pages/dashboard-home/dashboard-home').then((m) => m.DashboardHome),
        data: { title: 'Dashboard' },
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/users/pages/user-list/user-list').then((m) => m.UserListComponent),
        data: { title: 'User Management' },
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
        path: 'boms/:id',
        loadComponent: () =>
          import('./features/boms/pages/bom-detail/bom-detail').then((m) => m.BomDetail),
        data: { title: 'BOM Details' },
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
        path: 'stock-adjustments',
        loadComponent: () =>
          import('./features/stock-adjustments/pages/stock-adjustment-list/stock-adjustment-list').then(
            (m) => m.StockAdjustmentList,
          ),
        data: { title: 'Pending Adjustments' },
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
        canActivate: [reportGuard],
        loadComponent: () =>
          import('./features/reports/pages/reports-shell/reports-shell').then((m) => m.ReportsShellComponent),
        data: { title: 'Operational Reports' },
        children: [
          { path: '', redirectTo: 'inventory-summary', pathMatch: 'full' },
          {
            path: 'inventory-summary',
            loadComponent: () =>
              import('./features/reports/pages/inventory-summary/inventory-summary').then(
                (m) => m.InventorySummaryComponent,
              ),
          },
          {
            path: 'material-shortage',
            loadComponent: () =>
              import('./features/reports/pages/material-shortage/material-shortage').then(
                (m) => m.MaterialShortageComponent,
              ),
          },
          {
            path: 'production-output',
            loadComponent: () =>
              import('./features/reports/pages/production-output/production-output').then(
                (m) => m.ProductionOutputComponent,
              ),
          },
          {
            path: 'defect-rate',
            loadComponent: () =>
              import('./features/reports/pages/defect-rate/defect-rate').then((m) => m.DefectRateComponent),
          },
          {
            path: 'machine-downtime',
            loadComponent: () =>
              import('./features/reports/pages/machine-downtime/machine-downtime').then(
                (m) => m.MachineDowntimeComponent,
              ),
          },
          {
            path: 'stock-movement-history',
            loadComponent: () =>
              import('./features/reports/pages/stock-movement-history/stock-movement-history').then(
                (m) => m.StockMovementHistoryComponent,
              ),
          },
        ],
      },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '/dashboard' },
];
