import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { CargoType } from '../../../../core/models/cargo-types.models';
import { ClientServiceOrderRequest } from '../../../../core/models/client-service-orders.models';
import { CargoTypeService } from '../../../../core/services/cargo-types.service';
import { OperationsService } from '../../../../core/services/operations.service';

export interface ClientServiceOrderDialogData {
  operationId: number;
  clientId: number;
  clientName: string;
  serviceType: 'FCL' | 'LCL';
  reference: string | null;
  origin: string | null;
  destination: string | null;
  estimatedDate: string | null;
  customerOrderNumber: string | null;
  quantity: number | null;
  cargoTypeId: number;
  observation: string | null;
}

@Component({
  selector: 'app-client-service-order-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './client-service-order-dialog.component.html',
  styleUrl: './client-service-order-dialog.component.scss'
})
export class ClientServiceOrderDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly operations = inject(OperationsService);
  private readonly cargoTypesService = inject(CargoTypeService);
  readonly data = inject<ClientServiceOrderDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ClientServiceOrderDialogComponent>);
  readonly serviceType = this.data.serviceType;
  cargoTypes: CargoType[] = [];
  saving = false;
  loading = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    clientOrderNumber: [this.data.customerOrderNumber ?? '', [Validators.required, Validators.maxLength(80)]],
    reference: [this.data.reference ?? '', Validators.maxLength(120)],
    receivedDate: [''],
    estimatedArrivalDate: [this.data.estimatedDate ?? ''],
    origin: [this.data.origin ?? '', Validators.maxLength(200)],
    destination: [this.data.destination ?? '', Validators.maxLength(200)],
    observation: [this.data.observation ?? ''],
    
    totalPackages: [this.serviceType === 'LCL' ? (this.data.quantity ?? 0) : 0, [Validators.required, Validators.min(0)]],
    fclQuantities: this.fb.array([])
  });

  constructor() {
    // Reafirma la precarga desde la operación; los controles permanecen editables.
    this.form.patchValue({
      clientOrderNumber: this.data.customerOrderNumber ?? '',
      reference: this.data.reference ?? '',
      estimatedArrivalDate: this.data.estimatedDate ?? '',
      origin: this.data.origin ?? '',
      destination: this.data.destination ?? '',
      observation: this.data.observation ?? '',
      totalPackages: this.serviceType === 'LCL' ? (this.data.quantity ?? 0) : 0
    });

    if (this.serviceType === 'FCL') {
      this.cargoTypesService.getActiveCargoTypes().subscribe({
        next: values => {
          this.cargoTypes = values.filter(item => item.container && item.active);
          const operationCargoTypeId = Number(this.data.cargoTypeId);
          const initialCargoType = this.cargoTypes.find(item => Number(item.id) === operationCargoTypeId);

          this.fclQuantities.clear();
          this.fclQuantities.push(this.fb.nonNullable.group({
            cargoTypeId: [initialCargoType ? Number(initialCargoType.id) : operationCargoTypeId, Validators.required],
            totalQuantity: [
              this.data.quantity !== null && this.data.quantity > 0 ? Number(this.data.quantity) : 1,
              [Validators.required, Validators.min(1)]
            ]
          }));
        },
        error: error => {
          console.error(error);
          this.errorMessage = 'No se pudieron cargar los tipos de contenedor.';
        }
      });
    }
  }

  get fclQuantities(): FormArray {
    return this.form.controls.fclQuantities;
  }

  addFclQuantity(): void {
    this.fclQuantities.push(this.fb.nonNullable.group({
      cargoTypeId: [0, Validators.required],
      totalQuantity: [1, [Validators.required, Validators.min(1)]]
    }));
  }

  removeFclQuantity(index: number): void {
    this.fclQuantities.removeAt(index);
  }

  save(): void {
    if (this.saving) return;
    if (this.serviceType === 'FCL' && this.fclQuantities.length === 0) {
      this.errorMessage = 'Agrega al menos un tipo de contenedor.'; return;
    }
    if (this.serviceType === 'FCL') {
      const ids = this.fclQuantities.controls.map(control => control.value.cargoTypeId);
      if (ids.some(id => !id) || new Set(ids).size !== ids.length) {
        this.errorMessage = 'Selecciona tipos de contenedor válidos y sin repetir.'; return;
      }
    }
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const value = this.form.getRawValue();
    const request: ClientServiceOrderRequest = {
      clientOrderNumber: value.clientOrderNumber.trim(),
      clientId: this.data.clientId,
      operationId: this.data.operationId,
      reference: value.reference.trim() || null,
      serviceType: this.serviceType,
      receivedDate: value.receivedDate || null,
      estimatedArrivalDate: value.estimatedArrivalDate || null,
      origin: value.origin.trim() || null,
      destination: value.destination.trim() || null,
      observation: value.observation.trim() || null,
      statusId: null,
      fclQuantities: this.serviceType === 'FCL'
        ? (value.fclQuantities as { cargoTypeId: number; totalQuantity: number }[])
          .map(item => ({ cargoTypeId: item.cargoTypeId, totalQuantity: item.totalQuantity }))
        : [],
      totalPackages: this.serviceType === 'LCL' ? value.totalPackages : null
    };
    this.saving = true;
    this.errorMessage = '';
    this.operations.createClientServiceOrder(request).subscribe({
      next: result => this.dialogRef.close(result),
      error: error => {
        console.error(error);
        this.saving = false;
        this.errorMessage = error.error?.message || error.error?.detail || 'No se pudo registrar la O/S del cliente.';
      }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
