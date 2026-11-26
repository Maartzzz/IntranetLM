import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { ClientServiceOrder } from '../../../../core/models/client-service-orders.models';
import { InternalServiceOrder, InternalServiceOrderRequest } from '../../../../core/models/internal-service-orders.models';
import { Location, ClientSubclientLocation } from '../../../../core/models/locations.models';
import { CargoType } from '../../../../core/models/cargo-types.models';
import { Status } from '../../../../core/models/operations.models';
import { LocationsService } from '../../../../core/services/locations.service';
import { CargoTypeService } from '../../../../core/services/cargo-types.service';
import { OperationsService } from '../../../../core/services/operations.service';
import { VehiclesService, FleetVehicle } from '../../../../core/services/vehicles.service';

  interface FclRowValue {
    cargoTypeId: number;
    containerNumber: string;
    cubicMeters: number | null;
    returnDueDate: string;
    memo: string;
  }

  interface LclRowValue {
    packages: number;
    weightKg: number | null;
    cubicMeters: number | null;
  }

@Component({
  selector: 'app-internal-service-order-dialog',
  standalone: true,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatSelectModule],
  templateUrl: './internal-service-order-dialog.component.html',
  styleUrl: './internal-service-order-dialog.component.scss'
})
export class InternalServiceOrderDialogComponent {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(OperationsService);
  private readonly locationsApi = inject(LocationsService);
  private readonly cargoApi = inject(CargoTypeService);
  private readonly vehiclesApi = inject(VehiclesService);
  readonly data = inject<{ clientOrder: ClientServiceOrder; distribution: import('../../../../core/models/internal-service-orders.models').ClientServiceOrderDistribution; clientSubclientId?: number | null; order?: InternalServiceOrder }>(MAT_DIALOG_DATA);
  readonly editing = !!this.data.order;
  private readonly ref = inject(MatDialogRef<InternalServiceOrderDialogComponent>);

  locations: Location[] = [];
  originLocations: Array<Location | ClientSubclientLocation> = [];
  destinationLocations: Array<Location | ClientSubclientLocation> = [];
  usesSubclientLocations = false;
  cargoTypes: CargoType[] = [];
  statuses: Status[] = [];
  vehicles: FleetVehicle[] = [];
  loading = true;
  saving = false;
  errorMessage = '';

  readonly common = this.fb.nonNullable.group({
    originLocationId: [0, [Validators.required, Validators.min(1)]],
    destinationLocationId: [0, [Validators.required, Validators.min(1)]],
    statusId: [0],
    scheduledDate: [''],
    observations: [''],
    vehicleId: [0],
    fclRows: this.fb.array<FormGroup>([]),
    lclRows: this.fb.array<FormGroup>([])
  });

  get fclRows(): FormArray<FormGroup> { return this.common.controls.fclRows; }
  get lclRows(): FormArray<FormGroup> { return this.common.controls.lclRows; }

  constructor() {
    const locationsRequest = this.data.clientSubclientId
      ? this.locationsApi.listRelationLocations(this.data.clientSubclientId)
      : this.locationsApi.listActive();

    forkJoin({
      locations: locationsRequest,
      cargoTypes: this.cargoApi.getActiveCargoTypes(),
      statuses: this.api.getStatuses('OS_INTERNA'),
      vehicles: this.vehiclesApi.listActive()
    }).subscribe({
      next: result => {
        this.usesSubclientLocations = !!this.data.clientSubclientId;
        if (this.usesSubclientLocations) {
          const relationLocations = (result.locations as ClientSubclientLocation[]).filter(x => x.active);
          this.originLocations = relationLocations.filter(x => x.usageType === 'RECOJO' || x.usageType === 'AMBOS');
          this.destinationLocations = relationLocations.filter(x => x.usageType === 'ENTREGA' || x.usageType === 'AMBOS');
        } else {
          this.locations = (result.locations as Location[]).filter(x => x.active);
          this.originLocations = this.locations;
          this.destinationLocations = this.locations;
        }
        this.cargoTypes = result.cargoTypes.filter(x => x.active && x.container);
        this.statuses = result.statuses;
        this.vehicles = result.vehicles.filter(v => v.type !== 'CARRETA');
        if (this.editing && this.data.order) {
          const order = this.data.order;
          this.common.patchValue({ originLocationId: order.originLocationId, destinationLocationId: order.destinationLocationId, statusId: order.statusId ?? 0, scheduledDate: order.scheduledDate ?? '', observations: order.observations ?? '', vehicleId: order.vehicleId ?? 0 });
          const detail = (order.detail ?? {}) as any;
          if (this.isFcl) this.addFclRow(detail.cargoTypeId ?? 0, detail);
          else this.addLclRow(detail.packages ?? 0, detail);
        } else if (this.data.clientOrder.serviceType === 'FCL') {
          for (const item of this.data.distribution.fcl) for (let i = 0; i < item.available; i++) this.addFclRow(item.cargoTypeId);
        } else this.addLclRow(this.data.distribution.availablePackages ?? 0);
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'No se pudieron cargar ubicaciones, tipos de contenedor o estados.';
        this.loading = false;
      }
    });
  }

  locationId(location: Location | ClientSubclientLocation): number {
    return 'locationId' in location ? location.locationId : location.id;
  }

  locationLabel(location: Location | ClientSubclientLocation): string {
    return 'locationName' in location ? `${location.locationName} · ${location.address}` : `${location.name} · ${location.address}`;
  }

  get isFcl(): boolean { return this.data.clientOrder.serviceType === 'FCL'; }
  addFclRow(cargoTypeId = 0, detail?: any): void {
    this.fclRows.push(this.fb.nonNullable.group({
      cargoTypeId: [cargoTypeId, [Validators.required, Validators.min(1)]],
      containerNumber: [detail?.containerNumber ?? ''],
      cubicMeters: [detail?.cubicMeters ?? null as number | null, Validators.min(0)],
      returnDueDate: [detail?.returnDueDate ?? ''],
      memo: [detail?.memo ?? '']
    }));
  }
  removeFclRow(index: number): void { this.fclRows.removeAt(index); }
  addLclRow(packages = 0, detail?: any): void {
    this.lclRows.push(this.fb.nonNullable.group({
      packages: [packages, [Validators.required, Validators.min(0)]],
      weightKg: [detail?.weightKg ?? null as number | null, Validators.min(0)],
      cubicMeters: [detail?.cubicMeters ?? null as number | null, Validators.min(0)]
    }));
  }
  removeLclRow(index: number): void { this.lclRows.removeAt(index); }

  save(): void {
    if (this.saving || this.loading) return;
    if (this.common.invalid || (this.isFcl ? !this.fclRows.length || this.fclRows.invalid : !this.lclRows.length || this.lclRows.invalid)) {
      this.common.markAllAsTouched(); this.fclRows.markAllAsTouched(); this.lclRows.markAllAsTouched(); return;
    }
    const c = this.common.getRawValue();

    const requests: InternalServiceOrderRequest[] = this.isFcl
      ? (this.fclRows.getRawValue() as FclRowValue[]).map(row => ({
          ...this.baseRequest(),
          fclDetail: {
            cargoTypeId: Number(row.cargoTypeId),
            containerNumber: row.containerNumber.trim() || null,
            cubicMeters: row.cubicMeters,
            emptyWarehouseLocationId: null,
            returnDueDate: row.returnDueDate || null,
            memo: row.memo.trim() || null
          },
          lclDetail: null
        }))
      : (this.lclRows.getRawValue() as LclRowValue[]).map(row => ({
          ...this.baseRequest(),
          fclDetail: null,
          lclDetail: {
            packages: row.packages,
            weightKg: row.weightKg,
            cubicMeters: row.cubicMeters
          }
        }));
    this.saving = true; this.errorMessage = '';
    if (this.editing && this.data.order) {
      this.api.updateInternalServiceOrder(this.data.order.id, requests[0]).subscribe({ next: updated => this.ref.close(updated), error: err => { console.error(err); this.saving = false; this.errorMessage = err.error?.message || err.error?.detail || 'No se pudo actualizar la O/S interna.'; } });
      return;
    }
    this.api.createInternalServiceOrders(requests).subscribe({
      next: created => this.ref.close(created),
      error: err => {
        console.error(err);
        this.saving = false;
        this.errorMessage = err.error?.message || err.error?.detail || 'No se pudieron generar las O/S internas. No se guardó ninguna; revisa la distribución e inténtalo nuevamente.';
      }
    });
  }

  private baseRequest(): InternalServiceOrderRequest {
    const c = this.common.getRawValue();
    const order = this.data.clientOrder;
    return {
      clientId: order.clientId, clientServiceOrderId: order.id,
      clientOrderNumber: order.clientOrderNumber, serviceType: order.serviceType,
      reference: order.reference, originLocationId: c.originLocationId,
      destinationLocationId: c.destinationLocationId, providerId: this.data.order?.providerId ?? null,
      scheduledDate: c.scheduledDate || null, statusId: c.statusId || null,
      billingStatus: this.data.order?.billingStatus ?? 'PENDIENTE', observations: c.observations.trim() || order.observation,
      vehicleId: c.vehicleId || null, fclDetail: null, lclDetail: null, scheduleReason: this.editing ? 'Edición de O/S interna' : (c.scheduledDate ? 'Programación inicial al crear O/S interna' : null)
    };
  }

  cancel(): void { this.ref.close(); }
}
