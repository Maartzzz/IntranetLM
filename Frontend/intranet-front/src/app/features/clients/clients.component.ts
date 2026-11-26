import { Component, OnInit, inject } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/auth/auth.service';
import { Client } from '../../core/models/clients.models';
import { ClientService } from '../../core/services/clients.service';
import { LocationsService } from '../../core/services/locations.service';
import {
  ClientSubclient,
  ClientSubclientLocation,
  LocationType,
  LocationUsageType,
  Location
} from '../../core/models/locations.models';
import { ClientFormDialogComponent } from './client-form-dialog/client-form-dialog.component';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.scss'
})
export class ClientsComponent implements OnInit {
  private readonly clientsApi = inject(ClientService);
  private readonly auth = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly api = inject(LocationsService);

  clients: Client[] = [];
  relations: ClientSubclient[] = [];
  addresses: ClientSubclientLocation[] = [];
  availableLocations: Location[] = [];
  selectedLocationId: number | null = null;
  selected: Client | null = null;
  selectedRelation: ClientSubclient | null = null;

  search = '';
  relationSearch = '';
  busy = false;
  canManage = false;
  error = '';
  success = '';
  addRelationOpen = false;
  addAddressOpen = false;
  subclientId: number | null = null;
  pendingLocationId: number | null = null;

  address = {
    name: '',
    locationType: 'OTRO' as LocationType,
    streetAddress: '',
    reference: '',
    district: '',
    province: '',
    department: '',
    usageType: 'AMBOS' as LocationUsageType,
    contactName: '',
    contactPhone: '',
    operatingHours: '',
    instructions: ''
  };

  get filteredClients(): Client[] {
    const query = this.search.toLowerCase().trim();
    return this.clients.filter(client => client.active &&
      (!query || `${client.businessName} ${client.documentNumber}`.toLowerCase().includes(query)));
  }

  get available(): Client[] {
    const query = this.relationSearch.toLowerCase().trim();
    return this.clients.filter(client => client.active &&
      (!query || `${client.businessName} ${client.documentNumber}`.toLowerCase().includes(query)));
  }

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe({
      next: user => {
        this.canManage = this.auth.hasAnyPermission('CLIENTS.CREATE', 'CLIENTS.EDIT', 'CLIENTS.DELETE');
        const request = this.canManage
          ? this.clientsApi.getAllClients()
          : this.clientsApi.getActiveClients();
        request.subscribe({
          next: clients => this.clients = clients,
          error: () => this.error = 'No se pudieron cargar las empresas.'
        });
      },
      error: () => this.error = 'No se pudo validar el usuario.'
    });
  }

  selectClient(client: Client): void {
    this.selected = client;
    this.selectedRelation = null;
    this.addresses = [];
    this.addRelationOpen = false;
    this.addAddressOpen = false;
    this.error = '';
    this.success = '';
    this.api.listClientSubclients(client.id).subscribe({
      next: relations => this.relations = relations,
      error: () => this.error = 'No se pudieron cargar las empresas relacionadas.'
    });
  }

  selectRelation(relation: ClientSubclient): void {
    this.selectedRelation = relation;
    this.addAddressOpen = false;
    this.addresses = [];
    this.availableLocations = [];
    this.selectedLocationId = null;
    this.error = '';
    const otherClientId = this.relatedClientId(relation);
    const currentId = this.selected?.id;
    if (!currentId || !otherClientId) return;
    forkJoin({ current: this.clientsApi.getClientLocations(currentId), other: this.clientsApi.getClientLocations(otherClientId), addresses: this.canManage ? this.api.listAllRelationLocations(relation.id) : this.api.listRelationLocations(relation.id) }).subscribe({
      next: result => {
        this.availableLocations = [...result.current, ...result.other].filter((location, index, all) => location.active && all.findIndex(x => x.id === location.id) === index);
        this.addresses = result.addresses;
      },
      error: () => this.error = 'No se pudieron cargar las ubicaciones de las empresas relacionadas.'
    });
  }

  relatedClientId(relation: ClientSubclient): number {
    if (!this.selected) return relation.subclientId;
    return relation.clientId === this.selected.id ? relation.subclientId : relation.clientId;
  }

  relatedClientName(relation: ClientSubclient): string {
    if (!this.selected) return relation.subclientName;
    return relation.clientId === this.selected.id ? relation.subclientName : relation.clientName;
  }

  locationOwnerName(locationId: number): string {
    const owner = this.availableLocations.find(location => location.id === locationId);
    if (!owner || !this.selected || !this.selectedRelation) return 'Empresa relacionada';
    // El selector carga únicamente ubicaciones de los dos extremos de la relación.
    return owner ? (this.selected.businessName + ' / ' + this.relatedClientName(this.selectedRelation)) : 'Empresa relacionada';
  }

  createClient(): void {
    this.dialog.open(ClientFormDialogComponent, {
      width: '1100px',
      maxWidth: '96vw',
      maxHeight: '92vh',
      data: { mode: 'create' }
    }).afterClosed().subscribe((client?: Client) => {
      if (!client) return;
      this.upsertClient(client);
      this.success = 'Empresa registrada.';
    });
  }

  /** Registra una empresa desde el contexto actual y la vincula inmediatamente. */
  createRelatedClient(): void {
    if (!this.selected || this.busy) return;

    this.dialog.open(ClientFormDialogComponent, {
      width: '1100px',
      maxWidth: '96vw',
      maxHeight: '92vh',
      data: { mode: 'create' }
    }).afterClosed().subscribe((client?: Client) => {
      if (!client || !this.selected) return;
      this.upsertClient(client);
      this.busy = true;
      this.api.addSubclient(this.selected.id, client.id).subscribe({
        next: relation => {
          this.relations = [...this.relations.filter(item => item.id !== relation.id), relation];
          this.selectedRelation = relation;
          this.addresses = [];
          this.addRelationOpen = false;
          this.success = 'Empresa registrada y agregada a las empresas relacionadas.';
          this.error = '';
          this.busy = false;
        },
        error: error => {
          this.error = error?.error?.message ||
            'La empresa se registró, pero no se pudo agregar como relacionada. Puedes seleccionarla e intentarlo nuevamente.';
          this.busy = false;
        }
      });
    });
  }

  editClient(): void {
    if (!this.selected) return;
    this.dialog.open(ClientFormDialogComponent, {
      width: '1100px',
      maxWidth: '96vw',
      maxHeight: '92vh',
      data: { mode: 'edit', client: this.selected }
    }).afterClosed().subscribe((client?: Client) => {
      if (!client) return;
      this.upsertClient(client);
      this.selected = client;
    });
  }

  addRelation(): void {
    if (!this.selected || this.subclientId === null || this.busy) return;
    this.busy = true;
    this.api.addSubclient(this.selected.id, this.subclientId).subscribe({
      next: relation => {
        this.relations = [...this.relations.filter(item => item.id !== relation.id), relation];
        this.subclientId = null;
        this.addRelationOpen = false;
        this.selectedRelation = relation;
        this.addresses = [];
        this.success = 'Empresa relacionada agregada.';
        this.error = '';
        this.busy = false;
      },
      error: error => {
        this.error = error?.error?.message || 'No se pudo agregar la relación.';
        this.busy = false;
      }
    });
  }

  relationStatus(relation: ClientSubclient): void {
    this.api.changeRelationStatus(relation.id, !relation.active).subscribe({
      next: updated => this.relations = this.relations.map(item => item.id === updated.id ? updated : item),
      error: () => this.error = 'No se pudo actualizar la relación.'
    });
  }

  /** Asocia una ubicación ya registrada en cualquiera de las dos empresas. */
  saveAddress(): void {
    if (!this.selectedRelation || this.busy || this.selectedLocationId === null) return;
    this.busy = true; this.error = ''; this.success = '';
    this.linkAddress(this.selectedLocationId);
  }

  private linkAddress(locationId: number): void {
    if (!this.selectedRelation) {
      this.busy = false;
      return;
    }

    this.api.addRelationLocation(this.selectedRelation.id, {
      locationId,
      usageType: this.address.usageType,
      contactName: this.address.contactName.trim() || null,
      contactPhone: this.address.contactPhone.trim() || null,
      operatingHours: this.address.operatingHours.trim() || null,
      instructions: this.address.instructions.trim() || null
    }).subscribe({
      next: relationAddress => {
        this.addresses = [...this.addresses.filter(item => item.id !== relationAddress.id), relationAddress];
        this.pendingLocationId = null;
        this.selectedLocationId = null;
        this.addAddressOpen = false;
        this.resetAddressForm();
        this.success = 'Dirección registrada y asociada correctamente.';
        this.error = '';
        this.busy = false;
      },
      error: error => {
        this.error = error?.error?.message ||
          'No se pudo asociar la ubicación seleccionada. Verifica que pertenezca a una de las dos empresas de esta relación.';
        this.busy = false;
      }
    });
  }

  addressStatus(address: ClientSubclientLocation): void {
    this.api.changeRelationLocationStatus(address.id, !address.active).subscribe({
      next: updated => this.addresses = this.addresses.map(item => item.id === updated.id ? updated : item),
      error: () => this.error = 'No se pudo cambiar el estado de la dirección.'
    });
  }

  cancelAddress(): void {
    this.addAddressOpen = false;
    this.pendingLocationId = null;
    this.selectedLocationId = null;
    this.resetAddressForm();
  }

  private resetAddressForm(): void {
    this.address = {
      name: '',
      locationType: 'OTRO',
      streetAddress: '',
      reference: '',
      district: '',
      province: '',
      department: '',
      usageType: 'AMBOS',
      contactName: '',
      contactPhone: '',
      operatingHours: '',
      instructions: ''
    };
  }

  private upsertClient(client: Client): void {
    this.clients = [client, ...this.clients.filter(item => item.id !== client.id)];
  }
}
