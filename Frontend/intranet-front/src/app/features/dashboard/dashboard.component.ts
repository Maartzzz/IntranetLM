import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { Dashboard } from '../../core/models/dashboard.models';
import { DashboardService } from '../../core/services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  private readonly dashboardService =
    inject(DashboardService);

  dashboard: Dashboard | null = null;

  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {

    this.loading = true;
    this.errorMessage = '';

    this.dashboardService
      .getDashboard()
      .subscribe({
        next: (dashboard) => {
          this.dashboard = dashboard;
          this.loading = false;
        },
        error: () => {
          this.errorMessage =
            'No se pudo cargar la información del dashboard.';

          this.loading = false;
        }
      });
  }
}