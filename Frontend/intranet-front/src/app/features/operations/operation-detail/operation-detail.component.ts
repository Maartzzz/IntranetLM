import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { OperationsService } from '../../../core/services/operations.service';
import { Operation, OperationRequirement, RequirementCatalog } from '../../../core/models/operations.models';
import { OperationActivity } from '../../../core/models/operation-activity.models';
import { RequirementFormDialogComponent } from './requirement-form-dialog/requirement-form-dialog.component';

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
      next: item => { this.operation = item; this.loading = false; this.loadRequirements(); this.loadActivity(); },
      error: error => { console.error(error); this.errorMessage = 'No se pudo cargar la operación.'; this.loading = false; }
    });
    this.service.getRequirementCatalog(true).subscribe({ next: data => this.catalog = data, error: error => console.error('Catálogo de requisitos:', error) });
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
    }).afterClosed().subscribe(saved => { if (saved) { this.loadRequirements(); this.loadActivity(); } });
  }
}
