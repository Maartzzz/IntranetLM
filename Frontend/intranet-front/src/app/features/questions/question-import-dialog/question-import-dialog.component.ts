import {
  Component,
  Inject,
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
  MatButtonModule
} from '@angular/material/button';

import {
  QuestionImportService
} from '../../../core/services/question-import.service';

interface QuestionImportDialogData {
  courseId: number;
}

@Component({
  selector: 'app-question-import-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule
  ],
  templateUrl: './question-import-dialog.component.html',
  styleUrl: './question-import-dialog.component.scss'
})
export class QuestionImportDialogComponent {

  private readonly questionImportService =
    inject(QuestionImportService);

  readonly dialogRef =
    inject(MatDialogRef<QuestionImportDialogComponent>);

  file: File | null = null;

  importing = false;

  errorMessage = '';

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: QuestionImportDialogData
  ) {
  }

  onFileSelected(
    event: Event
  ): void {

    this.errorMessage = '';

    const input =
      event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      this.file = null;
      return;
    }

    const selectedFile =
      input.files[0];

    if (
      !selectedFile.name
        .toLowerCase()
        .endsWith('.xlsx')
    ) {

      this.errorMessage =
        'Selecciona un archivo Excel con extensión .xlsx.';

      this.file = null;

      input.value = '';

      return;
    }

    this.file = selectedFile;
  }

  import(): void {

    if (!this.file || this.importing) {
      return;
    }

    this.importing = true;

    this.errorMessage = '';

    this.questionImportService
      .importQuestions(
        this.data.courseId,
        this.file
      )
      .subscribe({

        next: response => {

          this.importing = false;

          this.dialogRef.close(
            response
          );
        },

        error: error => {

          console.error(
            'Error importando preguntas:',
            error
          );

          this.importing = false;

          this.errorMessage =
            error?.error ??
            'No se pudieron importar las preguntas.';
        }

      });
  }

  cancel(): void {

    if (!this.importing) {
      this.dialogRef.close();
    }
  }

  downloadTemplate(): void {

    this.questionImportService
      .downloadTemplate(this.data.courseId)
      .subscribe({
        next: blob => {

          const url = window.URL.createObjectURL(blob);

          const link = document.createElement('a');

          link.href = url;
          link.download = 'plantilla_preguntas.xlsx';

          link.click();

          window.URL.revokeObjectURL(url);
        },

        error: error => {

          console.error(
            'Error descargando plantilla:',
            error
          );

          this.errorMessage =
            'No se pudo descargar la plantilla.';
        }
      });
  }
}