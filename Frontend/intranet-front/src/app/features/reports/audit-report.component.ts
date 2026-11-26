import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuditLog } from '../../core/models/audit-log.models';
import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-audit-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './audit-report.component.html',
  styleUrl: './audit-report.component.scss'
})
export class AuditReportComponent implements OnInit {
  private readonly auditService = inject(AuditService);
  private readonly authService = inject(AuthService);

  get canExport(): boolean { return this.authService.hasPermission('AUDIT.EXPORT'); }

  records: AuditLog[] = [];
  loading = false;
  errorMessage = '';
  searchTerm = '';
  moduleFilter = '';
  entityTypeFilter = '';
  actionFilter = '';
  actorFilter = '';
  dateFrom = '';
  dateTo = '';
  page = 1;
  pageSize = 20;
  expandedId: number | null = null;

  readonly modules = [
    { value: 'OPERACIONES', label: 'Operaciones' },
    { value: 'OS_CLIENTE', label: 'O/S cliente' },
    { value: 'OS_INTERNA', label: 'O/S interna' },
    { value: 'USUARIOS', label: 'Usuarios' },
    { value: 'CONDUCTORES', label: 'Conductores' },
    { value: 'PROGRAMACION', label: 'Programación' },
    { value: 'APRENDIZAJE', label: 'Aprendizaje' },
    { value: 'SEGURIDAD', label: 'Seguridad' }
  ];

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.auditService.getAudit({
      module: this.moduleFilter || undefined
    }).subscribe({
      next: rows => {
        this.records = rows ?? [];
        this.page = 1;
        this.loading = false;
      },
      error: error => {
        console.error('Error cargando el reporte de auditoría:', error);
        this.errorMessage = error.status === 403
          ? 'No tienes permisos para consultar el reporte de auditoría.'
          : 'No se pudo cargar el reporte de auditoría.';
        this.loading = false;
      }
    });
  }

  get filteredRecords(): AuditLog[] {
    const term = this.searchTerm.trim().toLowerCase();
    const actor = this.actorFilter.trim().toLowerCase();
    const from = this.dateFrom ? new Date(`${this.dateFrom}T00:00:00`) : null;
    const to = this.dateTo ? new Date(`${this.dateTo}T23:59:59.999`) : null;

    return this.records.filter(row => {
      const searchable = [row.description, row.actorUsername, row.actorName,
        row.module, row.entityType, row.entityId, row.entityKey, row.action]
        .filter(value => value !== null && value !== undefined).join(' ').toLowerCase();
      const created = new Date(row.createdAt);
      return (!term || searchable.includes(term))
        && (!this.entityTypeFilter || row.entityType === this.entityTypeFilter)
        && (!actor || `${row.actorUsername} ${row.actorName}`.toLowerCase().includes(actor))
        && (!this.actionFilter || row.action === this.actionFilter)
        && (!from || created >= from)
        && (!to || created <= to);
    });
  }

  get pagedRecords(): AuditLog[] {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredRecords.slice(start, start + this.pageSize);
  }

  get totalPages(): number { return Math.max(1, Math.ceil(this.filteredRecords.length / this.pageSize)); }
  get rangeStart(): number { return this.filteredRecords.length ? (this.page - 1) * this.pageSize + 1 : 0; }
  get rangeEnd(): number { return Math.min(this.page * this.pageSize, this.filteredRecords.length); }

  applyFilters(): void { this.page = 1; this.load(); }

  clearFilters(): void {
    this.searchTerm = '';
    this.moduleFilter = '';
    this.entityTypeFilter = '';
    this.actionFilter = '';
    this.actorFilter = '';
    this.dateFrom = '';
    this.dateTo = '';
    this.page = 1;
    this.load();
  }

  changePage(delta: number): void {
    this.page = Math.min(this.totalPages, Math.max(1, this.page + delta));
  }

  toggleDetail(id: number): void { this.expandedId = this.expandedId === id ? null : id; }

  identifier(row: AuditLog): string {
    if (row.entityId !== null && row.entityId !== undefined) return String(row.entityId);
    return row.entityKey || '—';
  }

  moduleLabel(module: string): string {
    return this.modules.find(item => item.value === module)?.label ?? module.replaceAll('_', ' ');
  }

  actionLabel(action: string): string {
    const labels: Record<string, string> = {
      CREATED: 'Creación', UPDATED: 'Actualización', STATUS_CHANGED: 'Cambio de estado',
      ACTIVATED: 'Activación', DEACTIVATED: 'Desactivación', SCHEDULED: 'Programación',
      RESCHEDULED: 'Reprogramación', REMOVED_FROM_SCHEDULE: 'Retiro de programación',
      VERSION_CREATED: 'Versión creada', PROGRAMACION_INICIAL: 'Programación inicial',
      REPROGRAMACION: 'Reprogramación', REMOVED_FROM_BOARD: 'Retiro del tablero', DELETED: 'Eliminación',
      CANCELLED: 'Cancelación', ASSIGNED: 'Asignación', REMOVED: 'Retiro', IMPORTED: 'Importación',
      STARTED: 'Inicio de evaluación', PASSED: 'Evaluación aprobada', FAILED: 'Evaluación no aprobada',
      REHABILITATED: 'Rehabilitación', LOGIN_SUCCESS: 'Inicio de sesión', LOGIN_FAILURE: 'Acceso fallido'
    };
    return labels[action] ?? action.replaceAll('_', ' ');
  }

  valueForDisplay(value: string | null): string {
    if (!value) return 'Sin información';
    try { return JSON.stringify(JSON.parse(value), null, 2); }
    catch { return value; }
  }

  exportCsv(): void {
    if (!this.canExport) return;
    const headers = ['Fecha y hora', 'Usuario', 'Módulo', 'Entidad', 'Identificador', 'Acción', 'Descripción', 'Valor anterior', 'Valor nuevo'];
    const rows = this.filteredRecords.map(row => [row.createdAt, `${row.actorName} (${row.actorUsername})`,
      this.moduleLabel(row.module), row.entityType, this.identifier(row), this.actionLabel(row.action),
      row.description, row.oldValue ?? '', row.newValue ?? '']);
    const csv = [headers, ...rows].map(row => row.map(value => `"${String(value ?? '').replaceAll('"', '""')}"`).join(';')).join('\r\n');
    const blob = new Blob(['\ufeff', csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `reporte-auditoria-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }
}
