import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RolesService, RoleItem, RolePermission, RolePayload } from '../../core/services/roles.service';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-roles', standalone: true, imports: [FormsModule],
  templateUrl: './roles.component.html', styleUrl: './roles.component.scss'
})
export class RolesComponent implements OnInit {
  private readonly service = inject(RolesService);
  private readonly authService = inject(AuthService);
  roles: RoleItem[] = [];
  permissions: RolePermission[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editing: RoleItem | null = null;
  formOpen = false;
  code = ''; name = ''; description = '';
  selected = new Set<number>();

  get canCreate(): boolean { return this.authService.hasPermission('ROLES.CREATE'); }
  get canEdit(): boolean { return this.authService.hasPermission('ROLES.EDIT'); }
  get canChangeStatus(): boolean { return this.authService.hasPermission('ROLES.DELETE'); }

  ngOnInit(): void { this.load(); }
  load(): void {
    this.loading = true; this.error = '';
    this.service.getRoles().subscribe({
      next: rows => { this.roles = rows; this.loading = false; },
      error: () => { this.error = 'No se pudieron cargar los roles.'; this.loading = false; }
    });
    this.service.getPermissions().subscribe({
      next: rows => this.permissions = rows,
      error: () => this.error = 'No se pudo cargar el catálogo de permisos.'
    });
  }
  get modules(): { code: string; name: string; permissions: RolePermission[] }[] {
    const groups = new Map<string, { code: string; name: string; permissions: RolePermission[] }>();
    for (const permission of this.permissions) {
      if (!groups.has(permission.moduleCode)) groups.set(permission.moduleCode, { code: permission.moduleCode, name: permission.moduleName, permissions: [] });
      groups.get(permission.moduleCode)!.permissions.push(permission);
    }
    return [...groups.values()];
  }
  openCreate(): void { if (!this.canCreate) return; this.editing = null; this.code = ''; this.name = ''; this.description = ''; this.selected.clear(); this.formOpen = true; this.success = ''; }
  openEdit(role: RoleItem): void { if (!this.canEdit || role.protectedRole) return; this.editing = role; this.code = role.code; this.name = role.name; this.description = role.description || ''; this.selected = new Set(role.permissionIds); this.formOpen = true; this.success = ''; }
  isSelected(id: number): boolean { return this.selected.has(id); }
  togglePermission(id: number, checked: boolean): void { checked ? this.selected.add(id) : this.selected.delete(id); }
  moduleSelected(code: string): boolean { const group = this.modules.find(m => m.code === code); return !!group?.permissions.length && group.permissions.every(p => this.selected.has(p.id)); }
  toggleModule(code: string, checked: boolean): void {
    const group = this.modules.find(m => m.code === code); if (!group) return;
    for (const p of group.permissions) checked ? this.selected.add(p.id) : this.selected.delete(p.id);
  }
  save(): void {
    if (this.editing ? !this.canEdit : !this.canCreate) return;
    if (!this.name.trim()) { this.error = 'Ingresa el nombre del rol.'; return; }
    if (!this.editing && !this.code.trim()) { this.error = 'Ingresa el código del rol.'; return; }
    const payload: RolePayload = { name: this.name.trim(), description: this.description.trim(), permissionIds: [...this.selected] };
    if (!this.editing) payload.code = this.code.trim().toUpperCase();
    this.saving = true; this.error = '';
    const request = this.editing ? this.service.update(this.editing.id, payload) : this.service.create(payload);
    request.subscribe({
      next: () => { this.saving = false; this.formOpen = false; this.success = 'Rol guardado correctamente.'; this.load(); },
      error: err => { this.saving = false; this.error = err?.error?.message || 'No se pudo guardar el rol.'; }
    });
  }
  toggleActive(role: RoleItem): void {
    if (!this.canChangeStatus || role.protectedRole) return;
    const action = role.active ? 'desactivar' : 'activar';
    if (!confirm(`¿Deseas ${action} el rol ${role.name}?`)) return;
    this.service.setActive(role.id, !role.active).subscribe({
      next: () => { this.success = `Rol ${role.active ? 'desactivado' : 'activado'} correctamente.`; this.load(); },
      error: err => this.error = err?.error?.message || 'No se pudo cambiar el estado del rol.'
    });
  }
  cancel(): void { this.formOpen = false; this.error = ''; }
}
