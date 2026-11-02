import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { OperationsService } from '../../../core/services/operations.service';
import {
  RequirementCatalog,
  RequirementCatalogRequest
} from '../../../core/models/operations.models';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-requirement-catalog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './requirement-catalog.component.html',
  styleUrl: './requirement-catalog.component.scss'
})
export class RequirementCatalogComponent implements OnInit {
  private readonly service = inject(OperationsService);
  private readonly authService = inject(AuthService);

  requirements: RequirementCatalog[] = [];

  canManage = false;
  loading = false;
  saving = false;

  searchTerm = '';
  statusFilter: 'ALL' | 'ACTIVE' | 'INACTIVE' = 'ALL';

  showForm = false;
  editingId: number | null = null;

  form = {
    name: '',
    description: '',
    active: true
  };

  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.authService.getCurrentUser().subscribe({
      next: user => {
        this.canManage =
          user?.role === 'ADMIN' || user?.role === 'SUPERVISOR';
      },
      error: error => console.error('Error al consultar usuario:', error)
    });

    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    this.service.getRequirementCatalog(false).subscribe({
      next: data => {
        this.requirements = data;
        this.loading = false;
      },
      error: error => {
        console.error(error);
        this.errorMessage = 'No se pudo cargar el catálogo de requisitos.';
        this.loading = false;
      }
    });
  }

  get filteredRequirements(): RequirementCatalog[] {
    const term = this.searchTerm.trim().toLowerCase();

    return this.requirements.filter(item => {
      const matchesText =
        !term ||
        item.name.toLowerCase().includes(term) ||
        (item.description ?? '').toLowerCase().includes(term);

      const matchesStatus =
        this.statusFilter === 'ALL' ||
        (this.statusFilter === 'ACTIVE' && item.active) ||
        (this.statusFilter === 'INACTIVE' && !item.active);

      return matchesText && matchesStatus;
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = {
      name: '',
      description: '',
      active: true
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  openEdit(item: RequirementCatalog): void {
    this.editingId = item.id;
    this.form = {
      name: item.name,
      description: item.description ?? '',
      active: item.active
    };

    this.errorMessage = '';
    this.successMessage = '';
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingId = null;
  }

  save(): void {
    if (!this.canManage || this.saving) return;

    const name = this.form.name.trim();

    if (!name) {
      this.errorMessage = 'El nombre del requisito es obligatorio.';
      return;
    }

    const request: RequirementCatalogRequest = {
      name,
      description: this.form.description.trim() || null,
      active: this.form.active
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const operation = this.editingId
      ? this.service.updateRequirementCatalog(this.editingId, request)
      : this.service.createRequirementCatalog(request);

    operation.subscribe({
      next: () => {
        this.saving = false;
        this.showForm = false;
        this.editingId = null;
        this.successMessage = 'El requisito se guardó correctamente.';
        this.load();
      },
      error: error => {
        console.error(error);
        this.saving = false;

        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          'No se pudo guardar el requisito.';
      }
    });
  }

  toggleStatus(item: RequirementCatalog): void {
    if (!this.canManage) return;

    const request: RequirementCatalogRequest = {
      name: item.name,
      description: item.description,
      active: !item.active
    };

    this.service.updateRequirementCatalog(item.id, request).subscribe({
      next: () => {
        this.successMessage = item.active
          ? 'Requisito desactivado correctamente.'
          : 'Requisito activado correctamente.';

        this.load();
      },
      error: error => {
        console.error(error);
        this.errorMessage =
          error.error?.message ||
          error.error?.detail ||
          'No se pudo cambiar el estado del requisito.';
      }
    });
  }
}