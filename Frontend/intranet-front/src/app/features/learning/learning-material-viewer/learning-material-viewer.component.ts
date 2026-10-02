import {
  Component,
  OnDestroy,
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
  DomSanitizer,
  SafeResourceUrl
} from '@angular/platform-browser';

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
import { Observable, of } from 'rxjs';

@Component({
  selector: 'app-learning-material-viewer',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl:
    './learning-material-viewer.component.html',
  styleUrl:
    './learning-material-viewer.component.scss'
})
export class LearningMaterialViewerComponent
  implements OnInit, OnDestroy {

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly courseService =
    inject(CourseService);

  private readonly materialService =
    inject(CourseMaterialService);

  private readonly sanitizer =
    inject(DomSanitizer);

  course: Course | null = null;

  materials: CourseMaterial[] = [];

  material: CourseMaterial | null = null;

  contentUrl: SafeResourceUrl | null = null;

  private objectUrl: string | null = null;

  progress: CourseMaterialProgress | null = null

  private lastSavedProgress = 0
  private lastSavedPosition= 0
  private readonly progressIntervalSeconds = 10

  private currentVideo: HTMLVideoElement | null = null  

  currentIndex = 0;

  loading = false;

  progressLoading = false;

  errorMessage = '';

  ngOnInit(): void {

    this.route.paramMap.subscribe(params => {

      const courseId =
        Number(params.get('courseId'));

      const materialId =
        Number(params.get('materialId'));

      if (!courseId || !materialId) {

        this.errorMessage =
          'Contenido no válido.';

        return;
      }

      this.loadCourse(courseId);
      this.loadMaterials(courseId, materialId);

    });
  }

  private loadCourse(courseId: number): void {

    this.courseService
      .getCourse(courseId)
      .subscribe({

        next: course => {

          this.course = course;

        },

        error: error => {

          console.error(
            'Error cargando curso:',
            error
          );

        }

      });

  }

  private loadMaterials(
    courseId: number,
    materialId: number
  ): void {

    this.materialService
      .getByCourse(courseId)
      .subscribe({

        next: materials => {

          this.materials =
            materials.filter(
              material => material.active
            );

          this.currentIndex =
            this.materials.findIndex(
              material =>
                material.id === materialId
            );

          if (this.currentIndex === -1) {

            this.errorMessage =
              'El contenido no existe.';

            return;

          }

          this.material =
            this.materials[this.currentIndex];

          this.loadProgress();
          this.loadContent();

        },

        error: error => {

          console.error(
            'Error cargando materiales:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los contenidos.';

        }

      });

  }

  private loadProgress(): void {

    if (!this.course || !this.material) {
      return;
    }

    this.progressLoading = true;

    this.materialService
      .getProgress(
        this.course.id,
        this.material.id
      )
      .subscribe({

      next: progress => {

        this.progress = progress;

        this.progressLoading = false;

        if (
          this.material &&
          this.material.type !== 'VIDEO' &&
          progress.progressPercent < 100
        ) {
          this.completeNonVideoMaterial();
        }
      },

        error: error => {

          console.warn(
            'No existe progreso todavía:',
            error
          );

          this.progress = null;
          this.progressLoading = false;

        }

      });
  }

  private completeNonVideoMaterial(): void {

    if (!this.course || !this.material) {
      return;
    }

    this.materialService
      .updateProgress(
        this.course.id,
        this.material.id,
        {
          progressPercent: 100,
          lastPosition: null
        }
      )
      .subscribe({
        next: progress => {
          this.progress = progress;
        },
        error: error => {
          console.error(
            'Error marcando material como completado:',
            error
          );
        }
      });
  }

  private loadContent(): void {

    if (!this.material) {
      return;
    }

    this.revokeObjectUrl();

    this.contentUrl = null;

    this.loading = false;
    this.errorMessage = '';

    const type =
      this.material.type;

    if (
      type !== 'VIDEO' &&
      type !== 'PDF' &&
      type !== 'DOCUMENT' &&
      type !== 'PPTX'
    ) {

      return;

    }

    this.loading = true;

    this.materialService
      .getContent(this.material.id)
      .subscribe({

        next: blob => {

          this.objectUrl =
            URL.createObjectURL(blob);

          this.contentUrl =
            this.sanitizer
              .bypassSecurityTrustResourceUrl(
                this.objectUrl
              );

          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando contenido:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudo cargar el contenido.';

        }

      });

  }

  onVideoLoaded(video: HTMLVideoElement): void {

    if (!this.progress) {
      return;
    }

    if (
      this.progress.progressPercent >= 100 ||
      this.progress.completedAt
    ) {
      video.currentTime = video.duration;
      return;
    }

    if (
      this.progress.lastPosition !== null &&
      this.progress.lastPosition > 0 &&
      this.progress.lastPosition < video.duration
    ) {
      video.currentTime = this.progress.lastPosition;
    }
  }

  onVideoTimeUpdate(video: HTMLVideoElement): void {

    if (
      this.progress &&
      (
        this.progress.progressPercent >= 100 ||
        this.progress.completedAt
      )
    ) {
      return;
    }

    if (!this.course || !this.material) {
      return;
    }

    if (!video.duration || !isFinite(video.duration)) {
      return;
    }

    const currentPosition = Math.floor(video.currentTime);

    const progressPercent = Math.min(
      99,
      Math.floor(
        (video.currentTime / video.duration) * 100
      )
    );

    const enoughTimePassed =
      Math.abs(
        currentPosition - this.lastSavedPosition
      ) >= this.progressIntervalSeconds;

    if (!enoughTimePassed) {
      return;
    }

    this.saveVideoProgress(
      progressPercent,
      currentPosition
    );
  }

  private saveVideoProgress(
    progressPercent: number,
    lastPosition: number
  ): void {

    if (!this.course || !this.material) {
      return;
    }

    this.lastSavedProgress = progressPercent;
    this.lastSavedPosition = lastPosition;

    this.materialService
      .updateProgress(
        this.course.id,
        this.material.id,
        {
          progressPercent,
          lastPosition
        }
      )
      .subscribe({

        next: progress => {
          this.progress = progress;
        },

        error: error => {
          console.error(
            'Error guardando progreso:',
            error
          );
        }

      });
  }

  private saveCurrentVideoProgress(): Observable<CourseMaterialProgress | null> {

    if (
      !this.currentVideo ||
      !this.course ||
      !this.material
    ) {
      return of(null);
    }

    const video = this.currentVideo;

    if (
      !video.duration ||
      !isFinite(video.duration)
    ) {
      return of(null);
    }

    const currentPosition =
      Math.floor(video.currentTime);

    const progressPercent =
      Math.min(
        100,
        Math.floor(
          (video.currentTime / video.duration) * 100
        )
      );

    return this.materialService
      .updateProgress(
        this.course.id,
        this.material.id,
        {
          progressPercent,
          lastPosition:
            progressPercent >= 100
              ? null
              : currentPosition
        }
      );
  }

  onVideoEnded(): void {

    if (!this.course || !this.material) {
      return;
    }

    this.materialService
      .updateProgress(
        this.course.id,
        this.material.id,
        {
          progressPercent: 100,
          lastPosition: null
        }
      )
      .subscribe({

        next: progress => {
          this.progress = progress;
        },

        error: error => {
          console.error(
            'Error marcando material como completado:',
            error
          );
        }

      });
  }

  openMaterial(
    material: CourseMaterial
  ): void {

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

  previous(): void {

    if (this.currentIndex <= 0) {
      return;
    }

    const material =
      this.materials[this.currentIndex - 1];

    this.saveCurrentVideoProgress()
      .subscribe({

        next: progress => {

          if (progress) {
            this.progress = progress;
          }

          this.openMaterial(material);
        },

        error: error => {

          console.error(
            'Error guardando progreso antes de retroceder:',
            error
          );

          this.openMaterial(material);
        }

      });
  }

  next(): void {

    if (
      this.currentIndex >=
      this.materials.length - 1
    ) {
      return;
    }

    const material =
      this.materials[this.currentIndex + 1];

    this.saveCurrentVideoProgress()
      .subscribe({

        next: progress => {

          if (progress) {
            this.progress = progress;
          }

          this.openMaterial(material);
        },

        error: error => {

          console.error(
            'Error guardando progreso antes de avanzar:',
            error
          );

          this.openMaterial(material);
        }

      });
  }

  backToCourse(): void {

    if (!this.course) {
      return;
    }

    const courseId = this.course.id;

    this.saveCurrentVideoProgress()
      .subscribe({

        next: progress => {

          if (progress) {
            this.progress = progress;
          }

          this.router.navigate([
            '/learning/courses',
            courseId
          ]);
        },

        error: error => {

          console.error(
            'Error guardando progreso antes de volver:',
            error
          );

          this.router.navigate([
            '/learning/courses',
            courseId
          ]);
        }

      });
  }

  ngOnDestroy(): void {

    this.revokeObjectUrl();

  }

  private revokeObjectUrl(): void {

    if (this.objectUrl) {

      URL.revokeObjectURL(
        this.objectUrl
      );

      this.objectUrl = null;

    }
  }

}