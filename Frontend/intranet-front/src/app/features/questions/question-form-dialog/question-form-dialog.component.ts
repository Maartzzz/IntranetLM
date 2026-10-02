import {
  Component,
  Inject,
  inject
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  FormArray,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogRef,
  MatDialogModule
} from '@angular/material/dialog';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

import {
  MatInputModule
} from '@angular/material/input';

import {
  MatCheckboxModule
} from '@angular/material/checkbox';

import {
  Question,
  QuestionRequest
} from '../../../core/models/question.models';

import {
  QuestionService
} from '../../../core/services/question.service';

interface QuestionDialogData {
  question?: Question;
}

@Component({
  selector: 'app-question-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule
  ],
  templateUrl: './question-form-dialog.component.html',
  styleUrl: './question-form-dialog.component.scss'
})
export class QuestionFormDialogComponent {

  private readonly fb = inject(FormBuilder);
  private readonly questionService = inject(QuestionService);

  readonly dialogRef = inject(
    MatDialogRef<QuestionFormDialogComponent>
  );

  isEditing = false;
  saving = false;
  errorMessage = '';

  form = this.fb.group({
    questionText: [
      '',
      [
        Validators.required,
        Validators.maxLength(1000)
      ]
    ],

    active: [
      true
    ],

    options: this.fb.array([])
  });

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: QuestionDialogData
  ) {
    if (data.question) {
      this.isEditing = true;
      this.loadQuestion(data.question);
    } else {
      this.addOption();
      this.addOption();
    }
  }

  get options(): FormArray {
    return this.form.get('options') as FormArray;
  }

  createOption(
    optionText = '',
    correct = false
  ): FormGroup {
    return this.fb.group({
      optionText: [
        optionText,
        [
          Validators.required,
          Validators.maxLength(500)
        ]
      ],
      correct: [
        correct
      ]
    });
  }

  addOption(): void {
    this.options.push(
      this.createOption()
    );
  }

  removeOption(index: number): void {
    if (this.options.length <= 2) {
      return;
    }

    this.options.removeAt(index);
  }

  selectCorrectOption(index: number): void {
    this.options.controls.forEach(
      (control, currentIndex) => {
        control.get('correct')?.setValue(
          currentIndex === index
        );
      }
    );
  }

  loadQuestion(question: Question): void {
    this.form.patchValue({
      questionText: question.questionText,
      active: question.active
    });

    this.options.clear();

    question.options.forEach(option => {
      this.options.push(
        this.createOption(
          option.optionText,
          option.correct
        )
      );
    });
  }

  hasCorrectOption(): boolean {
    return this.options.controls.some(
      control =>
        control.get('correct')?.value === true
    );
  }

  save(): void {
    this.errorMessage = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    if (this.options.length < 2) {
      this.errorMessage =
        'La pregunta debe tener al menos dos opciones.';
      return;
    }

    if (!this.hasCorrectOption()) {
      this.errorMessage =
        'Debes seleccionar una respuesta correcta.';
      return;
    }

    const request: QuestionRequest = {
      questionText:
        this.form.get('questionText')?.value?.trim() ?? '',

      active:
        this.form.get('active')?.value ?? true,

      options: this.options.controls.map(
        control => ({
          optionText:
            control.get('optionText')?.value?.trim() ?? '',

          correct:
            control.get('correct')?.value ?? false
        })
      )
    };

    this.saving = true;

    const request$ = this.isEditing
      ? this.questionService.updateQuestion(
          this.data.question!.id,
          request
        )
      : this.questionService.createQuestion(
          request
        );

    request$.subscribe({
      next: response => {
        this.saving = false;
        this.dialogRef.close(response);
      },

      error: error => {
        console.error(
          'Error guardando pregunta:',
          error
        );

        this.saving = false;

        this.errorMessage =
          error?.error?.message ??
          'No se pudo guardar la pregunta.';
      }
    });
  }

  cancel(): void {
    if (!this.saving) {
      this.dialogRef.close();
    }
  }
}