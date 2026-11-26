import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { Client, ClientDocumentType, ClientRequest } from '../../../core/models/clients.models';
import { Location, LocationRequest, LocationType } from '../../../core/models/locations.models';
import { ClientService } from '../../../core/services/clients.service';

export interface ClientFormDialogData { mode: 'create' | 'edit'; client?: Client; }

@Component({
  selector: 'app-client-form-dialog', standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule, MatTabsModule, MatIconModule],
  templateUrl: './client-form-dialog.component.html', styleUrl: './client-form-dialog.component.scss'
})
export class ClientFormDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly clientService = inject(ClientService);
  private readonly data = inject<ClientFormDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<ClientFormDialogComponent>);
  readonly isEditMode = this.data.mode === 'edit';
  isSaving = false; isLocationSaving = false; errorMessage = ''; locationError = ''; activeTab = 0;
  clientId: number | null = this.data.client?.id ?? null;
  locations: Location[] = [];
  editingLocationId: number | null = null;
  showLocationForm = false;
  readonly form = this.fb.nonNullable.group({
    documentType: ['RUC' as ClientDocumentType, Validators.required],
    documentNumber: ['', [Validators.required, Validators.maxLength(20)]],
    businessName: ['', [Validators.required, Validators.maxLength(200)]],
    address: ['', Validators.maxLength(300)]
  });
  readonly locationForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    locationType: ['ALMACEN' as LocationType, Validators.required],
    address: ['', [Validators.required, Validators.maxLength(300)]],
    reference: [''], district: [''], province: [''], department: ['']
  });
  constructor() {
    if (this.data.client) {
      this.form.patchValue({ documentType: this.data.client.documentType, documentNumber: this.data.client.documentNumber, businessName: this.data.client.businessName, address: this.data.client.address ?? '' });
      this.loadLocations();
    }
  }
  loadLocations(): void {
    if (!this.clientId) return;
    this.clientService.getClientLocations(this.clientId).subscribe({ next: rows => this.locations = rows, error: () => this.locationError = 'No se pudieron cargar las ubicaciones del cliente.' });
  }
  save(): void {
    if (this.form.invalid || this.isSaving) { this.form.markAllAsTouched(); return; }
    this.isSaving = true; this.errorMessage = '';
    const v = this.form.getRawValue();
    const request: ClientRequest = { documentType: v.documentType, documentNumber: v.documentNumber.trim(), businessName: v.businessName.trim(), address: v.address.trim() || null };
    const operation = this.isEditMode && this.data.client ? this.clientService.updateClient(this.data.client.id, request) : this.clientService.createClient(request);
    operation.subscribe({ next: client => { this.clientId = client.id; this.dialogRef.close(client); }, error: error => { this.isSaving = false; this.errorMessage = error.error?.message || error.error?.detail || 'No se pudo guardar el cliente. Verifica los datos e inténtalo nuevamente.'; } });
  }
  saveAndContinue(): void {
    if (this.form.invalid || this.isSaving) { this.form.markAllAsTouched(); return; }
    this.isSaving = true; this.errorMessage = '';
    const v = this.form.getRawValue(); const req: ClientRequest = { documentType:v.documentType, documentNumber:v.documentNumber.trim(), businessName:v.businessName.trim(), address:v.address.trim() || null };
    const op = this.clientId ? this.clientService.updateClient(this.clientId, req) : this.clientService.createClient(req);
    op.subscribe({ next: client => { this.clientId=client.id; this.isSaving=false; this.activeTab=1; this.loadLocations(); }, error: e => { this.isSaving=false; this.errorMessage=e.error?.message || 'No se pudo guardar el cliente.'; } });
  }
  openNewLocation(): void { this.editingLocationId=null; this.locationForm.reset({name:'',locationType:'ALMACEN',address:'',reference:'',district:'',province:'',department:''}); this.locationError=''; this.showLocationForm=true; }
  editLocation(location: Location): void { this.editingLocationId=location.id; this.locationForm.patchValue({name:location.name,locationType:location.locationType,address:location.address,reference:location.reference||'',district:location.district||'',province:location.province||'',department:location.department||''}); this.locationError=''; this.showLocationForm=true; }
  saveLocation(): void {
    if (!this.clientId || this.locationForm.invalid || this.isLocationSaving) { this.locationForm.markAllAsTouched(); return; }
    this.isLocationSaving=true; this.locationError=''; const v=this.locationForm.getRawValue();
    const req:LocationRequest={name:v.name.trim(),locationType:v.locationType,address:v.address.trim(),reference:v.reference.trim()||null,district:v.district.trim()||null,province:v.province.trim()||null,department:v.department.trim()||null,latitude:null,longitude:null};
    const op=this.editingLocationId ? this.clientService.updateClientLocation(this.clientId,this.editingLocationId,req) : this.clientService.addClientLocation(this.clientId,req);
    op.subscribe({next:loc=>{this.locations=this.editingLocationId?this.locations.map(x=>x.id===loc.id?loc:x):[...this.locations,loc];this.showLocationForm=false;this.isLocationSaving=false;},error:e=>{this.locationError=e.error?.message||'No se pudo guardar la ubicación.';this.isLocationSaving=false;}});
  }
  toggleLocation(location: Location): void { if(!this.clientId)return; this.clientService.changeClientLocationStatus(this.clientId,location.id,!location.active).subscribe({next:updated=>this.locations=this.locations.map(x=>x.id===updated.id?updated:x),error:()=>this.locationError='No se pudo cambiar el estado de la ubicación.'}); }
  cancel(): void { this.dialogRef.close(); }
  finish(): void { this.save(); }
}
