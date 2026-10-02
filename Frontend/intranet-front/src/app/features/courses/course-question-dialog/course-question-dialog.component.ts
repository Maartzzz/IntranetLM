import {
  Component,
  Inject,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import {
  Question
} from '../../../core/models/question.models';

import {
  QuestionService
} from '../../../core/services/question.service';

import {
  CourseQuestionsService
} from '../../../core/services/course-question.service';

interface CourseQuestionsDialogData {
  courseId: number;
  courseTitle: string;
}

@Component({
  selector: 'app-course-questions-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule
  ],
  templateUrl: './course-question-dialog.component.html',
  styleUrl: './course-question-dialog.component.scss'
})
export class CourseQuestionsDialogComponent
  implements OnInit {

  private readonly questionService =
    inject(QuestionService);

  private readonly courseQuestionsService =
    inject(CourseQuestionsService);

  readonly dialogRef =
    inject(
      MatDialogRef<CourseQuestionsDialogComponent>
    );

  questions: Question[] = [];

  assignedQuestions: Question[] = [];

  loading = false;

  savingQuestionId: number | null = null;

  errorMessage = '';

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: CourseQuestionsDialogData
  ) {}

  ngOnInit(): void {
    this.loadQuestions();
  }

  loadQuestions(): void {

    this.loading = true;
    this.errorMessage = '';

    this.questionService
      .getQuestions()
      .subscribe({

        next: questions => {

          this.questions = questions.filter(
            question => question.active
          );

          this.loadAssignedQuestions();

        },

        error: error => {

          console.error(
            'Error cargando preguntas:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudieron cargar las preguntas.';
        }

      });
  }

  loadAssignedQuestions(): void {

    this.courseQuestionsService
      .getQuestionsByCourse(
        this.data.courseId
      )
      .subscribe({

        next: questions => {

          this.assignedQuestions = questions;

          this.loading = false;

        },

        error: error => {

          console.error(
            'Error cargando preguntas asignadas:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudieron cargar las preguntas asignadas.';
        }

      });
  }

  isAssigned(
    questionId: number
  ): boolean {

    return this.assignedQuestions.some(
      question => question.id === questionId
    );
  }

  addQuestion(
    question: Question
  ): void {

    if (
      this.isAssigned(question.id) ||
      this.savingQuestionId !== null
    ) {
      return;
    }

    this.savingQuestionId =
      question.id;

    this.courseQuestionsService
      .addQuestionToCourse(
        this.data.courseId,
        question.id
      )
      .subscribe({

        next: () => {

          this.assignedQuestions = [
            ...this.assignedQuestions,
            question
          ];

          this.savingQuestionId = null;

        },

        error: error => {

          console.error(
            'Error asociando pregunta:',
            error
          );

          this.savingQuestionId = null;

          this.errorMessage =
            error?.error?.message ??
            'No se pudo asociar la pregunta.';
        }

      });
  }

  removeQuestion(
    question: Question
  ): void {

    if (
      !this.isAssigned(question.id) ||
      this.savingQuestionId !== null
    ) {
      return;
    }

    this.savingQuestionId =
      question.id;

    this.courseQuestionsService
      .removeQuestionFromCourse(
        this.data.courseId,
        question.id
      )
      .subscribe({

        next: () => {

          this.assignedQuestions =
            this.assignedQuestions.filter(
              item => item.id !== question.id
            );

          this.savingQuestionId = null;

        },

        error: error => {

          console.error(
            'Error quitando pregunta:',
            error
          );

          this.savingQuestionId = null;

          this.errorMessage =
            error?.error?.message ??
            'No se pudo quitar la pregunta.';
        }

      });
  }

  close(): void {
    this.dialogRef.close();
  }
  
}