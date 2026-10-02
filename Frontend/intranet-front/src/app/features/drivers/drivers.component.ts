import { Component, OnInit, inject } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { CreateDriverRequest, DriverService } from '../../core/services/drivers.service';
import { Driver } from '../../core/models/drivers.models';
import { MatDialog } from '@angular/material/dialog';
import { DriverFormDialog } from './driver-form-dialog/driver-form-dialog.component';
import { User } from '../../core/models/users.models';
import { UserService } from '../../core/services/users.service';
import { DriverDetailDialog } from './driver-detail-dialog/driver-detail-dialog.component';

@Component({
  selector: 'app-drivers',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './drivers.component.html',
  styleUrl: './drivers.component.scss'
})
export class DriversComponent implements OnInit {

  private readonly dialog = inject(MatDialog);

  private readonly driverService =
    inject(DriverService);

  private readonly userService =
    inject(UserService);

  drivers: Driver[] = [];
  users: User[] = [];

  searchTerm = '';
  selectedCategory = 'ALL';
  selectedStatus = 'ALL';

  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadDrivers();
    this.loadUsers();
  }

  loadDrivers(): void {

    this.loading = true;
    this.errorMessage = '';

    this.driverService
      .getDrivers()
      .subscribe({

        next: drivers => {
          this.drivers = drivers;
          this.loading = false;
        },

        error: error => {

          console.error(
            'Error cargando conductores:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los conductores.';

          this.loading = false;
        }

      });
  }

  get filteredDrivers(): Driver[] {

    const search =
      this.searchTerm
        .trim()
        .toLowerCase();

    return this.drivers.filter(driver => {

      const matchesSearch =
        !search ||
        driver.username
          .toLowerCase()
          .includes(search) ||
        driver.firstName
          .toLowerCase()
          .includes(search) ||
        driver.lastName
          .toLowerCase()
          .includes(search) ||
        driver.documentNumber
          .toLowerCase()
          .includes(search) ||
        driver.licenseNumber
          .toLowerCase()
          .includes(search);

      const matchesCategory =
        this.selectedCategory === 'ALL' ||
        driver.licenseCategory ===
          this.selectedCategory;

      const matchesStatus =
        this.selectedStatus === 'ALL' ||
        (
          this.selectedStatus === 'ACTIVE' &&
          driver.active
        ) ||
        (
          this.selectedStatus === 'INACTIVE' &&
          !driver.active
        );

      return (
        matchesSearch &&
        matchesCategory &&
        matchesStatus
      );
    });
  }

  createDriver(request: CreateDriverRequest): void {

    this.loading = true;
    this.errorMessage = '';

    this.driverService
      .createDriver(request)
      .subscribe({

        next: () => {

          this.loading = false;

          this.loadDrivers();
        },

        error: error => {

          console.error(
            'Error creando conductor:',
            error
          );

          this.loading = false;

          if (error.status === 409) {

            this.errorMessage =
              error.error?.message ??
              'El usuario o número de licencia ya está registrado.';

          } else if (error.status === 400) {

            this.errorMessage =
              error.error?.message ??
              'Los datos del conductor no son válidos.';

          } else {

            this.errorMessage =
              'No se pudo crear el conductor.';
          }
        }

      });
  }

  openCreateDialog(): void {

    const availableDrivers =
      this.getAvailableDrivers();

    const dialogRef = this.dialog.open(
      DriverFormDialog,
      {
        width: '500px',
        maxWidth: 'calc(100vw - 32px)',

        data: {
          users: availableDrivers
        }
      }
    );

    dialogRef.afterClosed().subscribe(
      result => {

        if (!result) {
          return;
        }

        this.createDriver(result);
      }
    );
  }

  getAvailableDrivers(): User[] {

    return this.users.filter(user => {

      const isDriver =
        user.role === 'CHOFER';

      const alreadyRegistered =
        this.drivers.some(
          driver =>
            driver.username === user.username
        );

      return (
        isDriver &&
        user.active &&
        !alreadyRegistered
      );
    });
  }

  loadUsers(): void {

    this.userService
      .getUsers()
      .subscribe({

        next: users => {
          this.users = users;
        },

        error: error => {

          console.error(
            'Error cargando usuarios:',
            error
          );

        }

      });
  }

  openDetailDialog(driver: Driver): void {

    this.dialog.open(
      DriverDetailDialog,
      {
        width: '550px',
        maxWidth: 'calc(100vw - 32px)',
        data: driver
      }
    );
  }
  
}