import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import {
  MatDialog,
  MatDialogModule
} from '@angular/material/dialog';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  Course,
  CourseRequest
} from '../../core/models/courses.models';

import {
  CourseService
} from '../../core/services/courses.service';
import { CourseFormDialog } from './course-form-dialog/course-form-dialog.component';
import { CourseDetailDialog } from './course-detail-dialog/course-detail-dialog.component';
import { CourseAudienceService } from '../../core/services/course-audience.service';
import { Router } from '@angular/router';
import { CourseQuestionsDialogComponent } from './course-question-dialog/course-question-dialog.component';

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './courses.component.html',
  styleUrl: './courses.component.scss'
})
export class CoursesComponent implements OnInit {

  private readonly courseService =
    inject(CourseService);

  private readonly courseAudienceService =
    inject(CourseAudienceService);

  private readonly dialog =
    inject(MatDialog);

  private readonly router = inject(Router)

  courses: Course[] = [];

  searchTerm = '';

  statusFilter:
    'ALL' | 'ACTIVE' | 'INACTIVE' = 'ACTIVE';

  loading = false;

  errorMessage = '';

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {

    this.loading = true;
    this.errorMessage = '';

    this.courseService
      .getCourses()
      .subscribe({

        next: courses => {

          this.courses = courses;
          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando cursos:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudieron cargar los cursos.';
        }

      });
  }

  get filteredCourses(): Course[] {

    const search =
      this.searchTerm
        .trim()
        .toLowerCase();

    return this.courses.filter(course => {

      const matchesSearch =
        !search ||
        course.title
          .toLowerCase()
          .includes(search) ||
        (
          course.description ?? ''
        )
          .toLowerCase()
          .includes(search);

      const matchesStatus =
        this.statusFilter === 'ALL' ||
        (
          this.statusFilter === 'ACTIVE' &&
          course.active
        ) ||
        (
          this.statusFilter === 'INACTIVE' &&
          !course.active
        );

      return (
        matchesSearch &&
        matchesStatus
      );
    });
  }

  createCourse(result: {
    course: CourseRequest;
    audienceRoles: string[];
  }): void {

    this.loading = true;
    this.errorMessage = '';

    this.courseService
      .createCourse(result.course)
      .subscribe({

        next: createdCourse => {

          this.saveAudiences(
            createdCourse.id,
            result.audienceRoles
          );

        },

        error: error => {

          console.error(
            'Error creando curso:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudo crear el curso.';
        }

      });
  }

  private saveAudiences(
    courseId: number,
    roles: string[]
  ): void {

    if (roles.length === 0) {

      this.loading = false;
      this.loadCourses();

      return;
    }

    let completed = 0;

    roles.forEach(role => {

      this.courseAudienceService
        .add(courseId, { role })
        .subscribe({

          next: () => {

            completed++;

            if (completed === roles.length) {

              this.loading = false;
              this.loadCourses();

            }

          },

          error: error => {

            console.error(
              'Error guardando audiencia del curso:',
              error
            );

            this.loading = false;

            this.errorMessage =
              error.error?.message ??
              'El curso fue creado, pero no se pudieron guardar todas las audiencias.';

          }

        });

    });
  }

  openCreateDialog(): void {

    const dialogRef = this.dialog.open(
      CourseFormDialog,
      {
        width: '550px',
        maxWidth: 'calc(100vw - 32px)',
        data: null
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.createCourse(result);
      }
    );
  }

  updateCourse(
    id: number,
    request: CourseRequest
  ): void {

    this.loading = true;
    this.errorMessage = '';

    this.courseService
      .updateCourse(id, request)
      .subscribe({

        next: updatedCourse => {

          this.loading = false;
          this.loadCourses();

        },

        error: error => {

          console.error(
            'Error actualizando curso:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudo actualizar el curso.';
        }

      });
  }

  openEditDialog(course: Course): void {

    const dialogRef = this.dialog.open(
      CourseFormDialog,
      {
        width: '550px',
        maxWidth: 'calc(100vw - 32px)',
        data: course
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.updateCourse(
          course.id,
          result.course
        );

        this.updateAudiences(
          course.id,
          result.audienceRoles
        );
      }
    );
  }

  private updateAudiences(
      courseId: number,
      selectedRoles: string[]
    ): void {

      this.courseAudienceService
        .getByCourse(courseId)
        .subscribe({

          next: currentAudiences => {

            const currentRoles =
              currentAudiences
                .map(audience => audience.role);

            const rolesToAdd =
              selectedRoles.filter(
                role => !currentRoles.includes(role)
              );

            const audiencesToRemove =
              currentAudiences.filter(
                audience =>
                  !selectedRoles.includes(audience.role)
              );

            rolesToAdd.forEach(role => {

              this.courseAudienceService
                .add(courseId, {
                  role: role
                })
                .subscribe({

                  error: error => {

                    console.error(
                      'Error agregando audiencia:',
                      error
                    );

                  }

                });

            });

            audiencesToRemove.forEach(audience => {

              this.courseAudienceService
                .remove(
                  courseId,
                  audience.id
                )
                .subscribe({

                  error: error => {

                    console.error(
                      'Error eliminando audiencia:',
                      error
                    );

                  }

                });

            });

          },

          error: error => {

            console.error(
              'Error obteniendo audiencias actuales:',
              error
            );

          }

        });
    }

  toggleActive(course: Course): void {

    this.courseService
      .toggleActive(course.id)
      .subscribe({

        next: updatedCourse => {

          const index = this.courses.findIndex(
            item => item.id === updatedCourse.id
          );

          if (index !== -1) {

            this.courses[index] = updatedCourse;
            this.courses = [...this.courses];
          }
        },

        error: error => {

          console.error(
            'Error cambiando estado del curso:',
            error
          );

          this.errorMessage =
            error.error?.message ??
            'No se pudo cambiar el estado del curso.';
        }

      });
  }

  openDetailDialog(course: Course){

    this.dialog.open(
      CourseDetailDialog,{
        width: '600px',
        maxWidth: 'calc(100vw - 32px)',
        data: course
      }
    );
  }

  openMaterials(courseId: number): void {

    this.router.navigate([
      '/courses',
      courseId,
      'materials'
    ]);
  }

  openQuestions(course: Course): void {
    this.dialog.open(
      CourseQuestionsDialogComponent,
      {
        width: '700px',
        maxWidth: 'calc(100vw - 32px)',
        data: {
            courseId: course.id,
            courseTitle: course.title
        }
      }
    )
  }
}