import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/auth/auth.service';
import { Client } from '../../core/models/clients.models';
import { ClientService } from '../../core/services/clients.service';
import { ClientFormDialogComponent } from './client-form-dialog/client-form-dialog.component';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.scss'
})
export class ClientsComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  clients: Client[] = [];
  searchTerm = '';
  selectedStatus: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ACTIVE';
  loading = false;
  errorMessage = '';
  canManage = false;

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe({
      next: user => {
        this.canManage = user.role === 'ADMIN' || user.role === 'SUPERVISOR';
        this.loadClients();
      },
      error: () => {
        this.errorMessage = 'No se pudo validar el usuario actual.';
      }
    });
  }

  loadClients(): void {
    this.loading = true;
    this.errorMessage = '';

    const request = this.canManage
      ? this.clientService.getAllClients()
      : this.clientService.getActiveClients();

    request.subscribe({
      next: clients => {
        this.clients = clients;
        this.loading = false;
      },
      error: error => {
        console.error('Error cargando clientes:', error);
        this.errorMessage = 'No se pudieron cargar los clientes.';
        this.loading = false;
      }
    });
  }

  get filteredClients(): Client[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.clients.filter(client => {
      const matchesSearch = !search
        || client.businessName.toLowerCase().includes(search)
        || client.documentNumber.toLowerCase().includes(search)
        || client.documentType.toLowerCase().includes(search);

      const matchesStatus = this.selectedStatus === 'ALL'
        || (this.selectedStatus === 'ACTIVE' && client.active)
        || (this.selectedStatus === 'INACTIVE' && !client.active);

      return matchesSearch && matchesStatus;
    });
  }

  openCreateDialog(): void {
    this.openForm({ mode: 'create' });
  }

  openEditDialog(client: Client): void {
    this.openForm({ mode: 'edit', client });
  }

  private openForm(data: { mode: 'create' | 'edit'; client?: Client }): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '560px',
      maxWidth: 'calc(100vw - 32px)',
      data
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadClients();
      }
    });
  }

  toggleActive(client: Client): void {
    const nextStatus = !client.active;
    const action = nextStatus ? 'activar' : 'desactivar';

    if (!window.confirm(`¿Deseas ${action} al cliente "${client.businessName}"?`)) {
      return;
    }

    this.clientService.changeStatus(client.id, nextStatus).subscribe({
      next: updated => {
        const index = this.clients.findIndex(item => item.id === updated.id);
        if (index >= 0) {
          this.clients[index] = updated;
        }
      },
      error: error => {
        console.error('Error cambiando estado del cliente:', error);
        this.errorMessage = 'No se pudo cambiar el estado del cliente.';
      }
    });
  }
}
