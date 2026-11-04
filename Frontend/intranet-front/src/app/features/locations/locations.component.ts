import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ClientService } from '../../core/services/clients.service';
import { LocationsService } from '../../core/services/locations.service';
import { Client } from '../../core/models/clients.models';
import { ClientSubclient, ClientSubclientLocation, Location, LocationRequest, LocationType, LocationUsageType } from '../../core/models/locations.models';

@Component({
  selector: 'app-locations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './locations.component.html',
  styleUrl: './locations.component.scss'
})
export class LocationsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly clientsApi = inject(ClientService);
  private readonly api = inject(LocationsService);

  clients: Client[] = [];
  relations: ClientSubclient[] = [];
  locations: Location[] = [];
  relationLocations: ClientSubclientLocation[] = [];
  selectedClientId: number | null = null;
  selectedRelationId: number | null = null;
  canManage = false;
  loading = false;
  saving = false;
  error = '';
  success = '';
  showLocationForm = false;
  showAssociationForm = false;

  locationForm: LocationRequest = this.emptyLocation();
  association = { locationId: 0, usageType: 'AMBOS' as LocationUsageType, contactName: '', contactPhone: '', operatingHours: '', instructions: '' };

  readonly locationTypes: { value: LocationType; label: string }[] = [
    { value: 'ALMACEN', label: 'Almacén' }, { value: 'DEPOSITO', label: 'Depósito' },
    { value: 'PUERTO', label: 'Puerto' }, { value: 'PLANTA', label: 'Planta' },
    { value: 'CENTRO_DISTRIBUCION', label: 'Centro de distribución' }, { value: 'OTRO', label: 'Otro' }
  ];
  readonly usageTypes: { value: LocationUsageType; label: string }[] = [
    { value: 'RECOJO', label: 'Recojo' }, { value: 'ENTREGA', label: 'Entrega' }, { value: 'AMBOS', label: 'Ambos' }
  ];

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({
      next: user => {
        this.canManage = user.role === 'ADMIN' || user.role === 'SUPERVISOR';
        this.loadInitial();
      },
      error: () => this.error = 'No se pudo validar el usuario actual.'
    });
  }

  private loadInitial(): void {
    this.loading = true;
    forkJoin({
      clients: this.canManage ? this.clientsApi.getAllClients() : this.clientsApi.getActiveClients(),
      locations: this.canManage ? this.api.listAll() : this.api.listActive()
    }).subscribe({
      next: result => { this.clients = result.clients.filter(c => c.active); this.locations = result.locations; this.loading = false; },
      error: () => { this.error = 'No se pudieron cargar clientes y ubicaciones.'; this.loading = false; }
    });
  }

  onClientChange(): void {
    this.selectedRelationId = null; this.relations = []; this.relationLocations = [];
    if (!this.selectedClientId) return;
    this.api.listClientSubclients(this.selectedClientId).subscribe({
      next: rows => { this.relations = rows.filter(r => r.active); },
      error: () => this.error = 'No se pudieron cargar los subclientes del cliente seleccionado.'
    });
  }

  onRelationChange(): void {
    this.relationLocations = [];
    if (!this.selectedRelationId) return;
    this.api.listRelationLocations(this.selectedRelationId).subscribe({
      next: rows => this.relationLocations = rows,
      error: () => this.error = 'No se pudieron cargar las ubicaciones de esta relación.'
    });
  }

  createLocation(): void {
    if (!this.locationForm.name.trim() || !this.locationForm.address.trim()) return;
    this.saving = true; this.error = ''; this.success = '';
    this.api.create(this.locationForm).subscribe({
      next: created => {
        this.locations = [created, ...this.locations];
        this.locationForm = this.emptyLocation(); this.showLocationForm = false;
        this.association.locationId = created.id;
        this.success = 'Ubicación registrada. Ahora puedes asociarla a la relación seleccionada.';
        this.saving = false; this.showAssociationForm = !!this.selectedRelationId;
      },
      error: err => { this.error = err?.error?.message || 'No se pudo registrar la ubicación.'; this.saving = false; }
    });
  }

  addAssociation(): void {
    if (!this.selectedRelationId || !this.association.locationId) return;
    this.saving = true; this.error = ''; this.success = '';
    this.api.addRelationLocation(this.selectedRelationId, {
      locationId: this.association.locationId, usageType: this.association.usageType,
      contactName: this.nullable(this.association.contactName), contactPhone: this.nullable(this.association.contactPhone),
      operatingHours: this.nullable(this.association.operatingHours), instructions: this.nullable(this.association.instructions)
    }).subscribe({
      next: row => { this.relationLocations = [...this.relationLocations, row]; this.showAssociationForm = false; this.association = this.emptyAssociation(); this.success = 'Ubicación asociada correctamente.'; this.saving = false; },
      error: err => { this.error = err?.error?.message || 'No se pudo asociar la ubicación. Verifica que no esté asociada previamente.'; this.saving = false; }
    });
  }

  toggleAssociation(row: ClientSubclientLocation): void {
    this.api.changeRelationLocationStatus(row.id, !row.active).subscribe({
      next: updated => { this.relationLocations = this.relationLocations.map(x => x.id === updated.id ? updated : x); },
      error: () => this.error = 'No se pudo cambiar el estado de la asociación.'
    });
  }

  toggleLocation(row: Location): void {
    this.api.changeStatus(row.id, !row.active).subscribe({
      next: updated => this.locations = this.locations.map(x => x.id === updated.id ? updated : x),
      error: () => this.error = 'No se pudo cambiar el estado de la ubicación.'
    });
  }

  private nullable(value: string): string | null { const v = value.trim(); return v || null; }
  private emptyLocation(): LocationRequest {
    return { name: '', locationType: 'OTRO', address: '', reference: null, district: null, province: null, department: null, latitude: null, longitude: null };
  }
  private emptyAssociation() {
    return { locationId: 0, usageType: 'AMBOS' as LocationUsageType, contactName: '', contactPhone: '', operatingHours: '', instructions: '' };
  }
}
