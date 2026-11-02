import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { forkJoin } from 'rxjs';

import { OperationsService } from '../../core/services/operations.service';
import { Operation, Status } from '../../core/models/operations.models';
import { OperationFormDialogComponent } from './operation-form-dialog/operation-form-dialog.component';

@Component({
  selector: 'app-operations-home',
  standalone: true,
  imports: [FormsModule, RouterLink, DragDropModule],
  templateUrl: './operations-home.component.html',
  styleUrl: './operations-home.component.scss'
})
export class OperationsHomeComponent implements OnInit {
  private readonly service = inject(OperationsService);
  private readonly dialog = inject(MatDialog);

  operations: Operation[] = [];
  statuses: Status[] = [];

  searchTerm = '';
  loading = false;
  updatingOperationId: number | null = null;
  errorMessage = '';
  successMessage = '';

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      operations: this.service.getOperations(),
      statuses: this.service.getStatuses('OPERACION')
    }).subscribe({
      next: result => {
        this.operations = result.operations;
        this.statuses = result.statuses;
        this.loading = false;
      },
      error: error => {
        console.error('Error al cargar el tablero de operaciones:', error);
        this.errorMessage = 'No se pudo cargar el tablero de operaciones.';
        this.loading = false;
      }
    });
  }

  get filteredOperations(): Operation[] {
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      return this.operations;
    }

    return this.operations.filter(operation => {
      const searchableText = [
        operation.operationNumber,
        operation.reference,
        operation.clientName,
        operation.cargoTypeName,
        operation.customerOrderNumber,
        operation.origin,
        operation.destination
      ].filter(Boolean).join(' ').toLowerCase();

      return searchableText.includes(term);
    });
  }

  get dropListIds(): string[] {
    return this.statuses.map(status => this.dropListId(status.id));
  }

  dropListId(statusId: number): string {
    return `operation-status-${statusId}`;
  }

  operationsForStatus(statusId: number): Operation[] {
    return this.filteredOperations.filter(operation => operation.statusId === statusId);
  }

  countForStatus(statusId: number): number {
    return this.operationsForStatus(statusId).length;
  }

  trackStatus(_index: number, status: Status): number {
    return status.id;
  }

  trackOperation(_index: number, operation: Operation): number {
    return operation.id;
  }

  onDrop(event: CdkDragDrop<Operation[]>, targetStatus: Status): void {
    const operation = event.item.data as Operation;

    if (!operation || operation.statusId === targetStatus.id ||
        this.updatingOperationId !== null) {
      return;
    }

    this.changeOperationStatus(operation, targetStatus);
  }

  private changeOperationStatus(operation: Operation, targetStatus: Status): void {
    const previousStatusId = operation.statusId;
    this.updatingOperationId = operation.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.service.updateOperation(operation.id, {
      reference: operation.reference,
      clientId: operation.clientId,
      cargoTypeId: operation.cargoTypeId,
      customerOrderNumber: operation.customerOrderNumber,
      origin: operation.origin,
      destination: operation.destination,
      quantity: operation.quantity,
      estimatedDate: operation.estimatedDate,
      statusId: targetStatus.id,
      observation: operation.observation
    }).subscribe({
      next: updatedOperation => {
        this.operations = this.operations.map(item =>
          item.id === operation.id ? updatedOperation : item
        );
        this.updatingOperationId = null;
        this.successMessage = `${operation.operationNumber} se movió a "${targetStatus.name}".`;
      },
      error: error => {
        console.error('No se pudo actualizar el estado de la operación:', error);
        this.updatingOperationId = null;
        this.errorMessage = `No se pudo mover ${operation.operationNumber}. Se conserva su estado anterior.`;
        // Conserva el estado que tenía la operación en pantalla.
        this.operations = this.operations.map(item =>
          item.id === operation.id ? { ...item, statusId: previousStatusId } : item
        );
      }
    });
  }

  openCreate(): void {
    this.openForm('create');
  }

  openEdit(operation: Operation, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    this.openForm('edit', operation);
  }

  private openForm(mode: 'create' | 'edit', operation?: Operation): void {
    this.dialog.open(OperationFormDialogComponent, {
      width: '720px',
      maxWidth: 'calc(100vw - 28px)',
      data: { mode, operation }
    }).afterClosed().subscribe(saved => {
      if (saved) {
        this.load();
      }
    });
  }
}
