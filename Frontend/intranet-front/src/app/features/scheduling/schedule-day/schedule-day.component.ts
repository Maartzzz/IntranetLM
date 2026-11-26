import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ScheduleOrder } from '../../../core/models/schedule.models';
import { ScheduleService } from '../../../core/services/schedule.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { DirectInternalOrderDialogComponent } from './direct-internal-order-dialog.component';
import { VehiclesService, FleetVehicle } from '../../../core/services/vehicles.service';
import { HttpClient } from '@angular/common/http';

type ServiceFilter = 'ALL' | 'FCL' | 'LCL';

@Component({
  selector: 'app-schedule-day',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './schedule-day.component.html',
  styleUrl: './schedule-day.component.scss'
})
export class ScheduleDayComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly scheduleService = inject(ScheduleService);
  private readonly dialog = inject(MatDialog);
  private readonly vehiclesApi = inject(VehiclesService);
  private readonly http = inject(HttpClient);
  vehicles: FleetVehicle[] = [];

  dateKey = '';
  weekStart = '';
  dayDate: Date | null = null;
  scheduled: ScheduleOrder[] = [];
  loading = false;
  errorMessage = '';
  searchTerm = '';
  serviceFilter: ServiceFilter = 'ALL';

  ngOnInit(): void {
    this.vehiclesApi.listActive().subscribe({ next: rows => this.vehicles = rows.filter(v => v.type !== 'CARRETA') });
    this.route.paramMap.subscribe(params => {
      this.dateKey = params.get('date') ?? '';
      this.weekStart = this.route.snapshot.queryParamMap.get('weekStart') ?? '';
      if (!this.isValidDateKey(this.dateKey)) {
        this.errorMessage = 'La fecha seleccionada no es válida.';
        return;
      }
      this.dayDate = this.parseDate(this.dateKey);
      this.loadDay();
    });
  }

  get filteredOrders(): ScheduleOrder[] {
    const term = this.searchTerm.trim().toLocaleLowerCase();
    return this.scheduled.filter(order => {
      const matchesType = this.serviceFilter === 'ALL' || order.serviceType === this.serviceFilter;
      const searchable = [
        order.internalOrderNumber,
        order.clientOrderNumber,
        order.clientName,
        order.reference,
        order.providerName,
        order.fclDetail?.containerNumber
      ].filter(value => value !== null && value !== undefined).join(' ').toLocaleLowerCase();
      return matchesType && (!term || searchable.includes(term));
    });
  }

  get fclOrders(): ScheduleOrder[] {
    return this.filteredOrders.filter(order => order.serviceType === 'FCL');
  }

  get lclOrders(): ScheduleOrder[] {
    return this.filteredOrders.filter(order => order.serviceType === 'LCL');
  }

  get otherOrders(): ScheduleOrder[] {
    return this.filteredOrders.filter(order => order.serviceType !== 'FCL' && order.serviceType !== 'LCL');
  }

  setFilter(filter: ServiceFilter): void {
    this.serviceFilter = filter;
  }

  hasVehicleOption(order: ScheduleOrder): boolean {
    return order.vehicleId != null && !this.vehicles.some(vehicle => vehicle.id === order.vehicleId);
  }

  assignVehicle(order: ScheduleOrder, value: string): void {
    const vehicleId = value ? Number(value) : null;
    this.http.patch(`/api/internal-service-orders/${order.id}/vehicle`, { vehicleId }).subscribe({
      next: () => this.loadDay(),
      error: err => { this.errorMessage = err?.error?.message || 'No se pudo asignar el vehículo.'; }
    });
  }

  openCreateInternalOrder(): void {
    const dialogRef = this.dialog.open(DirectInternalOrderDialogComponent, {
      width: '760px', maxWidth: '96vw', maxHeight: '92vh',
      data: { scheduledDate: this.dateKey },
      autoFocus: false
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) this.loadDay();
    });
  }

  loadDay(): void {
    this.loading = true;
    this.errorMessage = '';
    this.scheduleService.getSchedule(this.dateKey, this.dateKey).subscribe({
      next: response => {
        this.scheduled = response.scheduled ?? [];
        this.loading = false;
      },
      error: error => {
        this.errorMessage = error?.error?.message
          || error?.error?.error
          || 'No se pudo cargar la programación del día.';
        this.loading = false;
      }
    });
  }

  goBackToWeek(): void {
    const queryParams = this.isValidDateKey(this.weekStart) ? { weekStart: this.weekStart } : undefined;
    this.router.navigate(['/scheduling'], { queryParams });
  }

  private isValidDateKey(value: string): boolean {
    return /^\d{4}-\d{2}-\d{2}$/.test(value) && !Number.isNaN(new Date(`${value}T00:00:00`).getTime());
  }

  private parseDate(value: string): Date {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }
}
