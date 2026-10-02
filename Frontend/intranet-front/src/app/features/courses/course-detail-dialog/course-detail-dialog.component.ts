import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import {
  MatButtonModule
} from '@angular/material/button';

import { Course } from '../../../core/models/courses.models';
import { EvaluationAttempt } from '../../../core/models/evaluation.models';

import { EvaluationService } from '../../../core/services/evaluation.service';

import { CourseAudienceComponent } from '../course-audience/course-audience.component';
import { MaterialsComponent } from '../../materials/materials.component';
import { QuestionService } from '../../../core/services/question.service';
import { CourseQuestionsService } from '../../../core/services/course-question.service';
import { QuestionFormDialogComponent } from '../../questions/question-form-dialog/question-form-dialog.component';
import { QuestionImportDialogComponent } from '../../questions/question-import-dialog/question-import-dialog.component';

@Component({
  selector: 'app-course-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    CourseAudienceComponent,
    MaterialsComponent
  ],
  templateUrl: './course-detail-dialog.component.html',
  styleUrl: './course-detail-dialog.component.scss'
})
export class CourseDetailDialog implements OnInit {

  readonly data =
    inject(MAT_DIALOG_DATA) as Course;

  private readonly dialogRef =
    inject(MatDialogRef<CourseDetailDialog>);

  private readonly evaluationService =
    inject(EvaluationService);

  private readonly courseQuestionService =
    inject(CourseQuestionsService);

  questions: any[] = [];
  loadingQuestions = false;

  evaluationAttempts: EvaluationAttempt[] = [];

  loadingEvaluations = false;

  private readonly dialog =
    inject(MatDialog);

  ngOnInit(): void {
    this.loadEvaluationAttempts();
    this.loadQuestions();
  }

  loadEvaluationAttempts(): void {

    this.loadingEvaluations = true;

    this.evaluationService
      .getCompletedAttemptsByCourse(this.data.id)
      .subscribe({
        next: (attempts) => {
          this.evaluationAttempts = attempts;
          this.loadingEvaluations = false;
        },
        error: () => {
          this.evaluationAttempts = [];
          this.loadingEvaluations = false;
        }
      });
  }

  close(): void {
    this.dialogRef.close();
  }

  loadQuestions(): void {

    this.loadingQuestions = true;

    this.courseQuestionService
      .getQuestionsByCourse(this.data.id)
      .subscribe({

        next: questions => {

          this.questions = questions;

          this.loadingQuestions = false;

        },

        error: error => {

          console.error(
            'Error cargando preguntas del curso:',
            error
          );

          this.loadingQuestions = false;

        }

      });
  }

  addQuestion(): void {

      const dialogRef =
        this.dialog.open(
          QuestionFormDialogComponent,
          {
            width: '700px',
            maxWidth: '95vw',
            data: {}
          }
        );

      dialogRef.afterClosed().subscribe(
        question => {

          if (!question) {
            return;
          }

          this.courseQuestionService
            .addQuestionToCourse(
              this.data.id,
              question.id
            )
            .subscribe({

              next: () => {
                this.loadQuestions();
              },

              error: error => {
                console.error(
                  'Error asociando pregunta al curso:',
                  error
                );
              }

            });

        }
      );
    }

    importQuestions(): void {

    const dialogRef =
      this.dialog.open(
        QuestionImportDialogComponent,
        {
          width: '600px',
          maxWidth: '95vw',
          data: {
            courseId: this.data.id
          }
        }
      );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.loadQuestions();

      }
    );
  }
  
}