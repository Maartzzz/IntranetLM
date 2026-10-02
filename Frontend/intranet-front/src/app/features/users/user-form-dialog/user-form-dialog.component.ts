import { Component, inject, OnInit } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  MatDialogRef, MAT_DIALOG_DATA
} from '@angular/material/dialog';

import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import {
  UpdateUserRequest,
  UserRequest,
  UserService
} from '../../../core/services/users.service';
import { User } from '../../../core/models/users.models';

export interface UserFormDialogData {
  mode: 'create' | 'edit';
  user?: User;
}

@Component({
  selector: 'app-user-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './user-form-dialog.component.html',
  styleUrl: './user-form-dialog.component.scss'
})
export class UserFormDialog implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly data = inject<UserFormDialogData>(MAT_DIALOG_DATA);

  private readonly dialogRef =
    inject(MatDialogRef<UserFormDialog>);

  get isEditMode(): boolean {
    return this.data.mode === 'edit';
  }

  ngOnInit(): void {

    if (!this.isEditMode || !this.data.user)
      return;

    const user = this.data.user;

    this.userForm.patchValue({
      username: user.username,
      email: user.email ?? '',
      firstName: user.firstName,
      lastName: user.lastName,
      documentType: user.documentType,
      documentNumber: user.documentNumber,
      phone: user.phone ?? '',
      role: user.role
    });

    this.userForm.controls.username.disable();
    this.userForm.controls.password.clearValidators();
    this.userForm.controls.password.updateValueAndValidity();

  }

  readonly userForm = this.fb.nonNullable.group({

    username: [
      '',
      [
        Validators.required,
        Validators.maxLength(50)
      ]
    ],

    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(100)
      ]
    ],

    email: [
      '',
      [
        Validators.email,
        Validators.maxLength(150)
      ]
    ],

    firstName: [
      '',
      [
        Validators.required,
        Validators.maxLength(100)
      ]
    ],

    lastName: [
      '',
      [
        Validators.required,
        Validators.maxLength(100)
      ]
    ],

    documentType: [
      'DNI',
      Validators.required
    ],

    documentNumber: [
      '',
      [
        Validators.required,
        Validators.maxLength(20)
      ]
    ],

    phone: [
      '',
      Validators.maxLength(20)
    ],

    role: [
      'CHOFER',
      Validators.required
    ]

  });

  isSaving = false;
  errorMessage = '';

  save(): void {

  if (this.userForm.invalid) {
    this.userForm.markAllAsTouched();
    return;
  }

  this.isSaving = true;
  this.errorMessage = '';

  const formValue = this.userForm.getRawValue();

  if (this.isEditMode && this.data.user) {

    const request: UpdateUserRequest = {
      email: formValue.email || null,
      firstName: formValue.firstName,
      lastName: formValue.lastName,
      documentType: formValue.documentType,
      documentNumber: formValue.documentNumber,
      phone: formValue.phone || null,
      role: formValue.role
    };

    this.userService
      .updateUser(
        this.data.user.username,
        request
      )
      .subscribe({

        next: user => {
          this.dialogRef.close(user);
        },

        error: error => {
          console.error(
            'Error actualizando usuario:',
            error
          );

          this.isSaving = false;

          if (error.status === 409) {
            this.errorMessage =
              error.error?.message ||
              'El email o documento ya está registrado.';
          } else {
            this.errorMessage =
              'No se pudo actualizar el usuario.';
          }
        }

      });

    return;
  }

  const request: UserRequest = {
    username: formValue.username,
    password: formValue.password,
    email: formValue.email || null,
    firstName: formValue.firstName,
    lastName: formValue.lastName,
    documentType: formValue.documentType,
    documentNumber: formValue.documentNumber,
    phone: formValue.phone || null,
    role: formValue.role
  };

  this.userService
    .createUser(request)
    .subscribe({

      next: user => {
        this.dialogRef.close(user);
      },

      error: error => {
        console.error(
          'Error creando usuario:',
          error
        );

        this.isSaving = false;

        if (error.status === 409) {
          this.errorMessage =
            error.error?.message ||
            'El usuario o alguno de sus datos ya está registrado.';
        } else {
          this.errorMessage =
            'No se pudo crear el usuario.';
        }
      }

    });
}

  cancel(): void {
    this.dialogRef.close();
  }
}