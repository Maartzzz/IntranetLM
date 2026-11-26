import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Vehicle {
  id: number;
  plate: string;
  type: 'FURGON' | 'PICKUP' | 'TRACTO' | 'CARRETA';
  brand: string | null;
  model: string | null;
  capacityDescription: string | null;
  active: boolean;
  observations: string | null;
  currentTrailerId?: number | null;
  currentTrailerPlate: string | null;
  currentTractorId?: number | null;
  currentTractorPlate: string | null;
}

interface Coupling {
  id: number;
  tractorId: number;
  tractorPlate: string;
  trailerId: number;
  trailerPlate: string;
  startAt: string;
  endAt: string | null;
  reason: string;
  observations: string | null;
}

interface DriverOption {
  id: number;
  username: string;
  fullName: string;
  licenseNumber: string;
}

interface DriverVehicleAssignment {
  id: number;
  driverId: number;
  driverUsername: string;
  driverName: string;
  licenseNumber: string;
  vehicleId: number;
  vehiclePlate: string;
  vehicleType: string;
  startAt: string;
  endAt: string | null;
  reason: string;
  observations: string | null;
  warnings: string[];
}

type VehicleTypeFilter = 'TODOS' | Vehicle['type'];

@Component({
  selector: 'app-vehicles',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './vehicles.component.html',
  styleUrl: './vehicles.component.scss'
})
export class VehiclesComponent implements OnInit {
  private http = inject(HttpClient);

  vehicles: Vehicle[] = [];
  history: Coupling[] = [];
  drivers: DriverOption[] = [];
  assignments: DriverVehicleAssignment[] = [];
  loading = false;
  error = '';
  success = '';
  warnings: string[] = [];
  showForm = false;
  showCoupling = false;
  showAssignment = false;
  showDetail = false;
  editingId: number | null = null;
  selectedVehicle: Vehicle | null = null;
  searchText = '';
  filterType: VehicleTypeFilter = 'TODOS';
  filterStatus: 'TODOS' | 'ACTIVOS' | 'INACTIVOS' = 'ACTIVOS';
  filterRelation: 'TODOS' | 'VINCULADO' | 'SIN_VINCULO' = 'TODOS';
  filterDriverRelation: 'TODOS' | 'VINCULADO' | 'SIN_VINCULO' = 'TODOS';
  filterDriverId: number | null = null;
  form: any = this.empty();
  coupling: any = this.emptyCoupling();
  assignment: any = this.emptyAssignment();

  ngOnInit(): void { this.load(); }

  empty(): any {
    return { plate: '', type: 'FURGON', brand: '', model: '', capacityDescription: '', active: true, observations: '' };
  }

  emptyCoupling(): any {
    return { tractorId: null, trailerId: null, startAt: this.localDateTime(), reason: 'REASIGNACION', observations: '' };
  }

  emptyAssignment(): any {
    return { driverId: null, vehicleId: null, startAt: this.localDateTime(), reason: 'ASIGNACION', observations: '' };
  }

  private localDateTime(): string {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
    return local.toISOString().slice(0, 16);
  }

  load(): void {
    this.loading = true;
    this.http.get<Vehicle[]>('/api/vehicles').subscribe({
      next: value => { this.vehicles = value; this.loading = false; this.refreshSelectedVehicle(); },
      error: e => { this.error = e?.error?.message || 'No se pudo cargar el catálogo'; this.loading = false; }
    });
    this.http.get<Coupling[]>('/api/vehicles/couplings').subscribe({
      next: value => this.history = value,
      error: () => this.history = []
    });
    this.http.get<DriverOption[]>('/api/vehicle-driver-assignments/drivers').subscribe({
      next: value => this.drivers = value,
      error: e => this.error = e?.error?.message || 'No se pudo cargar la lista de choferes'
    });
    this.http.get<DriverVehicleAssignment[]>('/api/vehicle-driver-assignments').subscribe({
      next: value => this.assignments = value,
      error: e => this.error = e?.error?.message || 'No se pudieron cargar las asignaciones'
    });
  }

  get filteredVehicles(): Vehicle[] {
    const term = this.searchText.trim().toLocaleLowerCase();
    return this.vehicles.filter(vehicle => {
      if (this.filterType !== 'TODOS' && vehicle.type !== this.filterType) return false;
      if (this.filterStatus === 'ACTIVOS' && !vehicle.active) return false;
      if (this.filterStatus === 'INACTIVOS' && vehicle.active) return false;
      if (term && ![vehicle.plate, vehicle.brand, vehicle.model, vehicle.capacityDescription]
        .some(value => (value || '').toLocaleLowerCase().includes(term))) return false;

      if (this.filterType === 'TRACTO' || this.filterType === 'CARRETA') {
        const coupled = vehicle.type === 'TRACTO' ? !!vehicle.currentTrailerPlate : !!vehicle.currentTractorPlate;
        if (this.filterRelation === 'VINCULADO' && !coupled) return false;
        if (this.filterRelation === 'SIN_VINCULO' && coupled) return false;
      }
      if (vehicle.type !== 'CARRETA') {
        const assigned = this.activeAssignments.some(a => a.vehicleId === vehicle.id);
        if (this.filterDriverRelation === 'VINCULADO' && !assigned) return false;
        if (this.filterDriverRelation === 'SIN_VINCULO' && assigned) return false;
        if (this.filterDriverId !== null && !this.activeAssignments.some(a => a.vehicleId === vehicle.id && a.driverId === this.filterDriverId)) return false;
      }
      return true;
    });
  }

  onFilterTypeChange(): void {
    this.filterRelation = 'TODOS';
    this.filterDriverRelation = 'TODOS';
    this.filterDriverId = null;
  }

  get tractors(): Vehicle[] { return this.vehicles.filter(v => v.type === 'TRACTO' && v.active); }
  get trailers(): Vehicle[] { return this.vehicles.filter(v => v.type === 'CARRETA' && v.active); }
  get assignableVehicles(): Vehicle[] { return this.vehicles.filter(v => v.active && v.type !== 'CARRETA'); }
  get activeAssignments(): DriverVehicleAssignment[] { return this.assignments.filter(a => !a.endAt); }
  get editingVehicle(): Vehicle | undefined { return this.vehicles.find(v => v.id === this.editingId); }

  vehicleAssignments(vehicleId: number): DriverVehicleAssignment[] {
    return this.activeAssignments.filter(a => a.vehicleId === vehicleId);
  }

  driversForVehicle(vehicleId: number): string {
    const names = this.vehicleAssignments(vehicleId).map(a => a.driverName);
    return names.length ? names.join(', ') : 'Sin chofer asignado';
  }

  relationSummary(vehicle: Vehicle): string {
    if (vehicle.type === 'TRACTO') return vehicle.currentTrailerPlate ? `Carreta ${vehicle.currentTrailerPlate}` : 'Sin acoplamiento vigente';
    if (vehicle.type === 'CARRETA') return vehicle.currentTractorPlate ? `Tracto ${vehicle.currentTractorPlate}` : 'Sin acoplamiento vigente';
    const names = this.driversForVehicle(vehicle.id);
    return names;
  }

  newVehicle(): void {
    this.editingId = null;
    this.form = this.empty();
    this.showForm = true;
    this.error = '';
  }

  edit(vehicle: Vehicle): void {
    this.editingId = vehicle.id;
    this.form = this.formFromVehicle(vehicle);
    this.showForm = true;
    this.error = '';
  }

  private formFromVehicle(vehicle: Vehicle): any {
    return {
      plate: vehicle.plate, type: vehicle.type, brand: vehicle.brand || '', model: vehicle.model || '',
      capacityDescription: vehicle.capacityDescription || '', active: vehicle.active, observations: vehicle.observations || ''
    };
  }

  save(closeAfterSave = false): void {
    const request = this.editingId
      ? this.http.put<Vehicle>(`/api/vehicles/${this.editingId}`, this.form)
      : this.http.post<Vehicle>('/api/vehicles', this.form);
    request.subscribe({
      next: saved => {
        this.editingId = saved.id;
        this.vehicles = [...this.vehicles.filter(vehicle => vehicle.id !== saved.id), saved]
          .sort((a, b) => a.type.localeCompare(b.type) || a.plate.localeCompare(b.plate));
        this.form = this.formFromVehicle(saved);
        this.success = this.showForm && !closeAfterSave
          ? 'Vehículo guardado. Ya puedes gestionar su acoplamiento y choferes asignados.'
          : 'Vehículo guardado';
        this.error = '';
        this.load();
        if (closeAfterSave) this.showForm = false;
      },
      error: e => this.error = e?.error?.message || 'No se pudo guardar'
    });
  }

  openDetail(vehicle: Vehicle): void {
    this.selectedVehicle = vehicle;
    this.showDetail = true;
  }

  private refreshSelectedVehicle(): void {
    if (this.selectedVehicle) this.selectedVehicle = this.vehicles.find(v => v.id === this.selectedVehicle?.id) || this.selectedVehicle;
  }

  openCoupling(vehicle?: Vehicle): void {
    this.coupling = this.emptyCoupling();
    if (vehicle?.type === 'TRACTO') this.coupling.tractorId = vehicle.id;
    if (vehicle?.type === 'CARRETA') this.coupling.trailerId = vehicle.id;
    this.showCoupling = true;
  }

  saveCoupling(): void {
    this.http.post('/api/vehicles/couplings', this.coupling).subscribe({
      next: () => {
        this.showCoupling = false;
        this.success = 'Acoplamiento registrado';
        this.error = '';
        this.load();
      },
      error: e => this.error = e?.error?.message || 'No se pudo registrar el acoplamiento'
    });
  }

  openAssignment(vehicle?: Vehicle): void {
    this.assignment = this.emptyAssignment();
    if (vehicle) this.assignment.vehicleId = vehicle.id;
    this.warnings = [];
    this.showAssignment = true;
  }

  saveAssignment(): void {
    this.http.post<DriverVehicleAssignment>('/api/vehicle-driver-assignments', this.assignment).subscribe({
      next: result => {
        this.warnings = result.warnings || [];
        this.showAssignment = false;
        this.success = 'Asignación chofer–vehículo registrada';
        this.error = '';
        this.load();
      },
      error: e => this.error = e?.error?.message || 'No se pudo registrar la asignación'
    });
  }

  closeAssignment(item: DriverVehicleAssignment): void {
    this.http.patch<DriverVehicleAssignment>(`/api/vehicle-driver-assignments/${item.id}/close`, {}).subscribe({
      next: () => { this.success = 'Asignación finalizada'; this.error = ''; this.load(); },
      error: e => this.error = e?.error?.message || 'No se pudo finalizar la asignación'
    });
  }
}
