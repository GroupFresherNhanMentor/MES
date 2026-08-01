import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';

@Component({
  selector: 'app-reports-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, MatCardModule, MatIconModule, MatTabsModule],
  template: `
    <div class="page-header mb-4">
      <h1 class="page-heading text-2xl font-bold text-text-primary flex items-center gap-2">
        <mat-icon class="text-primary">analytics</mat-icon>
        Factory Operational Reports
      </h1>
      <p class="page-subtitle text-text-secondary text-sm">
        Analytical insights across inventory, production, quality, maintenance, and audit movement ledger
      </p>
    </div>

    <mat-card class="ff-card mb-6">
      <div class="border-b border-border-default px-4 pt-2">
        <nav mat-tab-nav-bar [tabPanel]="tabPanel" class="!bg-transparent">
          <a mat-tab-link routerLink="inventory-summary" routerLinkActive #rla1="routerLinkActive" [active]="rla1.isActive">
            <mat-icon class="mr-2">inventory_2</mat-icon> Inventory Summary
          </a>
          <a mat-tab-link routerLink="material-shortage" routerLinkActive #rla2="routerLinkActive" [active]="rla2.isActive">
            <mat-icon class="mr-2">warning</mat-icon> Material Shortage
          </a>
          <a mat-tab-link routerLink="production-output" routerLinkActive #rla3="routerLinkActive" [active]="rla3.isActive">
            <mat-icon class="mr-2">precision_manufacturing</mat-icon> Production Output
          </a>
          <a mat-tab-link routerLink="defect-rate" routerLinkActive #rla4="routerLinkActive" [active]="rla4.isActive">
            <mat-icon class="mr-2">rule</mat-icon> Defect Rate
          </a>
          <a mat-tab-link routerLink="machine-downtime" routerLinkActive #rla5="routerLinkActive" [active]="rla5.isActive">
            <mat-icon class="mr-2">build_circle</mat-icon> Machine Downtime
          </a>
          <a mat-tab-link routerLink="stock-movement-history" routerLinkActive #rla6="routerLinkActive" [active]="rla6.isActive">
            <mat-icon class="mr-2">history</mat-icon> Movement History
          </a>
        </nav>
      </div>
      <mat-tab-nav-panel #tabPanel class="p-4">
        <router-outlet></router-outlet>
      </mat-tab-nav-panel>
    </mat-card>
  `,
})
export class ReportsShellComponent {}
