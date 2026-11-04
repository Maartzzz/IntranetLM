import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ClientFormDialogComponent } from '../clients/client-form-dialog/client-form-dialog.component';
import { AuthService } from '../../core/auth/auth.service';
import { Client } from '../../core/models/clients.models';
import { ClientSubclient } from '../../core/models/client-subclients.models';
import { ClientService } from '../../core/services/clients.service';
import { ClientSubclientsService } from '../../core/services/client-subclients.service';

@Component({
  selector: 'app-subclients',
  standalone: true,
  imports: [FormsModule, MatDialogModule],
  templateUrl: './subclients.component.html',
  styleUrl: './subclients.component.scss'
})
export class SubclientsComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly relationService = inject(ClientSubclientsService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  clients: Client[] = [];
  relations: ClientSubclient[] = [];
  selectedClientId: number | null = null;
  selectedSubclientId: number | null = null;
  searchTerm = '';
  loadingClients = false;
  loadingRelations = false;
  saving = false;
  canManage = false;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe({
      next: user => {
        this.canManage = user.role === 'ADMIN' || user.role === 'SUPERVISOR';
        this.loadClients();
      },
      error: () => this.errorMessage = 'No se pudo validar el usuario actual.'
    });
  }

  loadClients(): void {
    this.loadingClients = true;
    this.errorMessage = '';
    const request = this.canManage
      ? this.clientService.getAllClients()
      : this.clientService.getActiveClients();

    request.subscribe({
      next: clients => {
        this.clients = clients.filter(client => client.active);
        this.loadingClients = false;
        if (this.clients.length > 0) {
          this.selectedClientId = this.clients[0].id;
          this.loadRelations();
        } else {
          this.relations = [];
        }
      },
      error: error => {
        console.error('Error cargando clientes:', error);
        this.errorMessage = 'No se pudieron cargar los clientes.';
        this.loadingClients = false;
      }
    });
  }

  onClientChange(): void {
    this.selectedSubclientId = null;
    this.searchTerm = '';
    this.successMessage = '';
    this.loadRelations();
  }

  loadRelations(): void {
    if (this.selectedClientId === null) {
      this.relations = [];
      return;
    }

    this.loadingRelations = true;
    this.errorMessage = '';
    this.relationService.getByClient(this.selectedClientId).subscribe({
      next: relations => {
        this.relations = relations;
        this.loadingRelations = false;
      },
      error: error => {
        console.error('Error cargando subclientes:', error);
        this.errorMessage = 'No se pudieron cargar los subclientes de este cliente.';
        this.loadingRelations = false;
      }
    });
  }

  get availableSubclients(): Client[] {
    const linkedIds = new Set(this.relations.map(relation => relation.subclientId));
    const search = this.searchTerm.trim().toLocaleLowerCase();

    return this.clients.filter(client =>
      !linkedIds.has(client.id)
      && (!search
        || client.businessName.toLocaleLowerCase().includes(search)
        || client.documentNumber.toLocaleLowerCase().includes(search))
    );
  }

  get selectedClient(): Client | undefined {
    return this.clients.find(client => client.id === this.selectedClientId);
  }

  getSubclientDocument(subclientId: number): string {
    const client = this.clients.find(item => item.id === subclientId);
    return client ? `${client.documentType} ${client.documentNumber}` : '—';
  }

  openCreateCompany(): void {
    if (!this.canManage) return;
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '560px',
      maxWidth: '95vw',
      data: { mode: 'create' }
    });
    dialogRef.afterClosed().subscribe((created: Client | undefined) => {
      if (!created) return;
      this.clients = [...this.clients.filter(item => item.id !== created.id), created];
      this.selectedSubclientId = created.id;
      this.searchTerm = '';
      this.addSubclient();
    });
  }

  addSubclient(): void {
    if (!this.canManage || this.selectedClientId === null || this.selectedSubclientId === null) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.relationService.create(this.selectedClientId, { subclientId: this.selectedSubclientId }).subscribe({
      next: relation => {
        this.relations = [...this.relations.filter(item => item.id !== relation.id), relation]
          .sort((a, b) => a.subclientName.localeCompare(b.subclientName));
        this.selectedSubclientId = null;
        this.searchTerm = '';
        this.successMessage = 'La relación cliente-subcliente se registró correctamente.';
        this.saving = false;
      },
      error: error => {
        console.error('Error registrando subcliente:', error);
        this.errorMessage = error?.error?.detail
          || error?.error?.message
          || 'No se pudo registrar la relación. Verifica que ambas empresas estén activas y que la relación no exista.';
        this.saving = false;
      }
    });
  }

  deactivateRelation(relation: ClientSubclient): void {
    if (!this.canManage || !window.confirm(`¿Deseas retirar a "${relation.subclientName}" de los subclientes de este cliente?`)) {
      return;
    }

    this.relationService.changeStatus(relation.id, false).subscribe({
      next: () => {
        this.relations = this.relations.filter(item => item.id !== relation.id);
        this.successMessage = 'La relación fue desactivada. La empresa seguirá registrada como cliente.';
      },
      error: error => {
        console.error('Error desactivando relación:', error);
        this.errorMessage = error?.error?.detail || 'No se pudo desactivar la relación.';
      }
    });
  }
}
