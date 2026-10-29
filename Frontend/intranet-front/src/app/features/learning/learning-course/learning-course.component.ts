import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ActivatedRoute,
  Router
} from '@angular/router';

import {
  CourseService
} from '../../../core/services/courses.service';

import {
  CourseMaterialService
} from '../../../core/services/course-material.service';

import {
  Course
} from '../../../core/models/courses.models';

import {
  CourseMaterial,
  CourseMaterialProgress
} from '../../../core/models/course-materials.models';
import { EvaluationService } from '../../../core/services/evaluation.service';
import { PendingEvaluation } from '../../../core/models/evaluation.models';

@Component({
  selector: 'app-learning-course',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './learning-course.component.html',
  styleUrl: './learning-course.component.scss'
})
export class LearningCourseComponent
  implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly courseService =
    inject(CourseService);

  private readonly materialService =
    inject(CourseMaterialService);

  private readonly evaluationService = 
    inject(EvaluationService);

pendingEvaluation: PendingEvaluation | null = null;

  course: Course | null = null;

  materials: CourseMaterial[] = [];

  loading = false;

  errorMessage = '';

  progressMap = new Map<number, CourseMaterialProgress>();
  courseProgress  = 0;
  canTakeEvaluation = false;

  ngOnInit(): void {

    const courseId =
      Number(
        this.route.snapshot.paramMap.get('courseId')
      );

    if (!courseId) {

      this.errorMessage =
        'Curso no válido.';

      return;

    }

    this.loadCourse(courseId);
    this.loadMaterials(courseId);
    this.loadPendingEvaluation(courseId);
  }

  private loadCourse(courseId: number): void {

    this.loading = true;

    this.courseService
      .getCourse(courseId)
      .subscribe({

        next: course => {

          this.course = course;

          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando curso:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudo cargar el curso.';

        }

      });

  }

  private loadMaterials(courseId: number): void {

    this.materialService
      .getByCourse(courseId)
      .subscribe({

        next: materials => {

          this.materials =
            materials.filter(
              material => material.active
            );

          this.loadMaterialsProgress(courseId);

        },

        error: error => {

          console.error(
            'Error cargando materiales:',
            error
          );

        }

      });
  }

  private calculateCourseProgress(): void {

    const requiredMaterials =
      this.materials.filter(
        material => material.required
      );

    if (requiredMaterials.length === 0) {

      this.courseProgress = 0;
      this.canTakeEvaluation = false;

      return;
    }

    const totalProgress =
      requiredMaterials.reduce(
        (total, material) => {

          const progress =
            this.progressMap.get(material.id);

          return total +
            (progress?.progressPercent ?? 0);

        },
        0
      );

    this.courseProgress =
      Math.round(
        totalProgress /
        requiredMaterials.length
      );

    this.canTakeEvaluation =
      requiredMaterials.every(
        material => {

          const progress =
            this.progressMap.get(material.id);

          return progress?.progressPercent === 100;

        }
      );
  }

  private loadMaterialsProgress(courseId: number): void {

    this.progressMap.clear();

    if (this.materials.length === 0) {
      this.calculateCourseProgress();
      return;
    }

    let completedRequests = 0;

    this.materials.forEach(material => {

      this.materialService
        .getProgress(
          courseId,
          material.id
        )
        .subscribe({

          next: progress => {

            this.progressMap.set(
              material.id,
              progress
            );

            completedRequests++;

            if (
              completedRequests ===
              this.materials.length
            ) {
              this.calculateCourseProgress();
            }

          },

          error: error => {

            console.error(
              `Error cargando progreso del material ${material.id}:`,
              error
            );

            completedRequests++;

            if (
              completedRequests ===
              this.materials.length
            ) {
              this.calculateCourseProgress();
            }

          }

        });

    });
  }

  goBack(): void {

    this.router.navigate([
      '/learning'
    ]);

  }

  openMaterial(material: CourseMaterial): void {

    if (!this.course) {
      return;
    }

    this.router.navigate([
      '/learning/courses',
      this.course.id,
      'material',
      material.id
    ]);
  }

  getMaterialProgress(
    materialId: number
  ): number {

    return this.progressMap
      .get(materialId)
      ?.progressPercent ?? 0;
  }

  isMaterialCompleted(
    materialId: number
  ): boolean {

    return this.getMaterialProgress(materialId) === 100;
  }

  startEvaluation(): void {

    if (!this.course || !this.canTakeEvaluation) {
      return;
    }

    this.router.navigate([
      '/learning/courses',
      this.course.id,
      'evaluation'
    ]);
  }

  private loadPendingEvaluation(courseId: number): void {
    this.evaluationService.getPendingEvaluations().subscribe({
      next: evaluations => {
        this.pendingEvaluation =
          evaluations.find(evaluation => evaluation.courseId === courseId) ?? null;
      },
      error: error => {
        console.error('Error consultando evaluaciones pendientes:', error);
      }
    });
  }

continueEvaluation(): void {
    if (!this.course || !this.pendingEvaluation) {
      return;
    }

    this.router.navigate(
      ['/learning/courses', this.course.id, 'evaluation'],
      {
        queryParams: {
          attemptId: this.pendingEvaluation.attemptId
        }
      }
    );
  }
  
}