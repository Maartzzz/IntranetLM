import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { OperationRequirement, OperationRequirementRequest, RequirementCatalog } from '../../../../core/models/operations.models';
import { OperationsService } from '../../../../core/services/operations.service';

export interface RequirementFormDialogData {
  mode: 'create' | 'edit'; operationId: number; requirement?: OperationRequirement; catalog: RequirementCatalog[];
}

@Component({
  selector: 'app-requirement-form-dialog', standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatRadioModule],
  templateUrl: './requirement-form-dialog.component.html', styleUrl: './requirement-form-dialog.component.scss'
})
export class RequirementFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(OperationsService);
  readonly data = inject<RequirementFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<RequirementFormDialogComponent>);

  readonly isEditMode = this.data.mode === 'edit';
  readonly catalog = this.data.catalog;
  source: 'catalog' | 'custom' = this.data.requirement?.requirementCatalogId ? 'catalog' : (this.isEditMode ? 'custom' : 'catalog');
  isSaving = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    requirementCatalogId: [0],
    customName: ['', Validators.maxLength(120)],
    externalResponsible: ['', Validators.maxLength(150)],
    comment: ['', Validators.maxLength(10000)]
  });

  constructor() {
    if (this.data.requirement) {
      this.form.patchValue({
        requirementCatalogId: this.data.requirement.requirementCatalogId ?? 0,
        customName: this.data.requirement.requirementCatalogId ? '' : this.data.requirement.requirementName,
        externalResponsible: this.data.requirement.externalResponsible ?? '',
        comment: this.data.requirement.comment ?? ''
      });
    }
  }

  save(): void {
    if (this.isSaving) return;
    const value = this.form.getRawValue();
    if (this.source === 'catalog' && (!value.requirementCatalogId || value.requirementCatalogId <= 0)) {
      this.errorMessage = 'Selecciona un requisito del catálogo.'; return;
    }
    if (this.source === 'custom' && !value.customName.trim()) {
      this.errorMessage = 'Escribe el nombre del requisito personalizado.'; return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const request: OperationRequirementRequest = {
      requirementCatalogId: this.source === 'catalog' ? value.requirementCatalogId : null,
      customName: this.source === 'custom' ? value.customName.trim() : null,
      externalResponsible: value.externalResponsible.trim() || null,
      statusId: this.data.requirement?.statusId ?? null,
      comment: value.comment.trim() || null
    };
    const call = this.data.requirement
      ? this.service.updateRequirement(this.data.operationId, this.data.requirement.id, request)
      : this.service.createRequirement(this.data.operationId, request);
    call.subscribe({
      next: result => this.dialogRef.close(result),
      error: error => { console.error(error); this.isSaving = false; this.errorMessage = error.error?.message || error.error?.detail || 'No se pudo guardar el requisito.'; }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
