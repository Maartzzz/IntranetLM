import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ClientServiceOrder } from '../../../core/models/client-service-orders.models';
import { ClientServiceOrderDialogComponent } from './client-service-order-dialog/client-service-order-dialog.component';
import { InternalServiceOrder, ClientServiceOrderDistribution } from '../../../core/models/internal-service-orders.models';
import { OperationsService } from '../../../core/services/operations.service';
import { Operation, OperationRequirement, RequirementCatalog, Status } from '../../../core/models/operations.models';
import { OperationActivity } from '../../../core/models/operation-activity.models';
import { RequirementFormDialogComponent } from './requirement-form-dialog/requirement-form-dialog.component';
import { InternalServiceOrderDialogComponent } from './internal-service-order-dialog/internal-service-order-dialog.component';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-operation-detail', standalone: true, imports: [RouterLink, DatePipe],
  templateUrl: './operation-detail.component.html', styleUrl: './operation-detail.component.scss'
})
export class OperationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(OperationsService);
  private readonly dialog = inject(MatDialog);
  private readonly authService = inject(AuthService);

  get canViewClientOrder(): boolean { return this.authService.hasPermission('CUSTOMER_ORDERS.VIEW'); }
  get canViewInternalOrder(): boolean { return this.authService.hasPermission('INTERNAL_ORDERS.VIEW'); }
  get canViewRequirements(): boolean { return this.authService.hasPermission('REQUIREMENTS.OPERATION_VIEW'); }
  get canCreateClientOrder(): boolean { return this.authService.hasPermission('CUSTOMER_ORDERS.CREATE'); }
  get canEditClientOrder(): boolean { return this.authService.hasPermission('CUSTOMER_ORDERS.EDIT'); }
  get canDeleteClientOrder(): boolean { return this.authService.hasPermission('CUSTOMER_ORDERS.DELETE'); }
  get canCreateInternalOrder(): boolean { return this.authService.hasPermission('INTERNAL_ORDERS.CREATE'); }
  get canEditInternalOrder(): boolean { return this.authService.hasPermission('INTERNAL_ORDERS.EDIT'); }
  get canDeleteInternalOrder(): boolean { return this.authService.hasPermission('INTERNAL_ORDERS.DELETE'); }
  get canCreateRequirement(): boolean { return this.authService.hasPermission('REQUIREMENTS.CREATE'); }
  get canEditRequirement(): boolean { return this.authService.hasPermission('REQUIREMENTS.EDIT'); }
  get canViewActivity(): boolean { return this.authService.hasPermission('OPERATIONS.ACTIVITY_VIEW'); }
  operationId = 0;
  operation: Operation | null = null;
  requirements: OperationRequirement[] = [];
  clientServiceOrder: ClientServiceOrder | null = null;
  clientOrderLoading = false;
  clientOrderError = '';
  internalOrders: InternalServiceOrder[] = [];
  internalOrdersLoading = false;
  internalOrdersError = '';
  distribution: ClientServiceOrderDistribution | null = null;
  distributionLoading = false;
  distributionError = '';
  activities: OperationActivity[] = [];
  activityLoading = false;
  activityError = '';
  catalog: RequirementCatalog[] = [];
  requirementStatuses: Status[] = [];
  loading = true;
  errorMessage = '';
  requirementError = '';
  orderActionError = '';
  orderActionLoading = false;

  ngOnInit(): void {
    this.operationId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(this.operationId) || this.operationId <= 0) { this.errorMessage = 'Identificador de operación no válido.'; this.loading = false; return; }
    this.load();
  }

  load(): void {
    this.loading = true;
    this.service.getOperation(this.operationId).subscribe({
      next: item => { this.operation = item; this.loading = false; this.loadRequirements(); this.loadActivity(); this.loadClientServiceOrder(); },
      error: error => { console.error(error); this.errorMessage = 'No se pudo cargar la operación.'; this.loading = false; }
    });
    this.service.getRequirementCatalog(true).subscribe({ next: data => this.catalog = data, error: error => console.error('Catálogo de requisitos:', error) });
    this.service.getStatuses('REQUISITO').subscribe({ next: data => this.requirementStatuses = data.filter(item => item.active), error: error => console.error('Estados de requisitos:', error) });
  }


  loadClientServiceOrder(): void {
    if (!this.canViewClientOrder || !this.operation) {
      this.clientServiceOrder = null;
      this.clientOrderLoading = false;
      return;
    }
    this.clientOrderLoading = true;
    this.clientOrderError = '';
    this.service.getClientServiceOrders(this.operation.clientId).subscribe({
      next: orders => {
        this.clientServiceOrder = orders.find(order => order.operationId === this.operationId) ?? null;
        this.clientOrderLoading = false;
        if (this.clientServiceOrder) {
          this.loadInternalOrders();
          this.loadDistribution();
        } else {
          this.internalOrders = [];
          this.distribution = null;
        }
      },
      error: error => {
        console.error('O/S del cliente:', error);
        this.clientOrderError = 'No se pudo consultar la O/S del cliente.';
        this.clientOrderLoading = false;
      }
    });
  }

  loadInternalOrders(): void {
    if (!this.canViewInternalOrder || !this.operation) {
      this.internalOrders = [];
      this.internalOrdersLoading = false;
      return;
    }
    this.internalOrdersLoading = true;
    this.internalOrdersError = '';
    this.service.getInternalServiceOrders(this.operation.clientId).subscribe({
      next: orders => {
        this.internalOrders = orders.filter(order => order.clientServiceOrderId === this.clientServiceOrder?.id);
        this.internalOrdersLoading = false;
      },
      error: error => {
        console.error('O/S internas:', error);
        this.internalOrdersError = 'No se pudieron cargar las O/S internas.';
        this.internalOrdersLoading = false;
      }
    });
  }

  loadDistribution(): void {
    if (!this.canViewClientOrder || !this.clientServiceOrder) return;
    this.distributionLoading = true;
    this.distributionError = '';
    this.service.getClientServiceOrderDistribution(this.clientServiceOrder.id).subscribe({
      next: result => {
        this.distribution = result;
        this.distributionLoading = false;
      },
      error: error => {
        console.error('Distribución O/S cliente:', error);
        this.distributionError = 'No se pudo consultar la distribución de cantidades.';
        this.distributionLoading = false;
      }
    });
  }

  get clientOrderFclSummary(): string {
    return this.clientServiceOrder?.fclQuantities
      .map(item => `${item.cargoTypeName}: ${item.totalQuantity}`)
      .join(' · ') ?? '';
  }

  get canGenerateInternalOrders(): boolean {
    if (!this.canCreateInternalOrder) return false;
    if (!this.distribution || this.distributionLoading || !this.clientServiceOrder || !this.clientServiceOrder.active) {
      return false;
    }

    if (this.clientServiceOrder.serviceType === 'FCL') {
      return this.distribution.fcl.some(item => item.available > 0);
    }

    return (this.distribution.availablePackages ?? 0) > 0;
  }

  get canCreateClientServiceOrder(): boolean {
    return this.canCreateClientOrder && !!this.operation && !this.clientServiceOrder
      && (this.operation.statusName ?? '').trim().toLocaleLowerCase() === 'confirmada';
  }

  addClientServiceOrder(): void {
    if (!this.canCreateClientServiceOrder || !this.operation) return;
    const dialogRef = this.dialog.open(ClientServiceOrderDialogComponent, {
      width: '680px', maxWidth: 'calc(100vw - 28px)',
      data: {
        operationId: this.operation.id,
        clientId: this.operation.clientId,
        clientName: this.operation.clientName,
        serviceType: this.operation.container ? 'FCL' : 'LCL',
        reference: this.operation.reference,
        origin: this.operation.origin,
        destination: this.operation.destination,
        estimatedDate: this.operation.estimatedDate,
        customerOrderNumber: this.operation.customerOrderNumber,
        quantity: this.operation.quantity,
        cargoTypeId: this.operation.cargoTypeId,
        observation: this.operation.observation
      }
    });
    dialogRef.afterClosed().subscribe(saved => {
      if (saved) { this.loadClientServiceOrder(); this.loadActivity(); }
    });
  }

  editClientServiceOrder(): void {
    if (!this.canEditClientOrder || !this.operation || !this.clientServiceOrder) return;
    const order = this.clientServiceOrder;
    const dialogRef = this.dialog.open(ClientServiceOrderDialogComponent, { width: '680px', maxWidth: 'calc(100vw - 28px)', data: { operationId: this.operation.id, clientId: order.clientId, clientName: order.clientName, serviceType: order.serviceType, reference: order.reference, origin: order.origin, destination: order.destination, estimatedDate: order.estimatedArrivalDate, customerOrderNumber: order.clientOrderNumber, quantity: order.totalPackages, cargoTypeId: order.fclQuantities[0]?.cargoTypeId ?? this.operation.cargoTypeId, observation: order.observation, order } });
    dialogRef.afterClosed().subscribe(saved => { if (saved) { this.loadClientServiceOrder(); this.loadActivity(); } });
  }

  editInternalOrder(order: InternalServiceOrder): void {
    if (!this.canEditInternalOrder || !this.clientServiceOrder || !this.distribution || !this.clientServiceOrder.active) return;
    const dialogRef = this.dialog.open(InternalServiceOrderDialogComponent, { width: '900px', maxWidth: 'calc(100vw - 28px)', data: { clientOrder: this.clientServiceOrder, distribution: this.distribution, clientSubclientId: this.operation?.clientSubclientId ?? null, order } });
    dialogRef.afterClosed().subscribe(saved => { if (saved) { this.loadInternalOrders(); this.loadDistribution(); this.loadActivity(); } });
  }

  toggleClientOrderActive(): void {
    if (!this.canEditClientOrder || !this.clientServiceOrder || this.orderActionLoading) return;
    const next = !this.clientServiceOrder.active;
    if (!confirm(`¿${next ? 'Reactivar' : 'Desactivar'} la O/S del cliente ${this.clientServiceOrder.clientOrderNumber}?`)) return;
    this.orderActionLoading = true; this.orderActionError = '';
    this.service.setClientServiceOrderActive(this.clientServiceOrder.id, next).subscribe({
      next: () => { this.orderActionLoading = false; this.loadClientServiceOrder(); },
      error: e => { this.orderActionLoading = false; this.orderActionError = e?.error?.message || 'No se pudo cambiar el estado de la O/S del cliente.'; }
    });
  }

  deleteClientOrder(): void {
    if (!this.canDeleteClientOrder || !this.clientServiceOrder || this.orderActionLoading) return;
    if (!confirm(`¿Eliminar físicamente la O/S del cliente ${this.clientServiceOrder.clientOrderNumber}? Esta acción no se puede deshacer.`)) return;
    this.orderActionLoading = true; this.orderActionError = '';
    this.service.deleteClientServiceOrder(this.clientServiceOrder.id).subscribe({
      next: () => { this.orderActionLoading = false; this.clientServiceOrder = null; this.internalOrders = []; this.distribution = null; this.loadActivity(); },
      error: e => { this.orderActionLoading = false; this.orderActionError = e?.error?.message || 'No se puede eliminar la O/S porque tiene registros relacionados.'; }
    });
  }

  toggleInternalOrderActive(order: InternalServiceOrder): void {
    if (!this.canEditInternalOrder || this.orderActionLoading) return;
    const next = !order.active;
    if (!confirm(`¿${next ? 'Reactivar' : 'Desactivar'} la O/S interna #${order.internalOrderNumber}?`)) return;
    this.orderActionLoading = true; this.orderActionError = '';
    this.service.setInternalServiceOrderActive(order.id, next).subscribe({
      next: () => { this.orderActionLoading = false; this.loadInternalOrders(); this.loadDistribution(); },
      error: e => { this.orderActionLoading = false; this.orderActionError = e?.error?.message || 'No se pudo cambiar el estado de la O/S interna.'; }
    });
  }

  deleteInternalOrder(order: InternalServiceOrder): void {
    if (!this.canDeleteInternalOrder || this.orderActionLoading) return;
    if (!confirm(`¿Eliminar físicamente la O/S interna #${order.internalOrderNumber}? Esta acción no se puede deshacer.`)) return;
    this.orderActionLoading = true; this.orderActionError = '';
    this.service.deleteInternalServiceOrder(order.id).subscribe({
      next: () => { this.orderActionLoading = false; this.loadInternalOrders(); this.loadDistribution(); },
      error: e => { this.orderActionLoading = false; this.orderActionError = e?.error?.message || 'No se puede eliminar la O/S porque tiene programación o registros relacionados.'; }
    });
  }

  createInternalOrders(): void {
    if (!this.canCreateInternalOrder || !this.clientServiceOrder || !this.distribution || !this.clientServiceOrder.active) return;
    const dialogRef = this.dialog.open(InternalServiceOrderDialogComponent, {
      width: '900px', maxWidth: 'calc(100vw - 28px)',
      data: {
        clientOrder: this.clientServiceOrder,
        distribution: this.distribution,
        // La relación vigente se obtiene de la Operación, no se duplica en la O/S del cliente.
        clientSubclientId: this.operation?.clientSubclientId ?? null
      }
    });
    dialogRef.afterClosed().subscribe(created => {
      if (created?.length) {
        this.loadInternalOrders();
        this.loadDistribution();
        this.loadActivity();
      }
    });
  }

  loadRequirements(): void {
    if (!this.canViewRequirements) {
      this.requirements = [];
      return;
    }
    this.requirementError = '';
    this.service.getRequirements(this.operationId).subscribe({
      next: data => this.requirements = data,
      error: error => { console.error(error); this.requirementError = 'No se pudieron cargar los requisitos de esta operación.'; }
    });
  }

  loadActivity(): void {
    if (!this.canViewActivity) {
      this.activities = [];
      this.activityLoading = false;
      return;
    }
    this.activityLoading = true;
    this.activityError = '';
    this.service.getActivity(this.operationId).subscribe({
      next: data => { this.activities = data; this.activityLoading = false; },
      error: error => { console.error('Historial de actividad:', error); this.activityError = 'No se pudo cargar el historial de actividad.'; this.activityLoading = false; }
    });
  }

  actionLabel(action: string): string {
    switch (action) {
      case 'CREATED': return 'Creación';
      case 'UPDATED': return 'Actualización';
      case 'STATUS_CHANGED': return 'Cambio de estado';
      default: return action;
    }
  }

  entityLabel(entityType: string): string {
    return entityType === 'REQUISITO' ? 'Requisito' : 'Operación';
  }

  addRequirement(): void { if (this.canCreateRequirement) this.openRequirementForm('create'); }
  editRequirement(item: OperationRequirement): void { if (this.canEditRequirement) this.openRequirementForm('edit', item); }
  private openRequirementForm(mode: 'create' | 'edit', requirement?: OperationRequirement): void {
    this.dialog.open(RequirementFormDialogComponent, {
      width: '620px', maxWidth: 'calc(100vw - 28px)', data: { mode, operationId: this.operationId, requirement, catalog: this.catalog, statuses: this.requirementStatuses }
    }).afterClosed().subscribe(saved => { if (saved) { this.loadRequirements(); this.loadActivity(); this.loadClientServiceOrder(); } });
  }
}
