import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  CdkDragDrop,
  DragDropModule,
  moveItemInArray
} from '@angular/cdk/drag-drop';
import {
  ScheduleOrder,
  ScheduleVersionDetail,
  ScheduleVersionSummary,
  ScheduleStatusOption
} from '../../core/models/schedule.models';
import { ScheduleGlobalSummary, ScheduleService } from '../../core/services/schedule.service';
import { AuthService } from '../../core/auth/auth.service';

interface ScheduleDay {
  date: Date;
  dateKey: string;
  label: string;
  orders: ScheduleOrder[];
  isSunday: boolean;
  isSaturday: boolean;
}

@Component({
  selector: 'app-scheduling',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './scheduling.component.html',
  styleUrl: './scheduling.component.scss'
})
export class SchedulingComponent implements OnInit {
  private readonly scheduleService = inject(ScheduleService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);

  get canEditSchedule(): boolean { return this.authService.hasPermission('SCHEDULING.EDIT'); }
  get canCreateVersion(): boolean { return this.authService.hasPermission('SCHEDULING.VERSION_CREATE'); }
  get canViewVersions(): boolean { return this.authService.hasPermission('SCHEDULING.VERSION_VIEW'); }

  startDate = this.getMonday(new Date());
  endDate = this.addDays(this.startDate, 6);
  scheduled: ScheduleOrder[] = [];
  available: ScheduleOrder[] = [];
  globalSummary: ScheduleGlobalSummary = { pending: 0, programmed: 0, inExecution: 0, overdue: 0, completed: 0 };
  days: ScheduleDay[] = [];
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';
  savingVersion = false;
  loadingVersions = false;
  loadingSnapshot = false;
  historyOpen = false;
  versionMessage = '';
  versionError = '';
  versions: ScheduleVersionSummary[] = [];
  selectedVersion: ScheduleVersionDetail | null = null;
  statusOptions: ScheduleStatusOption[] = [];
  statusSavingOrderId: number | null = null;

  readonly unscheduledListId = 'schedule-unscheduled';
  readonly otherPeriodListId = 'schedule-other-period';
  readonly excludedListId = 'schedule-excluded';
  readonly overdueListId = 'schedule-overdue';

  // Mantener referencias estables: CDK usa estos arrays como datos de cada DropList.
  // No deben reconstruirse en getters durante cada ciclo de detección de cambios.
  unscheduledOrders: ScheduleOrder[] = [];
  otherPeriodOrders: ScheduleOrder[] = [];
  overdueOrders: ScheduleOrder[] = [];
  excludedOrders: ScheduleOrder[] = [];
  connectedDropListIds: string[] = [];

  private classifyAvailableOrders(): void {
    const start = this.toDateKey(this.startDate);
    const end = this.toDateKey(this.endDate);

    this.unscheduledOrders = [];
    this.otherPeriodOrders = [];
    this.overdueOrders = [];
    this.excludedOrders = [];

    for (const order of this.available) {
      if (!order.scheduledDate) {
        this.unscheduledOrders.push(order);
      } else if (order.scheduledDate < start) {
        this.overdueOrders.push(order);
      } else if (order.scheduledDate > end) {
        this.otherPeriodOrders.push(order);
      } else {
        this.excludedOrders.push(order);
      }
    }
  }

  ngOnInit(): void {
    const weekStart = this.route.snapshot.queryParamMap.get('weekStart');
    if (weekStart && /^\d{4}-\d{2}-\d{2}$/.test(weekStart)) {
      const [year, month, day] = weekStart.split('-').map(Number);
      const candidate = new Date(year, month - 1, day);
      if (!Number.isNaN(candidate.getTime())) this.startDate = candidate;
      this.endDate = this.addDays(this.startDate, 6);
    }
    this.loadStatuses();
    this.loadSchedule();
  }

  openDay(dateKey: string): void {
    this.router.navigate(['/scheduling/day', dateKey], {
      queryParams: { weekStart: this.toDateKey(this.startDate) }
    });
  }

  private loadStatuses(): void {
    this.scheduleService.getStatuses().subscribe({
      next: rows => this.statusOptions = (rows ?? []).filter(status => status.active && !status.autoTransition),
      error: error => this.errorMessage = error?.error?.message || 'No se pudo cargar el catálogo de estados de O/S internas.'
    });
  }

  changeOrderStatus(order: ScheduleOrder, rawStatusId: string): void {
    if (!this.canEditSchedule) return;
    const statusId = Number(rawStatusId);
    if (!order || !Number.isInteger(statusId) || statusId <= 0 || this.saving || this.statusSavingOrderId !== null) return;
    const target = this.statusOptions.find(status => status.id === statusId);
    if (!target || target.id === order.statusId) return;
    if (target.finalStatus && !window.confirm(`¿Confirmas cambiar la O/S ${order.internalOrderNumber} al estado ${target.name}?`)) {
      this.loadSchedule();
      return;
    }
    this.statusSavingOrderId = order.id;
    this.errorMessage = '';
    this.successMessage = '';
    this.scheduleService.changeOrderStatus(order.id, statusId).subscribe({
      next: () => {
        this.statusSavingOrderId = null;
        this.successMessage = `O/S ${order.internalOrderNumber}: estado actualizado a ${target.name}.`;
        this.loadSchedule();
      },
      error: error => {
        this.statusSavingOrderId = null;
        this.errorMessage = error?.error?.message || error?.error?.error || 'No se pudo cambiar el estado de la O/S.';
        this.loadSchedule();
      }
    });
  }

  loadSchedule(): void {
    this.loadGlobalSummary();
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.scheduleService.getSchedule(this.toDateKey(this.startDate), this.toDateKey(this.endDate))
      .subscribe({
        next: (response) => {
          this.scheduled = response.scheduled ?? [];
          this.available = response.available ?? [];
          this.classifyAvailableOrders();
          this.buildDays();
          this.loading = false;
        },
        error: (error) => {
          this.errorMessage = error?.error?.message
            || error?.error?.error
            || 'No se pudo cargar la programación. Verifica la conexión con el servidor.';
          this.loading = false;
        }
      });
  }

  private loadGlobalSummary(): void {
    this.scheduleService.getGlobalSummary().subscribe({
      next: summary => this.globalSummary = summary ?? { pending: 0, programmed: 0, inExecution: 0, overdue: 0, completed: 0 },
      error: () => this.globalSummary = { pending: 0, programmed: 0, inExecution: 0, overdue: 0, completed: 0 }
    });
  }

  onDrop(event: CdkDragDrop<ScheduleOrder[]>, targetDate?: string): void {
    if (!this.canEditSchedule) return;
    if (this.saving || this.loading) return;

    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    const order = event.item.data as ScheduleOrder;
    const sourceId = event.previousContainer.id;
    const destinationId = event.container.id;
    const availableListIds = [
      this.unscheduledListId,
      this.otherPeriodListId,
      this.overdueListId,
      this.excludedListId
    ];
    const destinationIsAvailable = availableListIds.includes(destinationId);
    const sourceIsAvailable = availableListIds.includes(sourceId);

    if (!order) return;

    if (destinationIsAvailable) {
      if (!sourceIsAvailable) this.removeFromBoard(order);
      return;
    }

    if (!targetDate) return;

    const destinationDay = this.days.find(day => day.dateKey === targetDate);
    if (!destinationDay || destinationDay.isSunday) {
      this.errorMessage = 'El domingo está definido como día no laborable. Selecciona otro día.';
      return;
    }

    const reason = window.prompt(
      `Motivo para programar la O/S ${order.internalOrderNumber}:`,
      'Programación desde tablero semanal'
    );
    if (reason === null) return;

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.scheduleService.includeOrder(order.id, {
      scheduledDate: targetDate,
      reason: reason.trim() || 'Programación desde tablero semanal'
    }).subscribe({
      next: () => {
        this.successMessage = `O/S ${order.internalOrderNumber} programada correctamente.`;
        this.loadAfterMutation();
      },
      error: error => this.handleMutationError(error)
    });
  }

  removeFromBoard(order: ScheduleOrder): void {
    if (!this.canEditSchedule) return;
    if (!order || this.saving || this.loading) return;

    const confirmed = window.confirm(
      `¿Retirar la O/S ${order.internalOrderNumber} del tablero? La orden no será cancelada y conservará su fecha programada.`
    );
    if (!confirmed) return;

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.scheduleService.excludeOrder(order.id).subscribe({
      next: () => {
        this.successMessage = `O/S ${order.internalOrderNumber} retirada del tablero. La orden no fue cancelada.`;
        this.loadAfterMutation();
      },
      error: error => this.handleMutationError(error)
    });
  }

  saveCurrentVersion(): void {
    if (!this.canCreateVersion) return;
    if (this.savingVersion || this.loading || this.saving) return;
    const confirmed = window.confirm(
      `¿Guardar una versión de la programación del ${this.toDateKey(this.startDate)} al ${this.toDateKey(this.endDate)}? Se conservarán ${this.scheduled.length} O/S programadas.`
    );
    if (!confirmed) return;

    this.savingVersion = true;
    this.versionMessage = '';
    this.versionError = '';
    this.scheduleService.saveVersion(this.toDateKey(this.startDate), this.toDateKey(this.endDate))
      .subscribe({
        next: version => {
          this.savingVersion = false;
          this.versionMessage = `Versión ${version.versionNumber} guardada correctamente.`;
          if (this.historyOpen) this.loadVersions();
        },
        error: error => {
          this.savingVersion = false;
          this.versionError = error?.error?.message || error?.error?.error
            || 'No se pudo guardar la versión de programación.';
        }
      });
  }

  toggleHistory(): void {
    if (!this.canViewVersions) return;
    this.historyOpen = !this.historyOpen;
    this.selectedVersion = null;
    if (this.historyOpen) this.loadVersions();
  }

  loadVersions(): void {
    if (!this.canViewVersions) return;
    this.loadingVersions = true;
    this.versionError = '';
    this.scheduleService.getVersions(this.toDateKey(this.startDate), this.toDateKey(this.endDate))
      .subscribe({
        next: versions => {
          this.versions = versions ?? [];
          this.loadingVersions = false;
        },
        error: error => {
          this.loadingVersions = false;
          this.versionError = error?.error?.message || error?.error?.error
            || 'No se pudo consultar el histórico de versiones.';
        }
      });
  }

  viewVersion(version: ScheduleVersionSummary): void {
    if (!this.canViewVersions) return;
    this.loadingSnapshot = true;
    this.selectedVersion = null;
    this.versionError = '';
    this.scheduleService.getVersionDetail(version.id).subscribe({
      next: detail => {
        this.selectedVersion = detail;
        this.loadingSnapshot = false;
      },
      error: error => {
        this.loadingSnapshot = false;
        this.versionError = error?.error?.message || error?.error?.error
          || 'No se pudo cargar el snapshot de la versión.';
      }
    });
  }

  closeSnapshot(): void {
    this.selectedVersion = null;
  }

  getDayListId(dateKey: string): string {
    return `schedule-day-${dateKey}`;
  }

  trackOrder(_: number, order: ScheduleOrder): number {
    return order.id;
  }

  private loadAfterMutation(): void {
    this.loadGlobalSummary();
    this.scheduleService.getSchedule(this.toDateKey(this.startDate), this.toDateKey(this.endDate))
      .subscribe({
        next: response => {
          this.scheduled = response.scheduled ?? [];
          this.available = response.available ?? [];
          this.classifyAvailableOrders();
          this.buildDays();
          this.loading = false;
          this.saving = false;
        },
        error: error => {
          this.saving = false;
          this.loading = false;
          this.errorMessage = error?.error?.message || 'La operación se guardó, pero no se pudo actualizar el tablero.';
        }
      });
  }

  private handleMutationError(error: any): void {
    this.saving = false;
    const message = error?.error?.message
      || error?.error?.error
      || 'No se pudo actualizar la programación. Se mantienen los datos del servidor.';
    this.loadSchedule();
    this.errorMessage = message;
  }

  previousWeek(): void {
    this.startDate = this.addDays(this.startDate, -7);
    this.endDate = this.addDays(this.startDate, 6);
    this.loadSchedule();
  }

  nextWeek(): void {
    this.startDate = this.addDays(this.startDate, 7);
    this.endDate = this.addDays(this.startDate, 6);
    this.loadSchedule();
  }

  goToCurrentWeek(): void {
    this.startDate = this.getMonday(new Date());
    this.endDate = this.addDays(this.startDate, 6);
    this.loadSchedule();
  }

  private buildDays(): void {
    const weekdayLabels = ['LUN', 'MAR', 'MIÉ', 'JUE', 'VIE', 'SÁB', 'DOM'];
    this.days = Array.from({ length: 7 }, (_, index) => {
      const date = this.addDays(this.startDate, index);
      const dateKey = this.toDateKey(date);
      return {
        date,
        dateKey,
        label: `${weekdayLabels[index]} ${date.getDate()}`,
        orders: this.scheduled.filter(order => order.scheduledDate === dateKey),
        isSunday: index === 6,
        isSaturday: index === 5
      };
    });

    // Una referencia estable para las conexiones de todas las listas CDK.
    this.connectedDropListIds = [
      this.unscheduledListId,
      this.otherPeriodListId,
      this.overdueListId,
      this.excludedListId,
      ...this.days.map(day => this.getDayListId(day.dateKey))
    ];
  }

  private getMonday(date: Date): Date {
    const result = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const day = result.getDay();
    result.setDate(result.getDate() + (day === 0 ? -6 : 1 - day));
    return result;
  }

  private addDays(date: Date, days: number): Date {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
  }

  private toDateKey(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
