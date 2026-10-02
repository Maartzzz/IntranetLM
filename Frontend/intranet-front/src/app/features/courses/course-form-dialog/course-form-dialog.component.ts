import {
  Component,
  inject,
  OnInit
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  Course,
  CourseRequest
} from '../../../core/models/courses.models';

import {
  COURSE_AUDIENCE_ROLES
} from '../../../core/models/course-audience.model'

import { CourseAudienceService } from '../../../core/services/course-audience.service';

interface CourseFormDialogResult {
  course: CourseRequest;
  audienceRoles: string[];
}

@Component({
  selector: 'app-course-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule
  ],
  templateUrl: './course-form-dialog.component.html',
  styleUrl: './course-form-dialog.component.scss'
})
export class CourseFormDialog implements OnInit {

  private readonly fb = inject(FormBuilder);

  private readonly dialogRef =
    inject(MatDialogRef<CourseFormDialog>);

  readonly data =
    inject(MAT_DIALOG_DATA) as Course | null;

  readonly isEditMode =
    this.data !== null;

  selectedAudienceRoles: string[] = [];
  audienceRoles =   COURSE_AUDIENCE_ROLES;

  private readonly audienceService =
    inject(CourseAudienceService)

      
  ngOnInit(): void {

    if (this.data)
    {

      this.form.patchValue({
        title: this.data.title,
        description: this.data.description ?? '',
        durationMinutes: this.data.durationMinutes,
        passingScore: this.data.passingScore,
        evaluationQuestionCount: this.data.evaluationQuestionCount,
        active: this.data.active
      });
      this.loadAudiences();
    }

  }

  form = this.fb.nonNullable.group({

    title: [
      this.data?.title ?? '',
      [
        Validators.required,
        Validators.maxLength(150)
      ]
    ],

    description: [
      this.data?.description ?? ''
    ],

    durationMinutes: [
      this.data?.durationMinutes ?? 60,
      [
        Validators.required,
        Validators.min(1)
      ]
    ],

    passingScore: [
      this.data?.passingScore ?? 70,
      [
        Validators.required,
        Validators.min(0),
        Validators.max(100)
      ]
    ],

    evaluationQuestionCount: [
    this.data?.evaluationQuestionCount ?? 10,
    [
      Validators.required,
      Validators.min(1)
    ]
  ],

    active: [
      this.data?.active ?? true
    ]

  });

  toggleAudienceRole(role: string): void {

    if (this.selectedAudienceRoles.includes(role)) {

      this.selectedAudienceRoles =
        this.selectedAudienceRoles.filter(
          item => item !== role
        );

      return;
    }

    this.selectedAudienceRoles = [
      ...this.selectedAudienceRoles,
      role
    ];
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

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      return;
    }

    const request: CourseRequest =
      this.form.getRawValue();

    const result = {
      course: request,
      audienceRoles: [...this.selectedAudienceRoles]
    };

    this.dialogRef.close(result);
  }

  private loadAudiences(): void {

  this.audienceService
    .getByCourse(this.data!.id)
    .subscribe({

      next: audiences => {

        this.selectedAudienceRoles =
          audiences.map(
            audience => audience.role
          );

      },

      error: error => {

        console.error(
          'Error cargando audiencia del curso:',
          error
        );

      }

    });
  }

}