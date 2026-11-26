import {
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  Question
} from '../../core/models/question.models';

import {
  QuestionService
} from '../../core/services/question.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { QuestionFormDialogComponent } from './question-form-dialog/question-form-dialog.component';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-questions',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule
  ],
  templateUrl: './questions.component.html',
  styleUrl: './questions.component.scss'
})
export class QuestionsComponent implements OnInit {

  private readonly questionService = inject(QuestionService);
  private readonly authService = inject(AuthService);

  questions: Question[] = [];

  loading = false;
  errorMessage = '';
  
  private readonly dialog = inject(MatDialog);

  get canCreate(): boolean {
    return this.authService.hasPermission('TRAINING.QUESTIONS.CREATE');
  }

  get canEdit(): boolean {
    return this.authService.hasPermission('TRAINING.QUESTIONS.EDIT');
  }

  get canDelete(): boolean {
    return this.authService.hasPermission('TRAINING.QUESTIONS.DELETE');
  }

  ngOnInit(): void {
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.questionService.getQuestions().subscribe({
      next: response => {
        this.questions = response;
        this.loading = false;
      },
      error: error => {
        console.error('Error cargando preguntas:', error);

        this.errorMessage =
          'No se pudieron cargar las preguntas.';

        this.loading = false;
      }
    });
  }

  newQuestion(): void {
    if (!this.canCreate) return;

    const dialogRef = this.dialog.open(
      QuestionFormDialogComponent,
      {
        width: '650px',
        maxWidth: '95vw',
        data: {}
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {
        if (result) {
          this.loadQuestions();
        }
      }
    );
  }

  editQuestion(question: Question): void {
    if (!this.canEdit) return;

    const dialogRef = this.dialog.open(
      QuestionFormDialogComponent,
      {
        width: '650px',
        maxWidth: '95vw',
        data: {
          question
        }
      }
    );

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadQuestions();
      }
    });
  }

  toggleActive(question: Question): void {
    if (!this.canEdit) return;

    this.questionService
      .toggleActive(question.id)
      .subscribe({
        next: () => {
          this.loadQuestions();
        },
        error: error => {
          console.error(
            'Error cambiando estado de la pregunta:',
            error
          );

          this.errorMessage =
            'No se pudo cambiar el estado de la pregunta.';
        }
      });
  }

  deleteQuestion(question: Question): void {
    if (!this.canDelete) return;

    const confirmed = window.confirm(
      `¿Deseas eliminar la pregunta "${question.questionText}"?`
    );

    if (!confirmed) {
      return;
    }

    this.questionService
      .deleteQuestion(question.id)
      .subscribe({
        next: () => {
          this.loadQuestions();
        },
        error: error => {
          console.error(
            'Error eliminando pregunta:',
            error
          );

          this.errorMessage =
            error?.error?.message ??
            'No se pudo eliminar la pregunta.';
        }
      });
  }
}