import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';

interface ArchivedOperation {
  id: number;
  operationNumber: string;
  reference: string | null;
  clientName: string;
  clientOrderNumber: string | null;
  internalOrderCount: number;
  internalOrderNumbers: string[];
  cargoType: string;
  container: boolean;
  estimatedDate: string | null;
  finalStatus: string;
  archivedAt: string;
  reason: string;
}

@Component({
  selector: 'app-operation-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './operation-history.component.html',
  styleUrl: './operation-history.component.scss'
})
export class OperationHistoryComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  rows: ArchivedOperation[] = [];
  loading = false;
  error = '';
  search = '';
  status = 'TODOS';
  restoringId: number | null = null;
  selectedOperation: any = null;
  selectedOrders: ArchivedOperation | null = null;

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.error = '';
    let params = new HttpParams().set('status', this.status);
    if (this.search.trim()) params = params.set('search', this.search.trim());
    this.http.get<ArchivedOperation[]>('/api/operations/archive', { params }).subscribe({
      next: rows => { this.rows = rows; this.loading = false; },
      error: () => { this.error = 'No se pudo cargar el histórico de operaciones.'; this.loading = false; }
    });
  }

  view(row: ArchivedOperation): void {
    this.selectedOrders = row;
    this.http.get<any>(`/api/operations/${row.id}`).subscribe({
      next: operation => { this.selectedOperation = operation; },
      error: () => { this.selectedOrders = null; this.error = 'No se pudo cargar el detalle de la operación.'; }
    });
  }

  closeDetail(): void { this.selectedOperation = null; this.selectedOrders = null; }

  restore(row: ArchivedOperation): void {
    if (this.restoringId !== null || !window.confirm(`¿Restaurar la operación ${row.operationNumber}?`)) return;
    this.restoringId = row.id;
    this.http.post(`/api/operations/archive/${row.id}/restore`, {}).subscribe({
      next: () => { this.restoringId = null; this.load(); },
      error: () => { this.restoringId = null; this.error = 'No se pudo restaurar la operación. Verifique sus permisos.'; }
    });
  }


  exportCsv(): void {
    const headers = ['N.º Operación', 'Referencia', 'Cliente', 'O/S del cliente',
      'O/S internas', 'Tipo de carga', 'Fecha estimada', 'Estado final', 'Fecha de archivo', 'Motivo'];
    const values = this.rows.map(row => [
      row.operationNumber, row.reference || '', row.clientName, row.clientOrderNumber || '',
      row.internalOrderNumbers.join(' | '), `${row.container ? 'FCL' : 'LCL'} - ${row.cargoType}`,
      row.estimatedDate || '', row.finalStatus, row.archivedAt, this.reasonLabel(row.reason)
    ]);
    const quote = (value: unknown) => `"${String(value ?? '').replace(/"/g, '""')}"`;
    const csv = '\\uFEFF' + [headers, ...values].map(line => line.map(quote).join(';')).join('\\r\\n');
    const url = URL.createObjectURL(new Blob([csv], { type: 'text/csv;charset=utf-8;' }));
    const anchor = document.createElement('a');
    anchor.href = url; anchor.download = 'historico-operaciones.csv'; anchor.click();
    URL.revokeObjectURL(url);
  }
    reasonLabel(reason: string): string {
    return reason === 'CANCELACION' ? 'Cancelación' : 'Finalización';
  }

  trackById(_index: number, row: ArchivedOperation): number { return row.id; }
}
