import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EvaluationService } from '../../core/services/evaluation.service';
import { MyEvaluationResponse } from '../../core/models/evaluation.models';
import { CourseService } from '../../core/services/courses.service';
import { Course } from '../../core/models/courses.models';

@Component({
  selector: 'app-my-evaluations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './evaluations.component.html',
  styleUrl: './evaluations.component.scss'
})
export class EvaluationsComponent implements OnInit {

  private readonly evaluationService = inject(EvaluationService);
  private readonly router = inject(Router);
  private readonly courseService = inject(CourseService);

  courses: Course[] = [];
  loadingCourses = false;
  courseErrorMessage = '';

  evaluations: MyEvaluationResponse[] = [];
  loading = true;
  errorMessage = '';

  downloadingCertificateId: number | null = null;
  certificateErrorMessage = '';

  get pendingEvaluations(): MyEvaluationResponse[] {
    return this.evaluations.filter(
      evaluation => evaluation.completedAt === null
    );
  }

  get completedEvaluations(): MyEvaluationResponse[] {
    return this.evaluations.filter(
      evaluation => evaluation.completedAt !== null
    );
  }

  ngOnInit(): void {
    this.loadEvaluations();
    this.loadCourses();
  }

  loadEvaluations(): void {
    this.loading = true;
    this.errorMessage = '';

    this.evaluationService.getMyEvaluations().subscribe({
      next: evaluations => {
        this.evaluations = evaluations;
        this.loading = false;
      },
      error: error => {
        console.error('Error al cargar evaluaciones', error);
        this.errorMessage = 'No se pudieron cargar tus evaluaciones.';
        this.loading = false;
      }
    });
  }

  continueEvaluation(evaluation: MyEvaluationResponse): void {
    this.router.navigate(
      ['/learning/courses', evaluation.courseId, 'evaluation'],
      {
        queryParams: { attemptId: evaluation.attemptId }
      }
    );
  }

loadCourses(): void {
  this.loadingCourses = true;
  this.courseErrorMessage = '';

  this.courseService.getAvailableEvaluationCourses().subscribe({
    next: courses => {
      this.courses = courses;
      this.loadingCourses = false;
    },
    error: error => {
      console.error('Error al cargar los cursos disponibles', error);
      this.courseErrorMessage = 'No se pudieron cargar los cursos disponibles.';
      this.loadingCourses = false;
    }
  });
}

  startEvaluation(courseId: number): void {
    this.evaluationService.startEvaluation(courseId).subscribe({
      next: response => {
        this.router.navigate(
          ['/learning/courses', courseId, 'evaluation'],
          { queryParams: { attemptId: response.attemptId } }
        );
      },
      error: error => {
        const message = error?.error?.message
          || 'No se pudo iniciar la evaluación. Verifica que hayas completado los materiales obligatorios y que tengas intentos disponibles.';

        this.errorMessage = message;
      }
    });

  }

  downloadCertificate(evaluation: MyEvaluationResponse): void {
    if (
      evaluation.completedAt === null ||
      evaluation.passed !== true
    ) {
      return;
    }

    this.downloadingCertificateId = evaluation.attemptId;
    this.certificateErrorMessage = '';

    this.evaluationService
      .downloadCertificate(evaluation.attemptId)
      .subscribe({
        next: pdf => {
          const url = URL.createObjectURL(pdf);
          const link = document.createElement('a');

          link.href = url;
          link.download = `certificado-${evaluation.attemptId}.pdf`;
          link.click();

          URL.revokeObjectURL(url);
          this.downloadingCertificateId = null;
        },
        error: error => {
          console.error('Error al descargar el certificado', error);
          this.certificateErrorMessage =
            'No se pudo descargar el certificado. Inténtalo nuevamente.';
          this.downloadingCertificateId = null;
        }
      });
  }

}