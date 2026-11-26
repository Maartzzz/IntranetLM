import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { Client } from '../../../core/models/clients.models';
import { ClientServiceOrder } from '../../../core/models/client-service-orders.models';
import { Location, ClientSubclientLocation } from '../../../core/models/locations.models';
import { CargoType } from '../../../core/models/cargo-types.models';
import { Status } from '../../../core/models/operations.models';
import { ClientService } from '../../../core/services/clients.service';
import { ClientSubclientsService } from '../../../core/services/client-subclients.service';
import { ClientSubclient } from '../../../core/models/client-subclients.models';
import { LocationsService } from '../../../core/services/locations.service';
import { CargoTypeService } from '../../../core/services/cargo-types.service';
import { OperationsService } from '../../../core/services/operations.service';
import { VehiclesService, FleetVehicle } from '../../../core/services/vehicles.service';
import { InternalServiceOrderRequest } from '../../../core/models/internal-service-orders.models';

@Component({
  selector: 'app-direct-internal-order-dialog', standalone: true,
  imports: [CommonModule, ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './direct-internal-order-dialog.component.html',
  styleUrl: './direct-internal-order-dialog.component.scss'
})
export class DirectInternalOrderDialogComponent {
  private fb = inject(FormBuilder);
  private clientsApi = inject(ClientService);
  private subclientsApi = inject(ClientSubclientsService);
  private locationsApi = inject(LocationsService);
  private cargoApi = inject(CargoTypeService);
  private operationsApi = inject(OperationsService);
  private vehiclesApi = inject(VehiclesService);
  private ref = inject(MatDialogRef<DirectInternalOrderDialogComponent>);
  readonly data = inject<{ scheduledDate: string }>(MAT_DIALOG_DATA);

  form = this.fb.nonNullable.group({
    clientId: [0, [Validators.required, Validators.min(1)]],
    clientSubclientId: [0],
    clientOrderMode: ['MANUAL'],
    clientServiceOrderId: [0],
    manualClientOrderNumber: [''],
    serviceType: ['LCL' as 'FCL'|'LCL', Validators.required],
    reference: [''], originLocationId: [0, [Validators.required, Validators.min(1)]],
    destinationLocationId: [0, [Validators.required, Validators.min(1)]],
    scheduledDate: [this.data.scheduledDate], statusId: [0], vehicleId: [0],
    cargoTypeId: [0], containerNumber: [''], packages: [1, [Validators.required, Validators.min(1)]],
    weightKg: [null as number|null], cubicMeters: [null as number|null], observations: ['']
  });
  clients: Client[] = []; subclients: ClientSubclient[] = []; clientOrders: ClientServiceOrder[] = []; locations: Location[] = [];
  originLocations: Array<Location | ClientSubclientLocation> = []; destinationLocations: Array<Location | ClientSubclientLocation> = [];
  cargoTypes: CargoType[] = []; statuses: Status[] = []; vehicles: FleetVehicle[] = [];
  loading = true; saving = false; errorMessage = '';
  get isFcl(): boolean { return this.form.controls.serviceType.value === 'FCL'; }
  get manualOrder(): boolean { return this.form.controls.clientOrderMode.value === 'MANUAL'; }
  get selectedVehicle(): FleetVehicle | undefined { return this.vehicles.find(v => v.id === this.form.controls.vehicleId.value); }

  constructor() {
    forkJoin({ clients: this.clientsApi.getActiveClients(), locations: this.locationsApi.listActive(), cargo: this.cargoApi.getActiveCargoTypes(), statuses: this.operationsApi.getStatuses('OS_INTERNA'), vehicles: this.vehiclesApi.listActive() }).subscribe({
      next: x => { this.clients = x.clients.filter(c=>c.active); this.locations=x.locations.filter(l=>l.active); this.originLocations=this.locations; this.destinationLocations=this.locations; this.cargoTypes=x.cargo.filter(c=>c.active&&c.container); this.statuses=x.statuses; this.vehicles=x.vehicles.filter(v=>v.active&&v.type!=='CARRETA'); this.loading=false; },
      error: e => { this.errorMessage=e?.error?.message||'No se pudieron cargar los catálogos.'; this.loading=false; }
    });
  }
  clientChanged(id: number): void {
    this.clientOrders=[];
    this.form.patchValue({clientSubclientId:0,clientServiceOrderId:0,clientOrderMode:'MANUAL',manualClientOrderNumber:'',originLocationId:0,destinationLocationId:0});
    this.originLocations=this.locations; this.destinationLocations=this.locations;
    this.subclients=[];
    if(id) this.subclientsApi.getByClient(id).subscribe({next: rows => this.subclients=rows.filter(x=>x.active), error:()=>this.errorMessage='No se pudieron cargar los subclientes del cliente.'});
    if(!id) return;
    this.operationsApi.getClientServiceOrders(id).subscribe({
      next: rows => {
        const active=(rows||[]).filter(o=>o.active);
        if(!active.length){this.clientOrders=[];return;}
        forkJoin(active.map(order=>this.operationsApi.getClientServiceOrderDistribution(order.id))).subscribe({
          next: balances => this.clientOrders=active.filter((order,index)=>order.serviceType==='FCL'
            ? (balances[index]?.fcl||[]).some(x=>x.available>0)
            : (balances[index]?.availablePackages||0)>0),
          error: () => this.errorMessage='No se pudo verificar la disponibilidad de las O/S del cliente.'
        });
      },
      error:()=>this.errorMessage='No se pudieron cargar las O/S del cliente.'
    });
  }
  relationChanged(relationId: number): void {
    this.form.patchValue({ originLocationId: 0, destinationLocationId: 0 });
    if (!relationId) { this.originLocations = this.locations; this.destinationLocations = this.locations; return; }
    this.locationsApi.listRelationLocations(relationId).subscribe({
      next: rows => {
        const active = rows.filter(row => row.active);
        this.originLocations = active.filter(row => row.usageType === 'RECOJO' || row.usageType === 'AMBOS');
        this.destinationLocations = active.filter(row => row.usageType === 'ENTREGA' || row.usageType === 'AMBOS');
      },
      error: () => { this.originLocations = []; this.destinationLocations = []; this.errorMessage = 'No se pudieron cargar las ubicaciones de la relación.'; }
    });
  }
  locationId(location: Location | ClientSubclientLocation): number { return 'locationId' in location ? location.locationId : location.id; }
  locationLabel(location: Location | ClientSubclientLocation): string { return 'locationName' in location ? `${location.locationName} · ${location.address}` : `${location.name} · ${location.address}`; }
  existingOrderChanged(id: number): void {
    const order = this.clientOrders.find(o => o.id === id);
    if (order) this.form.patchValue({ serviceType: order.serviceType, reference: order.reference || '' });
  }
  save(): void {
    if(this.loading||this.saving) return;
    const v=this.form.getRawValue();
    if(this.form.invalid || (this.manualOrder && !v.manualClientOrderNumber.trim()) || (this.isFcl && !v.cargoTypeId)) { this.form.markAllAsTouched(); return; }
    const selected=this.clientOrders.find(o=>o.id===v.clientServiceOrderId);
    if(!this.manualOrder && !selected){this.errorMessage='Selecciona una O/S del cliente válida o elige ingreso manual.';return;}
    if(selected && selected.serviceType!==v.serviceType){this.errorMessage='El tipo de servicio no coincide con la O/S del cliente seleccionada.';return;}
    const request: InternalServiceOrderRequest={
      clientId:v.clientId, clientSubclientId:v.clientSubclientId||null, clientServiceOrderId:selected?.id ?? null, clientOrderNumber:selected?.clientOrderNumber ?? v.manualClientOrderNumber.trim(),
      serviceType:v.serviceType, reference:v.reference.trim()||null, originLocationId:v.originLocationId,destinationLocationId:v.destinationLocationId,
      providerId:null,scheduledDate:v.scheduledDate||null,statusId:v.statusId||null,billingStatus:'PENDIENTE',observations:v.observations.trim()||null,
      vehicleId:v.vehicleId||null,
      fclDetail:this.isFcl?{cargoTypeId:v.cargoTypeId,containerNumber:v.containerNumber.trim()||null,cubicMeters:v.cubicMeters,emptyWarehouseLocationId:null,returnDueDate:null,memo:null}:null,
      lclDetail:this.isFcl?null:{packages:v.packages,weightKg:v.weightKg,cubicMeters:v.cubicMeters},
      scheduleReason:'Creación directa desde el planificador'
    };
    this.saving=true;this.errorMessage='';
    this.operationsApi.createInternalServiceOrders([request]).subscribe({next:r=>this.ref.close(r),error:e=>{this.saving=false;this.errorMessage=e?.error?.message||e?.error?.detail||'No se pudo crear la O/S interna.';}});
  }
  cancel():void{this.ref.close();}
}
