import { Component, OnInit, inject } from '@angular/core';

import { UserService } from '../../core/services/users.service';
import { User } from '../../core/models/users.models';
import { MatDialog } from '@angular/material/dialog';
import { UserFormDialog } from './user-form-dialog/user-form-dialog.component';
import { FormsModule } from '@angular/forms';
import { ConfirmDialog } from './confirm-dialog/confirm-dialog.component';
import { MatButton } from '@angular/material/button';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [FormsModule, MatButton],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss'
})
export class UsersComponent implements OnInit {

  searchTerm = '';
  selectedRole = 'ALL';
  selectedStatus = 'ACTIVE';

  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);

  users: User[] = [];
  loading = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {

    this.loading = true;
    this.errorMessage = '';

    this.userService
      .getUsers()
      .subscribe({
        next: users => {
          this.users = users;
          this.loading = false;
        },

        error: error => {
          console.error(
            'Error cargando usuarios:',
            error
          );

          this.errorMessage =
            'No se pudieron cargar los usuarios.';

          this.loading = false;
        }
      });
  }

  openCreateDialog(): void {

    const dialogRef = this.dialog.open(UserFormDialog, {
      width: '600px',
      maxWidth: '90vh',
      data: {
        mode: 'create'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  openEditDialog(user: User): void {

    const dialogRef = this.dialog.open(
      UserFormDialog,
      {
        width: '600px',
        maxWidth: '90Vh',
        data: {
          mode: 'edit',
          user
        }
      }
    );

    dialogRef.afterClosed().subscribe(result => {

      if (result) {
        this.loadUsers();
      }

    });
  }

toggleActive(user: User): void {

  const newStatus = !user.active;

  const action = newStatus
    ? 'activar'
    : 'desactivar';

  const dialogRef = this.dialog.open(
    ConfirmDialog,
    {
      width: '420px',
      maxWidth: 'calc(100vw - 32px)',
      data: {
        title: `${newStatus ? 'Activar' : 'Desactivar'} usuario`,
        message:
          `¿Deseas ${action} al usuario ` +
          `${user.firstName} ${user.lastName}?`,
        confirmText:
          newStatus ? 'Activar' : 'Desactivar',
        cancelText: 'Cancelar'
      }
    }
  );

  dialogRef.afterClosed().subscribe(
    confirmed => {

      if (!confirmed) {
        return;
      }

      this.userService
        .setActive(
          user.username,
          newStatus
        )
        .subscribe({

          next: () => {
            user.active = newStatus;
          },

          error: error => {

            console.error(
              'Error cambiando estado:',
              error
            );

            this.errorMessage =
              'No se pudo cambiar el estado del usuario.';
          }

        });

    }
  );
}

  get filteredUsers(): User[] {
    const search = this.searchTerm.trim().toLowerCase();

    return this.users.filter(user => {

      const matchesSearch = !search ||
        user.username.toLowerCase().includes(search) ||
        user.email?.toLowerCase().includes(search) ||
        user.firstName.toLowerCase().includes(search) ||
        user.lastName.toLowerCase().includes(search) ||
        user.documentNumber.toLowerCase().includes(search) ||
        user.phone?.toLowerCase().includes(search);

      const matchesRole = this.selectedRole === 'ALL' ||  
        user.role === this.selectedRole;

      const matchesStatus = this.selectedStatus === 'ALL' ||
        (this.selectedStatus === 'ACTIVE' && user.active) ||
        (this.selectedStatus === 'INACTIVE' && !user.active);

      return (matchesSearch && matchesRole && matchesStatus); 
    });
  }

}