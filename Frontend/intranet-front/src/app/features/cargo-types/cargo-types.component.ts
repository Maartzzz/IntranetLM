import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/auth/auth.service';
import { CargoType } from '../../core/models/cargo-types.models';
import { CargoTypeService } from '../../core/services/cargo-types.service';
import { CargoTypeFormDialogComponent } from './cargo-type-form-dialog/cargo-type-form-dialog.component';

@Component({
  selector: 'app-cargo-types',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './cargo-types.component.html',
  styleUrl: './cargo-types.component.scss'
})
export class CargoTypesComponent implements OnInit {
  private readonly cargoTypeService = inject(CargoTypeService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);

  cargoTypes: CargoType[] = [];
  searchTerm = '';
  selectedStatus: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ACTIVE';
  selectedKind: 'ALL' | 'CONTAINER' | 'LOOSE' = 'ALL';
  loading = false;
  errorMessage = '';
  canManage = false;

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe({
      next: user => {
        this.canManage = user.role === 'ADMIN';
        this.loadCargoTypes();
      },
      error: () => this.errorMessage = 'No se pudo validar el usuario actual.'
    });
  }

  loadCargoTypes(): void {
    this.loading = true;
    this.errorMessage = '';
    const request = this.canManage
      ? this.cargoTypeService.getAllCargoTypes()
      : this.cargoTypeService.getActiveCargoTypes();

    request.subscribe({
      next: items => { this.cargoTypes = items; this.loading = false; },
      error: error => {
        console.error('Error cargando tipos de carga:', error);
        this.errorMessage = 'No se pudieron cargar los tipos de carga.';
        this.loading = false;
      }
    });
  }

  get filteredCargoTypes(): CargoType[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.cargoTypes.filter(item => {
      const matchesSearch = !search || item.name.toLowerCase().includes(search);
      const matchesStatus = this.selectedStatus === 'ALL'
        || (this.selectedStatus === 'ACTIVE' && item.active)
        || (this.selectedStatus === 'INACTIVE' && !item.active);
      const matchesKind = this.selectedKind === 'ALL'
        || (this.selectedKind === 'CONTAINER' && item.container)
        || (this.selectedKind === 'LOOSE' && !item.container);
      return matchesSearch && matchesStatus && matchesKind;
    });
  }

  openCreateDialog(): void { this.openForm({ mode: 'create' }); }
  openEditDialog(item: CargoType): void { this.openForm({ mode: 'edit', cargoType: item }); }

  private openForm(data: { mode: 'create' | 'edit'; cargoType?: CargoType }): void {
    this.dialog.open(CargoTypeFormDialogComponent, {
      width: '500px', maxWidth: 'calc(100vw - 32px)', data
    }).afterClosed().subscribe(result => { if (result) this.loadCargoTypes(); });
  }

  toggleActive(item: CargoType): void {
    const nextStatus = !item.active;
    const action = nextStatus ? 'activar' : 'desactivar';
    if (!window.confirm(`¿Deseas ${action} el tipo de carga "${item.name}"?`)) return;

    this.cargoTypeService.changeStatus(item.id, nextStatus).subscribe({
      next: updated => {
        const index = this.cargoTypes.findIndex(value => value.id === updated.id);
        if (index >= 0) this.cargoTypes[index] = updated;
      },
      error: error => {
        console.error('Error cambiando estado del tipo de carga:', error);
        this.errorMessage = 'No se pudo cambiar el estado del tipo de carga.';
      }
    });
  }
}
