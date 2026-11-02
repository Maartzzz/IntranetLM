import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { ClientService } from '../../../core/services/clients.service';
import { CargoTypeService } from '../../../core/services/cargo-types.service';
import { OperationsService } from '../../../core/services/operations.service';
import { Client } from '../../../core/models/clients.models';
import { CargoType } from '../../../core/models/cargo-types.models';
import { Operation, OperationRequest } from '../../../core/models/operations.models';

export interface OperationFormDialogData { mode: 'create' | 'edit'; operation?: Operation; }

@Component({
  selector: 'app-operation-form-dialog', standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule],
  templateUrl: './operation-form-dialog.component.html', styleUrl: './operation-form-dialog.component.scss'
})
export class OperationFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly clientsApi = inject(ClientService);
  private readonly cargoApi = inject(CargoTypeService);
  private readonly operationsApi = inject(OperationsService);
  private readonly data = inject<OperationFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<OperationFormDialogComponent>);

  readonly isEditMode = this.data.mode === 'edit';
  clients: Client[] = [];
  cargoTypes: CargoType[] = [];
  loadingOptions = true;
  isSaving = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    reference: ['', Validators.maxLength(150)],
    clientId: [0, [Validators.required, Validators.min(1)]],
    cargoTypeId: [0, [Validators.required, Validators.min(1)]],
    customerOrderNumber: ['', Validators.maxLength(100)],
    origin: ['', Validators.maxLength(200)],
    destination: ['', Validators.maxLength(200)],
    quantity: [null as number | null, Validators.min(1)],
    estimatedDate: [''],
    observation: ['', Validators.maxLength(1000)]
  });

  constructor() {
    const requests = forkJoin({ clients: this.clientsApi.getActiveClients(), cargoTypes: this.cargoApi.getActiveCargoTypes() });
    requests.subscribe({
      next: result => {
        this.clients = result.clients;
        this.cargoTypes = result.cargoTypes;
        this.loadingOptions = false;
        if (this.data.operation) this.form.patchValue({
          reference: this.data.operation.reference ?? '', clientId: this.data.operation.clientId,
          cargoTypeId: this.data.operation.cargoTypeId, customerOrderNumber: this.data.operation.customerOrderNumber ?? '',
          origin: this.data.operation.origin ?? '', destination: this.data.operation.destination ?? '',
          quantity: this.data.operation.quantity, estimatedDate: this.data.operation.estimatedDate ?? '',
          observation: this.data.operation.observation ?? ''
        });
      },
      error: error => { console.error(error); this.loadingOptions = false; this.errorMessage = 'No se pudieron cargar clientes y tipos de carga.'; }
    });
  }

  get selectedCargoType(): CargoType | undefined { return this.cargoTypes.find(x => x.id === this.form.controls.cargoTypeId.value); }

  save(): void {
    if (this.loadingOptions || this.form.invalid || this.isSaving) { this.form.markAllAsTouched(); return; }
    this.isSaving = true;
    this.errorMessage = '';
    const value = this.form.getRawValue();
    const request: OperationRequest = {
      reference: value.reference.trim() || null, clientId: value.clientId, cargoTypeId: value.cargoTypeId,
      customerOrderNumber: value.customerOrderNumber.trim() || null, origin: value.origin.trim() || null,
      destination: value.destination.trim() || null, quantity: value.quantity,
      estimatedDate: value.estimatedDate || null, statusId: null, observation: value.observation.trim() || null
    };
    const call = this.isEditMode && this.data.operation
      ? this.operationsApi.updateOperation(this.data.operation.id, request)
      : this.operationsApi.createOperation(request);
    call.subscribe({
      next: result => this.dialogRef.close(result),
      error: error => { console.error(error); this.isSaving = false; this.errorMessage = error.error?.message || error.error?.detail || 'No se pudo guardar la operación.'; }
    });
  }

  cancel(): void { this.dialogRef.close(); }
}
