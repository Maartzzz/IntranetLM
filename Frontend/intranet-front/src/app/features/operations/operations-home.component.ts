import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { OperationsService } from '../../core/services/operations.service';
import { Operation, Status } from '../../core/models/operations.models';
import { ClientServiceOrder } from '../../core/models/client-service-orders.models';
import { ClientServiceOrderDistribution, InternalServiceOrder } from '../../core/models/internal-service-orders.models';
import { OperationFormDialogComponent } from './operation-form-dialog/operation-form-dialog.component';

type OperationBoardFilter = 'ALL' | 'WITHOUT_CLIENT_ORDER' | 'WITHOUT_INTERNAL_ORDERS' | 'PARTIAL' | 'COMPLETE';

interface OperationBoardSummary {
  clientServiceOrder: ClientServiceOrder | null;
  internalOrders: InternalServiceOrder[];
  requested: number | null;
  distributed: number | null;
  pending: number | null;
  serviceType: 'FCL' | 'LCL' | null;
}

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
  boardSummaries: Record<number, OperationBoardSummary> = {};
  boardFilter: OperationBoardFilter = 'ALL';

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
        this.loadBoardSummaries(result.operations);
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

    return this.operations.filter(operation => {
      const summary = this.boardSummaries[operation.id];
      if (!this.matchesBoardFilter(summary)) return false;
      if (!term) return true;
      const searchableText = [
        operation.operationNumber,
        operation.reference,
        operation.clientName,
        operation.cargoTypeName,
        operation.customerOrderNumber,
        operation.origin,
        operation.destination,
        summary?.clientServiceOrder?.clientOrderNumber,
        summary?.clientServiceOrder?.reference
      ].filter(Boolean).join(' ').toLowerCase();

      return searchableText.includes(term);
    });
  }

  private loadBoardSummaries(operations: Operation[]): void {
    const clientIds = [...new Set(operations.map(operation => operation.clientId).filter(Boolean))];
    if (clientIds.length === 0) {
      this.boardSummaries = {};
      this.loading = false;
      return;
    }

    forkJoin(clientIds.map(clientId => forkJoin({
      clientOrders: this.service.getClientServiceOrders(clientId).pipe(catchError(() => of([] as ClientServiceOrder[]))),
      internalOrders: this.service.getInternalServiceOrders(clientId).pipe(catchError(() => of([] as InternalServiceOrder[])))
    }).pipe(map(result => ({ clientId, ...result }))))).subscribe(results => {
      const ordersByClient = new Map(results.map(result => [result.clientId, result.clientOrders]));
      const internalByClient = new Map(results.map(result => [result.clientId, result.internalOrders]));
      const associatedOrders = operations
        .map(operation => (ordersByClient.get(operation.clientId) ?? [])
          .find(order => order.active && order.operationId === operation.id) ?? null)
        .filter((order): order is ClientServiceOrder => order !== null);
      const uniqueOrders = [...new Map(associatedOrders.map(order => [order.id, order])).values()];

      const distributionRequests = uniqueOrders.map(order => this.service
        .getClientServiceOrderDistribution(order.id)
        .pipe(catchError(() => of(null as ClientServiceOrderDistribution | null)))
        .pipe(map(distribution => [order.id, distribution] as const)));

      const distributions$ = distributionRequests.length
        ? forkJoin(distributionRequests)
        : of([] as readonly (readonly [number, ClientServiceOrderDistribution | null])[]);

      distributions$.subscribe(distributionEntries => {
        const distributions = new Map<number, ClientServiceOrderDistribution | null>(distributionEntries);
        const summaries: Record<number, OperationBoardSummary> = {};

        for (const operation of operations) {
          const order = (ordersByClient.get(operation.clientId) ?? [])
            .find(item => item.active && item.operationId === operation.id) ?? null;
          const internalOrders = order
            ? (internalByClient.get(operation.clientId) ?? [])
                .filter(item => item.active && item.clientServiceOrderId === order.id)
            : [];
          const distribution = order ? distributions.get(order.id) ?? null : null;
          const requested = distribution
            ? (distribution.serviceType === 'FCL'
                ? distribution.fcl.reduce((total, item) => total + item.requested, 0)
                : distribution.requestedPackages)
            : null;
          const distributed = distribution
            ? (distribution.serviceType === 'FCL'
                ? distribution.fcl.reduce((total, item) => total + item.distributed, 0)
                : distribution.distributedPackages)
            : null;
          const pending = distribution
            ? (distribution.serviceType === 'FCL'
                ? distribution.fcl.reduce((total, item) => total + item.available, 0)
                : distribution.availablePackages)
            : null;

          summaries[operation.id] = {
            clientServiceOrder: order,
            internalOrders,
            requested,
            distributed,
            pending,
            serviceType: distribution?.serviceType ?? order?.serviceType ?? null
          };
        }

        this.boardSummaries = summaries;
        this.loading = false;
      });
    });
  }

  private matchesBoardFilter(summary: OperationBoardSummary | undefined): boolean {
    const order = summary?.clientServiceOrder ?? null;
    const internalCount = summary?.internalOrders.length ?? 0;
    switch (this.boardFilter) {
      case 'WITHOUT_CLIENT_ORDER': return !order;
      case 'WITHOUT_INTERNAL_ORDERS': return !!order && internalCount === 0;
      case 'PARTIAL': return !!order && (summary?.distributed ?? 0) > 0 && (summary?.pending ?? 0) > 0;
      case 'COMPLETE': return !!order && summary?.pending === 0;
      default: return true;
    }
  }

  summaryFor(operationId: number): OperationBoardSummary | undefined {
    return this.boardSummaries[operationId];
  }

  pendingScheduleCount(operationId: number): number {
    return this.summaryFor(operationId)?.internalOrders.filter(order => !order.scheduledDate).length ?? 0;
  }

  distributionPercent(operationId: number): number {
    const summary = this.summaryFor(operationId);
    if (!summary || summary.requested === null || summary.requested <= 0 || summary.distributed === null) return 0;
    return Math.min(100, Math.round(summary.distributed / summary.requested * 100));
  }

  distributionLabel(operationId: number): string {
    const summary = this.summaryFor(operationId);
    if (!summary?.clientServiceOrder) return 'Sin O/S del cliente';
    if (summary.requested === null || summary.distributed === null || summary.pending === null) return 'Distribución no disponible';
    if (summary.pending === 0) return 'Distribución completa';
    if (summary.distributed > 0) return 'Distribución parcial';
    return 'Sin distribución';
  }

  formatQuantity(value: number | null, serviceType: 'FCL' | 'LCL' | null): string {
    if (value === null) return '—';
    return serviceType === 'FCL' ? `${value} cont.` : `${value} bultos`;
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
      clientSubclientId: operation.clientSubclientId,
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