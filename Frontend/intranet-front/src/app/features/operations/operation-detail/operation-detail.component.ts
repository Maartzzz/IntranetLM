import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { ClientServiceOrder } from '../../../core/models/client-service-orders.models';
import { ClientServiceOrderDialogComponent } from './client-service-order-dialog/client-service-order-dialog.component';
import { InternalServiceOrder, ClientServiceOrderDistribution } from '../../../core/models/internal-service-orders.models';
import { OperationsService } from '../../../core/services/operations.service';
import { Operation, OperationRequirement, RequirementCatalog } from '../../../core/models/operations.models';
import { OperationActivity } from '../../../core/models/operation-activity.models';
import { RequirementFormDialogComponent } from './requirement-form-dialog/requirement-form-dialog.component';
import { InternalServiceOrderDialogComponent } from './internal-service-order-dialog/internal-service-order-dialog.component';

@Component({
  selector: 'app-operation-detail', standalone: true, imports: [RouterLink, DatePipe],
  templateUrl: './operation-detail.component.html', styleUrl: './operation-detail.component.scss'
})
export class OperationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(OperationsService);
  private readonly dialog = inject(MatDialog);
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
  loading = true;
  errorMessage = '';
  requirementError = '';

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
  }


  loadClientServiceOrder(): void {
    if (!this.operation) return;
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
    if (!this.operation) return;
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
    if (!this.clientServiceOrder) return;
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
    if (!this.distribution || this.distributionLoading || !this.clientServiceOrder) {
      return false;
    }

    if (this.clientServiceOrder.serviceType === 'FCL') {
      return this.distribution.fcl.some(item => item.available > 0);
    }

    return (this.distribution.availablePackages ?? 0) > 0;
  }

  addClientServiceOrder(): void {
    if (!this.operation || this.clientServiceOrder) return;
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

  createInternalOrders(): void {
    if (!this.clientServiceOrder || !this.distribution) return;
    const dialogRef = this.dialog.open(InternalServiceOrderDialogComponent, {
      width: '900px', maxWidth: 'calc(100vw - 28px)',
      data: { clientOrder: this.clientServiceOrder, distribution: this.distribution }
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
    this.requirementError = '';
    this.service.getRequirements(this.operationId).subscribe({
      next: data => this.requirements = data,
      error: error => { console.error(error); this.requirementError = 'No se pudieron cargar los requisitos de esta operación.'; }
    });
  }

  loadActivity(): void {
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

  addRequirement(): void { this.openRequirementForm('create'); }
  editRequirement(item: OperationRequirement): void { this.openRequirementForm('edit', item); }
  private openRequirementForm(mode: 'create' | 'edit', requirement?: OperationRequirement): void {
    this.dialog.open(RequirementFormDialogComponent, {
      width: '620px', maxWidth: 'calc(100vw - 28px)', data: { mode, operationId: this.operationId, requirement, catalog: this.catalog }
    }).afterClosed().subscribe(saved => { if (saved) { this.loadRequirements(); this.loadActivity(); this.loadClientServiceOrder(); } });
  }
}
