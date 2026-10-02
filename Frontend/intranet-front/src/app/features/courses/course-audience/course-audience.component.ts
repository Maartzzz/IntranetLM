import {
  Component,
  OnInit,
  inject,
  Input
} from '@angular/core';

import {
  CourseAudience,
  COURSE_AUDIENCE_ROLES
} from '../../../core/models/course-audience.model';

import {
  CourseAudienceService
} from '../../../core/services/course-audience.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-course-audience',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './course-audience.component.html',
  styleUrl: './course-audience.component.scss'
})
export class CourseAudienceComponent
  implements OnInit {

  @Input({ required: true })
  courseId!: number;

  private readonly audienceService =
    inject(CourseAudienceService);

  audiences: CourseAudience[] = [];

  availableRoles =
    COURSE_AUDIENCE_ROLES;

  selectedRole = '';

  loading = false;

  errorMessage = '';

  ngOnInit(): void {
    this.loadAudiences();
  }

  loadAudiences(): void {

    this.loading = true;
    this.errorMessage = '';

    this.audienceService
      .getByCourse(this.courseId)
      .subscribe({

        next: audiences => {

          this.audiences = audiences;
          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando audiencia:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudo cargar la audiencia.';
        }

      });
  }

  get availableRolesToAdd(): string[] {

    const assignedRoles =
      this.audiences.map(
        audience => audience.role
      );

    return this.availableRoles.filter(
      role => !assignedRoles.includes(role)
    );
  }

  addAudience(): void {

    if (!this.selectedRole) {
      return;
    }

    this.audienceService
      .add(
        this.courseId,
        {
          role: this.selectedRole
        }
      )
      .subscribe({

        next: audience => {

          this.audiences = [
            ...this.audiences,
            audience
          ];

          this.selectedRole = '';

        },

        error: error => {

          console.error(
            'Error agregando audiencia:',
            error
          );

          this.errorMessage =
            error.error?.message ??
            'No se pudo agregar la audiencia.';
        }

      });
  }

  removeAudience(
    audience: CourseAudience
  ): void {

    this.audienceService
      .remove(
        this.courseId,
        audience.id
      )
      .subscribe({

        next: () => {

          this.audiences =
            this.audiences.filter(
              item => item.id !== audience.id
            );

        },

        error: error => {

          console.error(
            'Error eliminando audiencia:',
            error
          );

          this.errorMessage =
            error.error?.message ??
            'No se pudo eliminar la audiencia.';
        }

      });
  }

  getRoleLabel(role: string): string {

    const labels: Record<string, string> = {

      ADMIN: 'Administración',

      SUPERVISOR: 'Supervisor',

      CHOFER: 'Chofer',

      OPERACIONES: 'Operaciones'

    };

    return labels[role] ?? role;
  }
}