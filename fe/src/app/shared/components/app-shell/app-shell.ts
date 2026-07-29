import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet, NavigationEnd, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map, startWith } from 'rxjs';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { AuthService } from '../../../core/services/auth';
import { LoadingService } from '../../../core/services/loading';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  roles?: string[];
}

@Component({
  selector: 'app-app-shell',
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatSidenavModule, MatButtonModule, MatIconModule,
    MatListModule, MatDividerModule, MatMenuModule, MatBadgeModule,
    MatProgressBarModule,
  ],
  templateUrl: './app-shell.html',
  styleUrl: './app-shell.scss',
})
export class AppShell {
  private readonly authService = inject(AuthService);
  private readonly loadingService = inject(LoadingService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly currentUser = this.authService.getCurrentUser();
  readonly isAdmin = this.currentUser?.role === 'ADMIN';
  readonly isLoading = this.loadingService.isLoading;

  readonly sidenavOpened = signal(true);

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
    { path: '/products', label: 'Products', icon: 'inventory_2' },
    { path: '/warehouses', label: 'Warehouses', icon: 'warehouse' },
    { path: '/production-lines', label: 'Production Lines', icon: 'precision_manufacturing' },
    { path: '/machines', label: 'Machines', icon: 'settings' },
    { path: '/boms', label: 'Bill of Materials', icon: 'description' },
    { path: '/work-orders', label: 'Work Orders', icon: 'assignment' },
    { path: '/stock-balances', label: 'Stock Balances', icon: 'shelves' },
    { path: '/stock-movements', label: 'Stock Movements', icon: 'swap_horiz' },
    { path: '/quality-inspections', label: 'Quality', icon: 'fact_check' },
    { path: '/maintenance-tickets', label: 'Maintenance', icon: 'build' },
    { path: '/reports', label: 'Reports', icon: 'bar_chart', roles: ['ADMIN'] },
  ];

  readonly filteredNavItems = this.navItems;

  toggleSidenav(): void {
    this.sidenavOpened.update((v) => !v);
  }

  logout(): void {
    this.authService.logout();
  }
}
