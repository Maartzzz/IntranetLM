import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { CourseService } from '../../core/services/courses.service';
import { Course } from '../../core/models/courses.models';
import { Router } from '@angular/router';

@Component({
  selector: 'app-learning',
  standalone: true,
  imports: [
    CommonModule
  ],
  templateUrl: './learning.component.html',
  styleUrl: './learning.component.scss'
})
export class LearningComponent implements OnInit {

  private readonly courseService =
    inject(CourseService);

  courses: Course[] = [];

  loading = false;

  errorMessage = '';

  private readonly router = inject(Router);

  ngOnInit(): void {

    this.loadCourses();

  }

  private loadCourses(): void {

    this.loading = true;
    this.errorMessage = '';

    this.courseService
      .getAssignedCourses()
      .subscribe({

        next: courses => {

          this.courses = courses;

          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando cursos asignados:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudieron cargar los cursos.';

        }

      });

  }

  openCourse(courseId: number): void {

    this.router.navigate([
      '/learning/courses',
      courseId
    ]);

  }

}