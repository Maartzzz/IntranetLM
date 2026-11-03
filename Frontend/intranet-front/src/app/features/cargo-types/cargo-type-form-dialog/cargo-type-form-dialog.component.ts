import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { CargoType, CargoTypeRequest } from '../../../core/models/cargo-types.models';
import { CargoTypeService } from '../../../core/services/cargo-types.service';

export interface CargoTypeFormDialogData {
  mode: 'create' | 'edit';
  cargoType?: CargoType;
}

@Component({
  selector: 'app-cargo-type-form-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './cargo-type-form-dialog.component.html',
  styleUrl: './cargo-type-form-dialog.component.scss'
})
export class CargoTypeFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(CargoTypeService);
  private readonly data = inject<CargoTypeFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<CargoTypeFormDialogComponent>);

  readonly isEditMode = this.data.mode === 'edit';
  isSaving = false;
  errorMessage = '';
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    isContainer: [false, Validators.required]
  });

  constructor() {
    if (this.data.cargoType) {
      this.form.patchValue({ name: this.data.cargoType.name, isContainer: this.data.cargoType.container });
    }
  }

  save(): void {
    if (this.form.invalid || this.isSaving) { this.form.markAllAsTouched(); return; }
    this.isSaving = true;
    this.errorMessage = '';
    const value = this.form.getRawValue();
    const request: CargoTypeRequest = { name: value.name.trim(), isContainer: value.isContainer };
    const operation = this.isEditMode && this.data.cargoType
      ? this.service.updateCargoType(this.data.cargoType.id, request)
      : this.service.createCargoType(request);

    operation.subscribe({
      next: item => this.dialogRef.close(item),
      error: error => {
        this.isSaving = false;
        this.errorMessage = error.error?.message || error.error?.detail
          || 'No se pudo guardar el tipo de carga. Verifica que el nombre no esté duplicado.';
      }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
