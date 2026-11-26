import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { ClientService } from '../../../core/services/clients.service';
import { ClientSubclientsService } from '../../../core/services/client-subclients.service';
import { LocationsService } from '../../../core/services/locations.service';
import { CargoTypeService } from '../../../core/services/cargo-types.service';
import { OperationsService } from '../../../core/services/operations.service';
import { Client } from '../../../core/models/clients.models';
import { ClientSubclient } from '../../../core/models/client-subclients.models';
import { ClientLocation, ClientSubclientLocation } from '../../../core/models/locations.models';
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
  private readonly clientSubclientsApi = inject(ClientSubclientsService);
  private readonly locationsApi = inject(LocationsService);
  private readonly operationsApi = inject(OperationsService);
  private readonly data = inject<OperationFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<OperationFormDialogComponent>);

  readonly isEditMode = this.data.mode === 'edit';
  clients: Client[] = [];
  cargoTypes: CargoType[] = [];
  subclients: ClientSubclient[] = [];
  clientLocations: ClientLocation[] = [];
  relationLocations: ClientSubclientLocation[] = [];
  loadingSubclients = false;
  loadingLocations = false;
  loadingOptions = true;
  isSaving = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    reference: ['', Validators.maxLength(150)],
    clientId: [0, [Validators.required, Validators.min(1)]],
    clientSubclientId: [null as number | null],
    originLocationId: [null as number | null],
    destinationLocationId: [null as number | null],
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
          clientSubclientId: this.data.operation.clientSubclientId ?? null,
          cargoTypeId: this.data.operation.cargoTypeId, customerOrderNumber: this.data.operation.customerOrderNumber ?? '',
          origin: this.data.operation.origin ?? '', destination: this.data.operation.destination ?? '',
          quantity: this.data.operation.quantity, estimatedDate: this.data.operation.estimatedDate ?? '',
          observation: this.data.operation.observation ?? ''
        });
        const clientId = this.form.controls.clientId.value;
        if (clientId > 0) this.loadClientLocations(clientId);
        if (this.data.operation) this.loadSubclients(clientId);
      },
      error: error => { console.error(error); this.loadingOptions = false; this.errorMessage = 'No se pudieron cargar clientes y tipos de carga.'; }
    });
  }

  onClientChange(clientId: number): void {
    this.form.patchValue({ clientSubclientId: null, originLocationId: null, destinationLocationId: null });
    this.subclients = [];
    this.relationLocations = [];
    this.clientLocations = [];
    if (clientId > 0) {
      this.loadClientLocations(clientId);
      this.loadSubclients(clientId);
    }
  }

  onRelationChange(relationId: number | null): void {
    this.form.patchValue({ originLocationId: null, destinationLocationId: null });
    this.relationLocations = [];
    if (relationId === null) return;
    this.loadingLocations = true;
    this.locationsApi.listRelationLocations(relationId).subscribe({
      next: locations => { this.relationLocations = locations.filter(item => item.active); this.loadingLocations = false; },
      error: error => { console.error('Ubicaciones de la relación:', error); this.loadingLocations = false; this.errorMessage = 'No se pudieron cargar las ubicaciones de la relación.'; }
    });
  }

  private loadClientLocations(clientId: number): void {
    this.loadingLocations = true;
    this.locationsApi.listClientLocations(clientId).subscribe({
      next: locations => { this.clientLocations = locations.filter(item => item.active); this.loadingLocations = false; },
      error: error => { console.error('Ubicaciones del cliente:', error); this.clientLocations = []; this.loadingLocations = false; this.errorMessage = 'No se pudieron cargar las ubicaciones del cliente.'; }
    });
  }

  private loadSubclients(clientId: number): void {
    this.loadingSubclients = true;
    this.clientSubclientsApi.getByClient(clientId).subscribe({
      next: relations => {
        this.subclients = relations.filter(item => item.active);
        this.loadingSubclients = false;
        const selectedId = this.form.controls.clientSubclientId.value;
        if (selectedId && !this.subclients.some(item => item.id === selectedId)) {
          this.form.controls.clientSubclientId.setValue(null);
        }
        if (selectedId && this.subclients.some(item => item.id === selectedId)) this.onRelationChange(selectedId);
      },
      error: error => {
        console.error('Relaciones comerciales:', error);
        this.subclients = [];
        this.loadingSubclients = false;
        this.errorMessage = 'No se pudieron cargar las relaciones comerciales del cliente.';
      }
    });
  }

  get selectedCargoType(): CargoType | undefined { return this.cargoTypes.find(x => x.id === this.form.controls.cargoTypeId.value); }

  get originLocations(): Array<{id:number; name:string; address:string}> {
    if (this.form.controls.clientSubclientId.value !== null) {
      return this.relationLocations.filter(item => item.usageType === 'RECOJO' || item.usageType === 'AMBOS')
        .map(item => ({ id: item.locationId, name: item.locationName, address: item.address }));
    }
    return this.clientLocations.map(item => ({ id: item.id, name: item.name, address: item.address }));
  }

  get destinationLocations(): Array<{id:number; name:string; address:string}> {
    if (this.form.controls.clientSubclientId.value !== null) {
      return this.relationLocations.filter(item => item.usageType === 'ENTREGA' || item.usageType === 'AMBOS')
        .map(item => ({ id: item.locationId, name: item.locationName, address: item.address }));
    }
    return this.clientLocations.map(item => ({ id: item.id, name: item.name, address: item.address }));
  }

  selectOriginLocation(locationId: number | null): void {
    const location = this.originLocations.find(item => item.id === locationId);
    if (location) this.form.controls.origin.setValue(this.formatAddress(location.name, location.address));
  }

  selectDestinationLocation(locationId: number | null): void {
    const location = this.destinationLocations.find(item => item.id === locationId);
    if (location) this.form.controls.destination.setValue(this.formatAddress(location.name, location.address));
  }

  private formatAddress(name: string, address: string): string {
    return `${name}: ${address}`.slice(0, 200);
  }

  clearOriginLocation(): void { this.form.controls.originLocationId.setValue(null); }
  clearDestinationLocation(): void { this.form.controls.destinationLocationId.setValue(null); }

  save(): void {
    if (this.loadingOptions || this.form.invalid || this.isSaving) { this.form.markAllAsTouched(); return; }
    this.isSaving = true;
    this.errorMessage = '';
    const value = this.form.getRawValue();
    const request: OperationRequest = {
      reference: value.reference.trim() || null, clientId: value.clientId,
      clientSubclientId: value.clientSubclientId, cargoTypeId: value.cargoTypeId,
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
