import { Component, OnInit, inject } from '@angular/core';
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
  LocationUsageType
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
        this.canManage = user.role === 'ADMIN' || user.role === 'SUPERVISOR';
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
    this.error = '';
    this.api.listRelationLocations(relation.id).subscribe({
      next: addresses => this.addresses = addresses,
      error: () => this.error = 'No se pudieron cargar las direcciones.'
    });
  }

  createClient(): void {
    this.dialog.open(ClientFormDialogComponent, {
      width: '560px',
      maxWidth: 'calc(100vw - 32px)',
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
      width: '560px',
      maxWidth: 'calc(100vw - 32px)',
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
      width: '560px',
      maxWidth: 'calc(100vw - 32px)',
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

  /** Crea la dirección física y la asocia en una sola acción para el usuario. */
  saveAddress(): void {
    if (!this.selectedRelation || this.busy) return;
    if (!this.address.name.trim() || !this.address.streetAddress.trim()) return;

    this.busy = true;
    this.error = '';
    this.success = '';

    if (this.pendingLocationId !== null) {
      this.linkAddress(this.pendingLocationId);
      return;
    }

    this.api.create({
      name: this.address.name.trim(),
      locationType: this.address.locationType,
      address: this.address.streetAddress.trim(),
      reference: this.address.reference.trim() || null,
      district: this.address.district.trim() || null,
      province: this.address.province.trim() || null,
      department: this.address.department.trim() || null,
      latitude: null,
      longitude: null
    }).subscribe({
      next: location => {
        this.pendingLocationId = location.id;
        this.linkAddress(location.id);
      },
      error: error => {
        this.error = error?.error?.message || 'No se pudo guardar la dirección.';
        this.busy = false;
      }
    });
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
        this.addresses = [...this.addresses, relationAddress];
        this.pendingLocationId = null;
        this.addAddressOpen = false;
        this.resetAddressForm();
        this.success = 'Dirección registrada y asociada correctamente.';
        this.error = '';
        this.busy = false;
      },
      error: error => {
        this.error = error?.error?.message ||
          'La dirección se guardó, pero no se pudo asociar. Pulsa “Reintentar guardado” para completar el registro sin duplicarla.';
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
