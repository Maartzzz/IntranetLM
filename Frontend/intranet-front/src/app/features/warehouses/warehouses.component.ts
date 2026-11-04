import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { WarehousesService } from '../../core/services/warehouses.service';
import { Warehouse, WarehouseRequest, WarehouseType, WarehouseLocation } from '../../core/models/warehouses.models';

type LocationType = 'ALMACEN' | 'DEPOSITO' | 'PUERTO' | 'PLANTA' | 'CENTRO_DISTRIBUCION' | 'OTRO';
interface NewLocationForm {
  name: string;
  locationType: LocationType;
  address: string;
  reference: string;
  district: string;
  province: string;
  department: string;
}

@Component({
  selector: 'app-warehouses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './warehouses.component.html',
  styleUrl: './warehouses.component.scss'
})
export class WarehousesComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly api = inject(WarehousesService);
  warehouses: Warehouse[] = [];
  locations: WarehouseLocation[] = [];
  canManage = false;
  loading = false;
  saving = false;
  error = '';
  success = '';
  search = '';
  showForm = false;
  editingId: number | null = null;
  locationMode: 'EXISTING' | 'NEW' = 'EXISTING';
  newLocation: NewLocationForm = this.emptyLocationForm();
  form: WarehouseRequest = this.emptyForm();

  readonly types: { value: WarehouseType; label: string }[] = [
    { value: 'ALMACEN_DEVOLUCION', label: 'Almacén de devolución' },
    { value: 'ALMACEN_VACIOS', label: 'Almacén de vacíos' },
    { value: 'ALMACEN_CARGA', label: 'Almacén de carga' },
    { value: 'DEPOSITO_TEMPORAL', label: 'Depósito temporal' },
    { value: 'OTRO', label: 'Otro' }
  ];

  get filtered(): Warehouse[] {
    const q = this.search.trim().toLocaleLowerCase();
    if (!q) return this.warehouses;
    return this.warehouses.filter(w => [w.warehouseCode, w.warehouseName, w.locationName, w.address, w.operatorName]
      .some(v => (v || '').toLocaleLowerCase().includes(q)));
  }

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({
      next: user => {
        this.canManage = user.role === 'ADMIN' || user.role === 'SUPERVISOR';
        this.load();
      },
      error: () => this.error = 'No se pudo validar el usuario actual.'
    });
  }

  private load(): void {
    this.loading = true;
    forkJoin({
      warehouses: this.canManage ? this.api.listAll() : this.api.listActive(),
      locations: this.api.listLocations()
    }).subscribe({
      next: data => { this.warehouses = data.warehouses; this.locations = data.locations.filter(x => x.active); this.loading = false; },
      error: () => { this.error = 'No se pudieron cargar los almacenes y ubicaciones.'; this.loading = false; }
    });
  }

  openCreate(): void { this.editingId = null; this.form = this.emptyForm(); this.locationMode = 'EXISTING'; this.newLocation = this.emptyLocationForm(); this.showForm = true; this.error = ''; this.success = ''; }
  openEdit(row: Warehouse): void {
    this.editingId = row.id;
    this.form = { locationId: row.locationId, warehouseCode: row.warehouseCode, warehouseName: row.warehouseName,
      warehouseType: row.warehouseType, operatorName: row.operatorName, observations: row.observations };
    this.locationMode = 'EXISTING';
    this.newLocation = this.emptyLocationForm();
    this.showForm = true; this.error = ''; this.success = '';
  }
  cancel(): void { this.showForm = false; this.editingId = null; this.form = this.emptyForm(); this.locationMode = 'EXISTING'; this.newLocation = this.emptyLocationForm(); }

  save(): void {
    if (!this.form.warehouseCode.trim() || !this.form.warehouseName.trim() ||
        !this.form.operatorName.trim() || !this.form.warehouseType) return;

    if (this.locationMode === 'EXISTING' && !this.form.locationId) return;
    if (this.locationMode === 'NEW' && (!this.newLocation.name.trim() || !this.newLocation.address.trim())) return;

    this.saving = true;
    this.error = '';
    this.success = '';

    if (this.locationMode === 'NEW') {
      this.api.createLocation({
        name: this.newLocation.name.trim(),
        locationType: this.newLocation.locationType,
        address: this.newLocation.address.trim(),
        reference: this.newLocation.reference.trim() || null,
        district: this.newLocation.district.trim() || null,
        province: this.newLocation.province.trim() || null,
        department: this.newLocation.department.trim() || null,
        latitude: null,
        longitude: null
      }).subscribe({
        next: location => {
          // Se conserva la ubicación creada y se pasa a modo existente.
          // Si falla el alta del almacén, el usuario podrá reintentar sin duplicarla.
          this.locations = [location, ...this.locations.filter(x => x.id !== location.id)];
          this.form.locationId = location.id;
          this.locationMode = 'EXISTING';
          this.persistWarehouse();
        },
        error: err => {
          this.error = err?.error?.message || 'No se pudo registrar la ubicación.';
          this.saving = false;
        }
      });
      return;
    }

    this.persistWarehouse();
  }

  private persistWarehouse(): void {
    const request: WarehouseRequest = {
      ...this.form,
      warehouseCode: this.form.warehouseCode.trim(),
      warehouseName: this.form.warehouseName.trim(),
      operatorName: this.form.operatorName.trim(),
      observations: this.form.observations?.trim() || null
    };
    const action = this.editingId === null
      ? this.api.create(request)
      : this.api.update(this.editingId, request);

    action.subscribe({
      next: saved => {
        this.warehouses = this.editingId === null
          ? [saved, ...this.warehouses]
          : this.warehouses.map(x => x.id === saved.id ? saved : x);
        this.success = this.editingId === null
          ? 'Almacén registrado correctamente.'
          : 'Almacén actualizado correctamente.';
        this.cancel();
        this.saving = false;
      },
      error: err => {
        this.error = err?.error?.message || 'No se pudo guardar el almacén. La ubicación quedó registrada y puedes volver a intentarlo seleccionándola.';
        this.saving = false;
      }
    });
  }

  toggle(row: Warehouse): void {
    this.api.changeStatus(row.id, !row.active).subscribe({
      next: updated => { this.warehouses = this.warehouses.map(x => x.id === updated.id ? updated : x); },
      error: err => this.error = err?.error?.message || 'No se pudo cambiar el estado del almacén.'
    });
  }

  typeLabel(type: WarehouseType): string { return this.types.find(x => x.value === type)?.label || type; }
  private emptyForm(): WarehouseRequest {
    return { locationId: 0, warehouseCode: '', warehouseName: '', warehouseType: 'ALMACEN_DEVOLUCION', operatorName: '', observations: null };
  }

  private emptyLocationForm(): NewLocationForm {
    return { name: '', locationType: 'ALMACEN', address: '', reference: '', district: '', province: '', department: '' };
  }
}
