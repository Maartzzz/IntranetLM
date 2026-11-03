import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { EvaluationService } from '../../../core/services/evaluation.service';
import {
  EvaluationStartResponse,
  EvaluationQuestion,
  EvaluationSubmitRequest,
  EvaluationResultResponse,
  EvaluationResumeResponse
} from '../../../core/models/evaluation.models';
import { CertificateService } from '../../../core/services/certificate.service';

@Component({
  selector: 'app-learning-evaluation',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './learning-evaluations.component.html',
  styleUrl: './learning-evaluations.component.scss'
})
export class LearningEvaluationComponent implements OnInit {

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly evaluationService = inject(EvaluationService);
  private readonly certificateService = inject(CertificateService);

  courseId!: number;

  evaluation: EvaluationStartResponse | null = null;
  result: EvaluationResultResponse | null = null;

  currentQuestionIndex = 0;

  selectedAnswers: Record<number, number | null> = {};

  private leavingEvaluation = false;

  loading = true;
  submitting = false;
  errorMessage = '';

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const courseId = Number(params.get('courseId'));

      if (!courseId) {
        this.errorMessage = 'No se encontró el curso.';
        this.loading = false;
        return;
      }

      this.courseId = courseId;

      const attemptId = Number(
        this.route.snapshot.queryParamMap.get('attemptId')
      );

      if (attemptId) {
        this.resumeEvaluation(attemptId);
      } else {
        this.startEvaluation();
      }
    });
  }

  startEvaluation(): void {
    this.loading = true;
    this.errorMessage = '';

    this.evaluationService
      .startEvaluation(this.courseId)
      .subscribe({
        next: response => {
          this.evaluation = response;

          this.selectedAnswers = {};

          response.questions.forEach(question => {
            this.selectedAnswers[question.questionId] = null;
          });

          this.currentQuestionIndex = 0;
          this.loading = false;
        },
        error: error => {
          console.error('Error al iniciar evaluación:', error);

          this.errorMessage =
            error?.error?.message ??
            'No se pudo iniciar la evaluación.';

          this.loading = false;
        }
      });
  }

  resumeEvaluation(attemptId: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.evaluationService.resumeEvaluation(attemptId).subscribe({
      next: (response: EvaluationResumeResponse) => {
        this.evaluation = response;

        this.selectedAnswers = {};

        response.questions.forEach(question => {
          this.selectedAnswers[question.questionId] = null;
        });

        response.savedAnswers.forEach(answer => {
          this.selectedAnswers[answer.questionId] =
            answer.selectedOptionId;
        });

        this.currentQuestionIndex = 0;
        this.loading = false;
      },
      error: error => {
        console.error('Error al recuperar evaluación:', error);

        this.errorMessage =
          error?.error?.message ??
          'No se pudo recuperar la evaluación pendiente.';

        this.loading = false;
      }
    });
  }

  get currentQuestion(): EvaluationQuestion | null {
    if (!this.evaluation) {
      return null;
    }

    return this.evaluation.questions[this.currentQuestionIndex] ?? null;
  }

  get isFirstQuestion(): boolean {
    return this.currentQuestionIndex === 0;
  }

  get isLastQuestion(): boolean {
    if (!this.evaluation) {
      return false;
    }

    return this.currentQuestionIndex ===
      this.evaluation.questions.length - 1;
  }

  get currentAnswer(): number | null {
    if (!this.currentQuestion) {
      return null;
    }

    return this.selectedAnswers[
      this.currentQuestion.questionId
    ] ?? null;
  }

  selectOption(optionId: number): void {
    if (!this.currentQuestion || !this.evaluation) {
      return;
    }

    const questionId = this.currentQuestion.questionId;

    this.selectedAnswers[questionId] = optionId;

    this.evaluationService.saveAnswerProgress(
      this.evaluation.attemptId,
      {
        questionId,
        selectedOptionId: optionId
      }
    ).subscribe({
      error: error => {
        console.error('Error al guardar respuesta:', error);

        this.errorMessage =
          'No se pudo guardar la respuesta. Verifica tu conexión.';
      }
    });
  }

  previousQuestion(): void {
    if (!this.isFirstQuestion) {
      this.currentQuestionIndex--;
    }
  }

  nextQuestion(): void {
    if (!this.isLastQuestion) {
      this.currentQuestionIndex++;
    }
  }

  submitEvaluation(): void {
    if (!this.evaluation || this.submitting) {
      return;
    }

    const request: EvaluationSubmitRequest = {
      answers: this.evaluation.questions.map(question => ({
        questionId: question.questionId,
        selectedOptionId:
          this.selectedAnswers[question.questionId] ?? null
      }))
    };

    this.submitting = true;
    this.errorMessage = '';

    this.evaluationService
      .submitEvaluation(
        this.evaluation.attemptId,
        request
      )
      .subscribe({
        next: response => {
          this.result = response;
          this.submitting = false;
        },
        error: error => {
          console.error('Error al enviar evaluación:', error);

          this.errorMessage =
            error?.error?.message ??
            'No se pudo enviar la evaluación.';

          this.submitting = false;
        }
      });
  }

  downloadCertificate(): void {
    if (!this.result?.passed) {
      return;
    }

    this.certificateService
      .downloadCertificate(this.result.attemptId)
      .subscribe({
        next: blob => {
          const url = window.URL.createObjectURL(blob);

          const link = document.createElement('a');

          link.href = url;
          link.download = `certificado-${this.result!.attemptId}.pdf`;

          link.click();

          window.URL.revokeObjectURL(url);
        },
        error: error => {
          console.error(
            'Error descargando certificado:',
            error
          );

          this.errorMessage =
            'No se pudo descargar el certificado.';
        }
      });
  }

  backToCourse(): void {

    if (!this.evaluation || this.result) {
      this.router.navigate([
        '/learning/courses',
        this.courseId
      ]);

      return;
    }

    // Si no respondió ninguna pregunta,
    // eliminar el intento antes de salir.
    if (!this.hasAnsweredQuestions()) {

      this.evaluationService
        .cancelEvaluation(this.evaluation.attemptId)
        .subscribe({
          next: () => {
            this.router.navigate([
              '/learning/courses',
              this.courseId
            ]);
          },

          error: error => {
            console.error(
              'Error cancelando evaluación:',
              error
            );

            this.router.navigate([
              '/learning/courses',
              this.courseId
            ]);
          }
        });

      return;
    }

    // Si ya respondió al menos una pregunta,
    // conservar el intento y simplemente salir.
    this.router.navigate([
      '/learning/courses',
      this.courseId
    ]);
  }

  hasAnsweredQuestions(): boolean {

    return Object.values(this.selectedAnswers)
      .some(answer => answer !== null);
  }

}