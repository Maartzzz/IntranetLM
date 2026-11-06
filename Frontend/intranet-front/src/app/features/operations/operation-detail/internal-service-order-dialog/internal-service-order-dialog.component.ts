import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin, from, of } from 'rxjs';
import { concatMap, toArray } from 'rxjs/operators';
import { ClientServiceOrder } from '../../../../core/models/client-service-orders.models';
import { InternalServiceOrderRequest } from '../../../../core/models/internal-service-orders.models';
import { Location } from '../../../../core/models/locations.models';
import { CargoType } from '../../../../core/models/cargo-types.models';
import { Status } from '../../../../core/models/operations.models';
import { LocationsService } from '../../../../core/services/locations.service';
import { CargoTypeService } from '../../../../core/services/cargo-types.service';
import { OperationsService } from '../../../../core/services/operations.service';

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
  readonly data = inject<{ clientOrder: ClientServiceOrder; distribution: import('../../../../core/models/internal-service-orders.models').ClientServiceOrderDistribution }>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<InternalServiceOrderDialogComponent>);

  locations: Location[] = [];
  cargoTypes: CargoType[] = [];
  statuses: Status[] = [];
  loading = true;
  saving = false;
  errorMessage = '';

  readonly common = this.fb.nonNullable.group({
    originLocationId: [0, [Validators.required, Validators.min(1)]],
    destinationLocationId: [0, [Validators.required, Validators.min(1)]],
    statusId: [0],
    scheduledDate: [''],
    observations: [''],
    fclRows: this.fb.array<FormGroup>([]),
    lclRows: this.fb.array<FormGroup>([])
  });

  get fclRows(): FormArray<FormGroup> { return this.common.controls.fclRows; }
  get lclRows(): FormArray<FormGroup> { return this.common.controls.lclRows; }

  constructor() {
    forkJoin({
      locations: this.locationsApi.listActive(),
      cargoTypes: this.cargoApi.getActiveCargoTypes(),
      statuses: this.api.getStatuses('OS_INTERNA')
    }).subscribe({
      next: result => {
        this.locations = result.locations.filter(x => x.active);
        this.cargoTypes = result.cargoTypes.filter(x => x.active && x.container);
        this.statuses = result.statuses;
        if (this.data.clientOrder.serviceType === 'FCL') {
          for (const item of this.data.distribution.fcl) {
            for (let i = 0; i < item.available; i++) this.addFclRow(item.cargoTypeId);
          }
        } else {
          this.addLclRow(this.data.distribution.availablePackages ?? 0);
        }
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'No se pudieron cargar ubicaciones, tipos de contenedor o estados.';
        this.loading = false;
      }
    });
  }

  get isFcl(): boolean { return this.data.clientOrder.serviceType === 'FCL'; }
  addFclRow(cargoTypeId = 0): void {
    this.fclRows.push(this.fb.nonNullable.group({
      cargoTypeId: [cargoTypeId, [Validators.required, Validators.min(1)]],
      containerNumber: [''],
      cubicMeters: [null as number | null, Validators.min(0)],
      returnDueDate: [''],
      memo: ['']
    }));
  }
  removeFclRow(index: number): void { this.fclRows.removeAt(index); }
  addLclRow(packages = 0): void {
    this.lclRows.push(this.fb.nonNullable.group({
      packages: [packages, [Validators.required, Validators.min(0)]],
      weightKg: [null as number | null, Validators.min(0)],
      cubicMeters: [null as number | null, Validators.min(0)]
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
    from(requests).pipe(
      concatMap(request => this.api.createInternalServiceOrder(request)),
      toArray()
    ).subscribe({
      next: created => this.ref.close(created),
      error: err => {
        console.error(err);
        this.saving = false;
        this.errorMessage = err.error?.message || err.error?.detail || 'No se pudieron crear todas las O/S internas. Verifica la distribución y vuelve a consultar antes de reintentar.';
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
      destinationLocationId: c.destinationLocationId, providerId: null,
      scheduledDate: c.scheduledDate || null, statusId: c.statusId || null,
      billingStatus: 'PENDIENTE', observations: c.observations.trim() || order.observation,
      fclDetail: null, lclDetail: null, scheduleReason: c.scheduledDate ? 'Programación inicial al crear O/S interna' : null
    };
  }

  cancel(): void { this.ref.close(); }
}
