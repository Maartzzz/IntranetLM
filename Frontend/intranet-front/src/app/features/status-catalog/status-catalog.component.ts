import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface StatusItem {
  id: number; processCode: string; name: string; description: string | null; color: string | null;
  active: boolean; initial: boolean; finalStatus: boolean; autoTransition: boolean; allowsScheduling: boolean;
}

@Component({
  selector: 'app-status-catalog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './status-catalog.component.html',
  styleUrl: './status-catalog.component.scss'
})
export class StatusCatalogComponent implements OnInit {
  private readonly http = inject(HttpClient);
  readonly processCode = 'OS_INTERNA';
  statuses: StatusItem[] = [];
  loading = false;
  saving = false;
  error = '';
  success = '';
  editingId: number | null = null;
  form: Omit<StatusItem, 'id'> = this.emptyForm();

  ngOnInit(): void { this.load(); }
  private emptyForm(): Omit<StatusItem, 'id'> {
    return { processCode: this.processCode, name: '', description: '', color: '#64748B',
      active: true, initial: false, finalStatus: false, autoTransition: false, allowsScheduling: true };
  }
  load(): void {
    this.loading = true; this.error = '';
    this.http.get<StatusItem[]>('/api/statuses', { params: { processCode: this.processCode } }).subscribe({
      next: rows => { this.statuses = rows ?? []; this.loading = false; },
      error: e => { this.error = e?.error?.message || 'No se pudo cargar el catálogo.'; this.loading = false; }
    });
  }
  edit(item: StatusItem): void {
    this.editingId = item.id;
    const { id, ...data } = item;
    this.form = { ...data };
    this.success = ''; this.error = '';
  }
  reset(): void { this.editingId = null; this.form = this.emptyForm(); }
  save(): void {
    if (!this.form.name.trim() || this.saving) return;
    this.saving = true; this.error = ''; this.success = '';
    const request = this.editingId
      ? this.http.put<StatusItem>(`/api/statuses/${this.editingId}`, this.form)
      : this.http.post<StatusItem>('/api/statuses', this.form);
    request.subscribe({
      next: () => { this.saving = false; this.success = 'Estado guardado correctamente.'; this.reset(); this.load(); },
      error: e => { this.saving = false; this.error = e?.error?.message || 'No se pudo guardar el estado.'; }
    });
  }
}
