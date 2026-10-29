
import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { Dashboard } from '../../core/models/dashboard.models';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/auth/auth.service';
import { ExhaustedEvaluation } from '../../core/models/exhausted-evaluation.models';
import { ExhaustedEvaluationService } from '../../core/services/exhausted-evaluation.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {

  private readonly dashboardService = inject(DashboardService);
  private readonly authService = inject(AuthService);
  private readonly exhaustedEvaluationService =
    inject(ExhaustedEvaluationService);

  dashboard: Dashboard | null = null;
  exhaustedEvaluations: ExhaustedEvaluation[] = [];
  rehabilitatingKey: string | null = null;
  successMessage = '';
  rehabilitationError = '';

  loading = false;
  exhaustedLoading = false;
  errorMessage = '';
  exhaustedErrorMessage = '';

  get canManageEvaluations(): boolean {
    return this.authService.isAdmin() ||
          this.authService.isSupervisor();
  }

  ngOnInit(): void {
    this.loadDashboard();

    if (this.canManageEvaluations) {
      this.loadExhaustedEvaluations();
    }
  }

  loadDashboard(): void {
    this.loading = true;
    this.errorMessage = '';

    this.dashboardService.getDashboard().subscribe({
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

  loadExhaustedEvaluations(): void {
    this.exhaustedLoading = true;
    this.exhaustedErrorMessage = '';

    this.exhaustedEvaluationService
      .getExhaustedEvaluations()
      .subscribe({
        next: (evaluations) => {
          this.exhaustedEvaluations = evaluations;
          this.exhaustedLoading = false;
        },
        error: () => {
          this.exhaustedErrorMessage =
            'No se pudieron cargar las evaluaciones agotadas.';
          this.exhaustedLoading = false;
        }
      });
  }

  rehabilitateEvaluation(evaluation: ExhaustedEvaluation): void {
    const confirmed = window.confirm(
      `¿Deseas rehabilitar la evaluación de ${evaluation.fullName} ` +
      `para el curso "${evaluation.courseTitle}"? Se habilitará una nueva ronda de 3 intentos.`
    );

    if (!confirmed) {
      return;
    }

    const key = `${evaluation.courseId}-${evaluation.username}`;

    this.rehabilitatingKey = key;
    this.successMessage = '';
    this.rehabilitationError = '';

    this.exhaustedEvaluationService
      .rehabilitateEvaluation(
        evaluation.courseId,
        evaluation.username
      )
      .subscribe({
        next: () => {
          this.rehabilitatingKey = null;
          this.successMessage =
            `Se rehabilitó la evaluación de ${evaluation.fullName}.`;
          this.loadExhaustedEvaluations();
        },
        error: (error) => {
          this.rehabilitatingKey = null;
          this.rehabilitationError =
            error.status === 409
              ? 'La evaluación ya no cumple las condiciones para rehabilitarse.'
              : 'No se pudo rehabilitar la evaluación. Verifica los permisos e inténtalo nuevamente.';
        }
    });
  }

}