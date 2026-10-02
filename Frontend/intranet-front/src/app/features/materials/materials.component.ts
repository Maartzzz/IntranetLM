import {
  Component,
  inject,
  Input,
  OnInit
} from '@angular/core';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  ActivatedRoute
} from '@angular/router';

import {
  CourseMaterial,
  CourseMaterialRequest
} from '../../core/models/course-materials.models';

import {
  CourseMaterialService
} from '../../core/services/course-material.service';
import { MatDialog } from '@angular/material/dialog';
import { MaterialFormDialog } from './material-form-dialog/material-form-dialog.component';
import { MaterialViewerDialog } from './material-viewer-dialog/material-viewer-dialog.component';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-materials',
  standalone: true,
  imports: [
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './materials.component.html',
  styleUrl: './materials.component.scss'
})
export class MaterialsComponent implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly materialService =
    inject(CourseMaterialService);

  materials: CourseMaterial[] = [];

  loading = false;

  errorMessage = '';

  private readonly dialog = inject(MatDialog)

  previewUrl: string | null = null;
  previewMaterial: CourseMaterial | null = null;

  @Input() courseId?: number;

  ngOnInit(): void {

    if (this.courseId === undefined) {

      this.courseId =
        Number(
          this.route.snapshot.paramMap.get('courseId')
        );

    }

    this.loadMaterials();
  }
  
  loadMaterials(): void {

    this.loading = true;
    this.errorMessage = '';

    if (this.courseId === undefined) {
      this.errorMessage =
        'No se pudo identificar el curso.';

      this.loading = false;

      return;
    }

    this.materialService
      .getByCourse(this.courseId)
      .subscribe({

        next: materials => {

          this.materials = materials;

          this.loading = false;
        },

        error: error => {

          console.error(
            'Error cargando materiales:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudieron cargar los materiales.';
        }

      });
  }

  getMaterialTypeLabel(
    type: string
  ): string {

    const labels: Record<string, string> = {

      VIDEO: 'Video',
      YOUTUBE: 'Youtube',
      PDF: 'PDF',
      PPTX: 'PowerPoint',
      DOCUMENT: 'Documento',
      URL: 'Enlace'

    };

    return labels[type] ?? type;
  }

  getMaterialIcon(
    type: string
  ): string {

    const icons: Record<string, string> = {

      VIDEO: 'play_circle',
      YOUTUBE: 'smart-display',
      PDF: 'picture_as_pdf',
      PPTX: 'slideshow',
      DOCUMENT: 'description',
      URL: 'link'

    };

    return icons[type] ?? 'description';
  }

  createMaterial(
    request: CourseMaterialRequest & {
      file?: File | null;
    }
  ): void {

    this.loading = true;
    this.errorMessage = '';

    // Si el material tiene archivo, primero lo subimos
    if (request.file) {

      this.materialService
        .upload(request.file)
        .subscribe({

          next: uploadResponse => {

            const materialRequest: CourseMaterialRequest = {

              title: request.title,

              description: request.description,

              type: request.type,

              url: uploadResponse.url,

              required: request.required,

              active: request.active

            };

            this.saveMaterial(
              materialRequest
            );

          },

          error: error => {

            console.error(
              'Error subiendo archivo:',
              error
            );

            this.loading = false;

            this.errorMessage =
              error.error?.message ??
              'No se pudo subir el archivo.';

          }

        });

      return;
    }

    // Material externo: YouTube / URL
    this.saveMaterial(request);
  }

  private saveMaterial(
    request: CourseMaterialRequest
  ): void {

      if (this.courseId === undefined) {
        this.errorMessage =
          'No se pudo identificar el curso.';

        this.loading = false;

        return;
      }

    this.materialService
      .create(
        this.courseId,
        request
      )
      .subscribe({

        next: () => {

          this.loading = false;

          this.loadMaterials();

        },

        error: error => {

          console.error(
            'Error creando material:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudo crear el material.';

        }

      });
  }

  openCreateDialog(): void {

    const dialogRef = this.dialog.open(
      MaterialFormDialog,
      {
        width: '550px',
        maxWidth: 'calc(100vw - 32px)',
        data: null
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.createMaterial(result);

      }
    );
  }

  editMaterial(material: CourseMaterial): void {

    const dialogRef = this.dialog.open(
      MaterialFormDialog,
      {
        width: '550px',
        maxWidth: 'calc(100vw - 32px)',
        data: material
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.updateMaterial(
          material.id,
          result
        );

      }
    );
  }

  deleteMaterial(material: CourseMaterial): void {

    if (this.courseId === undefined) {
      this.errorMessage =
        'No se pudo identificar el curso.';
      return;
    }

    const confirmed = window.confirm(
      `¿Deseas eliminar el material "${material.title}"?`
    );

    if (!confirmed) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.materialService
      .delete(
        this.courseId,
        material.id
      )
      .subscribe({

        next: () => {
          this.loading = false;
          this.loadMaterials();
        },

        error: error => {

          console.error(
            'Error eliminando material:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudo eliminar el material.';
        }

      });
  }

  updateMaterial(
    materialId: number,
    request: CourseMaterialRequest & {
      file?: File | null;
    }
  ): void {

    this.loading = true;
    this.errorMessage = '';

    // Si se seleccionó un archivo nuevo,
    // primero lo subimos.
    if (request.file) {

      this.materialService
        .upload(request.file)
        .subscribe({

          next: uploadResponse => {

            const materialRequest: CourseMaterialRequest = {

              title: request.title,

              description: request.description,

              type: request.type,

              url: uploadResponse.url,

              required: request.required,

              active: request.active

            };

            this.saveUpdatedMaterial(
              materialId,
              materialRequest
            );

          },

          error: error => {

            console.error(
              'Error subiendo archivo:',
              error
            );

            this.loading = false;

            this.errorMessage =
              error.error?.message ??
              'No se pudo subir el archivo.';

          }

        });

      return;
    }

    // Si no se seleccionó archivo,
    // conservamos la URL que ya tenía.
    const materialRequest: CourseMaterialRequest = {

      title: request.title,

      description: request.description,

      type: request.type,

      url: request.url,

      required: request.required,

      active: request.active

    };

    this.saveUpdatedMaterial(
      materialId,
      materialRequest
    );
  }

  viewMaterial(material: CourseMaterial): void {

    this.dialog.open(MaterialViewerDialog, {
      width: '800px',
      maxWidth: 'calc(100vw - 32px)',
      maxHeight: '90vh',
      data: material
    });
  }

  private saveUpdatedMaterial(
    materialId: number,
    request: CourseMaterialRequest
  ): void {

      if (this.courseId === undefined) {
        this.errorMessage =
          'No se pudo identificar el curso.';

        this.loading = false;

        return;
      }

    this.materialService
      .update(
        this.courseId,
        materialId,
        request
      )
      .subscribe({

        next: () => {

          this.loading = false;

          this.loadMaterials();

        },

        error: error => {

          console.error(
            'Error actualizando material:',
            error
          );

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No se pudo actualizar el material.';

        }

      });

  }

  uploadFile(
    file: File
  ): Observable<{ filename: string; url: string }> {

    return this.materialService.upload(file);
  }

  openMaterial(
    material: CourseMaterial
  ): void {

    if (
      material.type === 'YOUTUBE' ||
      material.type === 'URL'
    ) {

      window.open(
        material.url,
        '_blank'
      );

      return;
    }

    this.materialService
      .getContent(material.id)
      .subscribe({

        next: blob => {

          if (this.previewUrl) {
            URL.revokeObjectURL(
              this.previewUrl
            );
          }

          this.previewUrl =
            URL.createObjectURL(blob);

          this.previewMaterial =
            material;

        },

        error: error => {

          console.error(
            'Error obteniendo material:',
            error
          );

          this.errorMessage =
            'No se pudo abrir el material.';

        }

      });
  }
}