import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { Client, ClientDocumentType, ClientRequest } from '../../../core/models/clients.models';
import { ClientService } from '../../../core/services/clients.service';

export interface ClientFormDialogData {
  mode: 'create' | 'edit';
  client?: Client;
}

@Component({
  selector: 'app-client-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './client-form-dialog.component.html',
  styleUrl: './client-form-dialog.component.scss'
})
export class ClientFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly data = inject<ClientFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ClientFormDialogComponent>);

  readonly isEditMode = this.data.mode === 'edit';
  isSaving = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    documentType: ['RUC' as ClientDocumentType, Validators.required],
    documentNumber: ['', [Validators.required, Validators.maxLength(20)]],
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    address: ['', Validators.maxLength(300)]
  });

  constructor() {
    if (this.data.client) {
      this.form.patchValue({
        documentType: this.data.client.documentType,
        documentNumber: this.data.client.documentNumber,
        businessName: this.data.client.businessName,
        address: this.data.client.address ?? ''
      });
    }
  }

  save(): void {
    if (this.form.invalid || this.isSaving) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    const value = this.form.getRawValue();
    const request: ClientRequest = {
      documentType: value.documentType,
      documentNumber: value.documentNumber.trim(),
      businessName: value.businessName.trim(),
      address: value.address.trim() || null
    };

    const operation = this.isEditMode && this.data.client
      ? this.clientService.updateClient(this.data.client.id, request)
      : this.clientService.createClient(request);

    operation.subscribe({
      next: client => this.dialogRef.close(client),
      error: error => {
        this.isSaving = false;
        this.errorMessage = error.error?.message
          || error.error?.detail
          || 'No se pudo guardar el cliente. Verifica los datos e inténtalo nuevamente.';
      }
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
