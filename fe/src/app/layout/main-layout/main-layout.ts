import { Component, inject, signal, effect } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AuthService } from '../../core/services/auth';
import { LoadingService } from '../../core/services/loading';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  roles?: string[];
}

@Component({
  selector: 'app-main-layout',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatButtonModule, MatIconModule,
    MatListModule, MatDividerModule, MatMenuModule, MatBadgeModule,
    MatProgressBarModule,
  ],
  templateUrl: './main-layout.html',
})
export class MainLayout {
  private readonly authService = inject(AuthService);
  private readonly loadingService = inject(LoadingService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly breakpointObserver = inject(BreakpointObserver);

  readonly currentUser = this.authService.getCurrentUser();
  readonly isAdmin = this.currentUser?.role === 'ADMIN';
  readonly isLoading = this.loadingService.isLoading;

  readonly isMobile = toSignal(
    this.breakpointObserver.observe([Breakpoints.Handset, Breakpoints.TabletPortrait]).pipe(
      map(result => result.matches)
    ),
    { initialValue: false }
  );

  readonly sidenavOpened = signal(true);

  constructor() {
    effect(() => {
      // Automatically close sidenav on mobile, open on desktop
      this.sidenavOpened.set(!this.isMobile());
    }, { allowSignalWrites: true });
  }

  readonly pageTitle = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      startWith(null),
      map(() => {
        let route = this.activatedRoute.snapshot;
        while (route.firstChild) route = route.firstChild;
        return (route.data['title'] as string | undefined) ?? 'Dashboard';
      }),
    ),
    { initialValue: 'Dashboard' },
  );

  readonly navItems: NavItem[] = [
    { path: '/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/products', label: 'Products', icon: 'inventory_2', roles: ['ADMIN'] },
    { path: '/warehouses', label: 'Warehouses', icon: 'warehouse', roles: ['ADMIN', 'WAREHOUSE_MANAGER'] },
    { path: '/production-lines', label: 'Production Lines', icon: 'precision_manufacturing', roles: ['ADMIN'] },
    { path: '/machines', label: 'Machines', icon: 'settings', roles: ['ADMIN', 'PRODUCTION_OPERATOR'] },
    { path: '/boms', label: 'BOM', icon: 'description', roles: ['ADMIN', 'PLANNER'] },
    { path: '/work-orders', label: 'Work Orders', icon: 'assignment', roles: ['ADMIN', 'PLANNER', 'PRODUCTION_OPERATOR'] },
    { path: '/stock-balances', label: 'Stock', icon: 'shelves', roles: ['ADMIN', 'WAREHOUSE_MANAGER'] },
    { path: '/stock-movements', label: 'Movements', icon: 'swap_horiz', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'AUDITOR'] },
    { path: '/stock-adjustments', label: 'Adjustments', icon: 'pending_actions', roles: ['ADMIN', 'FACTORY_MANAGER'] },
    { path: '/quality-inspections', label: 'Quality', icon: 'fact_check', roles: ['ADMIN', 'QC_INSPECTOR'] },
    { path: '/maintenance-tickets', label: 'Maintenance', icon: 'build', roles: ['ADMIN', 'MAINTENANCE_ENGINEER'] },
    { path: '/reports', label: 'Reports', icon: 'bar_chart', roles: ['ADMIN', 'FACTORY_MANAGER'] },
  ];

  readonly filteredNavItems = this.navItems.filter(
    (item) => !item.roles || item.roles.includes(this.currentUser?.role ?? ''),
  );

  toggleSidenav(): void {
    this.sidenavOpened.update((v) => !v);
  }

  closeSidenavOnMobile(): void {
    if (this.isMobile()) {
      this.sidenavOpened.set(false);
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
