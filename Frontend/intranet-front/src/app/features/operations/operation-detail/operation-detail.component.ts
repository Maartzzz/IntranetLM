
import { Component, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { OperationsService } from '../../../core/services/operations.service';
import {
  Operation,
  OperationRequirement,
  RequirementCatalog,
  Status
} from '../../../core/models/operations.models';
import { RequirementFormDialogComponent } from './requirement-form-dialog/requirement-form-dialog.component';

@Component({
  selector: 'app-operation-detail',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './operation-detail.component.html',
  styleUrl: './operation-detail.component.scss'
})
export class OperationDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(OperationsService);
  private readonly dialog = inject(MatDialog);

  operationId = 0;
  operation: Operation | null = null;
  requirements: OperationRequirement[] = [];
  catalog: RequirementCatalog[] = [];
  statuses: Status[] = [];

  loading = true;
  errorMessage = '';
  requirementError = '';

  ngOnInit(): void {
    this.operationId = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isInteger(this.operationId) || this.operationId <= 0) {
      this.errorMessage = 'Identificador de operación no válido.';
      this.loading = false;
      return;
    }

    this.load();
  }

  load(): void {
    this.loading = true;

    this.service.getOperation(this.operationId).subscribe({
      next: item => {
        this.operation = item;
        this.loading = false;
        this.loadRequirements();
      },
      error: error => {
        console.error(error);
        this.errorMessage = 'No se pudo cargar la operación.';
        this.loading = false;
      }
    });

    this.service.getRequirementCatalog(true).subscribe({
      next: data => this.catalog = data,
      error: error => console.error('Catálogo de requisitos:', error)
    });

    this.service.getStatuses('REQUISITO').subscribe({
      next: data => this.statuses = data,
      error: error => console.error('Estados de requisitos:', error)
    });
  }

  loadRequirements(): void {
    this.requirementError = '';

    this.service.getRequirements(this.operationId).subscribe({
      next: data => this.requirements = data,
      error: error => {
        console.error(error);
        this.requirementError = 'No se pudieron cargar los requisitos de esta operación.';
      }
    });
  }

  get completedRequirements(): number {
    return this.requirements.filter(
      item => item.statusName.toLowerCase() === 'completado'
    ).length;
  }

  get pendingRequirements(): number {
    return this.requirements.filter(
      item => item.statusName.toLowerCase() === 'pendiente'
    ).length;
  }

  get inProgressRequirements(): number {
    return this.requirements.filter(
      item => item.statusName.toLowerCase() === 'en seguimiento'
    ).length;
  }

  get blockedRequirements(): number {
    return this.requirements.filter(
      item => item.statusName.toLowerCase() === 'bloqueado'
    ).length;
  }

  get progressPercentage(): number {
    if (!this.requirements.length) return 0;

    return Math.round(
      (this.completedRequirements / this.requirements.length) * 100
    );
  }

  addRequirement(): void {
    this.openRequirementForm('create');
  }

  editRequirement(item: OperationRequirement): void {
    this.openRequirementForm('edit', item);
  }

  private openRequirementForm(
    mode: 'create' | 'edit',
    requirement?: OperationRequirement
  ): void {
    if (this.statuses.length === 0) {
      this.requirementError = 'No se pudieron cargar los estados de requisitos.';
      return;
    }

    this.dialog.open(RequirementFormDialogComponent, {
      width: '620px',
      maxWidth: 'calc(100vw - 28px)',
      data: {
        mode,
        operationId: this.operationId,
        requirement,
        catalog: this.catalog,
        statuses: this.statuses
      }
    }).afterClosed().subscribe(saved => {
      if (saved) this.loadRequirements();
    });
  }
}
