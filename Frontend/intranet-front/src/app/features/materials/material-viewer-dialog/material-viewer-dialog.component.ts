import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  CourseMaterial
} from '../../../core/models/course-materials.models';

import {
  CourseMaterialService
} from '../../../core/services/course-material.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-material-view-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './material-viewer-dialog.component.html',
  styleUrl: './material-viewer-dialog.component.scss'
})
export class MaterialViewerDialog
  implements OnInit, OnDestroy {

  private readonly materialService =
    inject(CourseMaterialService);

  private readonly dialogRef =
    inject(MatDialogRef<MaterialViewerDialog>);

  readonly material =
    inject(MAT_DIALOG_DATA) as CourseMaterial;

  contentUrl: SafeResourceUrl | null = null;

  private objectUrl: string | null = null;

  private readonly sanitizer =
  inject(DomSanitizer);

  loading = false;

  errorMessage = '';

  ngOnInit(): void {

    if (
      this.material.type === 'VIDEO' ||
      this.material.type === 'PDF' ||
      this.material.type === 'PPTX' ||
      this.material.type === 'DOCUMENT'
    ) {

      this.loadContent();

    }

  }

  private loadContent(): void {

    this.loading = true;
    this.errorMessage = '';

    this.materialService
      .getContent(this.material.id)
      .subscribe({

    next: blob => {

      this.objectUrl =
        URL.createObjectURL(blob);

      console.log(
        'Object URL:',
        this.objectUrl
      );

      this.contentUrl =
        this.sanitizer
          .bypassSecurityTrustResourceUrl(
            this.objectUrl
          );

      this.loading = false;

    },

        error: error => {

          console.error(
            'Error cargando material:',
            error
          );

          this.loading = false;

          this.errorMessage =
            'No se pudo cargar el material.';

        }

      });

  }

  close(): void {

    this.dialogRef.close();

  }

  ngOnDestroy(): void {

      if (this.objectUrl) {

        URL.revokeObjectURL(
          this.objectUrl
        );

      }

  }

}