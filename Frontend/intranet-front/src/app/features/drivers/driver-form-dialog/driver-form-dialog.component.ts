import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

import {
  CreateDriverRequest
} from '../../../core/services/drivers.service';
import { MatButtonModule } from '@angular/material/button';
import { User } from '../../../core/models/users.models';
import { Driver } from '../../../core/models/drivers.models';

@Component({
  selector: 'app-driver-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule
  ],
  templateUrl: './driver-form-dialog.component.html',
  styleUrl: './driver-form-dialog.component.scss'
})
export class DriverFormDialog {

  private readonly fb = inject(FormBuilder);

  private readonly dialogRef =
    inject(MatDialogRef<DriverFormDialog>);

  readonly data = inject(
      MAT_DIALOG_DATA
    ) as {
    users: User[];
    driver?: Driver;
  };

  form = this.fb.nonNullable.group({

    username: [
      '',
      Validators.required
    ],

    licenseNumber: [
      '',
      [
        Validators.required,
        Validators.maxLength(30)
      ]
    ],

    licenseCategory: [
      '',
      Validators.required
    ],

    licenseIssueDate: [
      '',
      Validators.required
    ],

    licenseExpiryDate: [
      '',
      Validators.required
    ]

  });

  constructor() {
    if (this.data.driver) {
      this.form.patchValue({
        username: this.data.driver.username,
        licenseNumber: this.data.driver.licenseNumber,
        licenseCategory: this.data.driver.licenseCategory,
        licenseIssueDate: this.data.driver.licenseIssueDate,
        licenseExpiryDate: this.data.driver.licenseExpiryDate
      });
      this.form.controls.username.disable();
    }
  }

  get isEditing(): boolean {
    return !!this.data.driver;
  }

  cancel(): void {
    this.dialogRef.close();
  }

  submit(): void {

    if (this.form.invalid) {

      this.form.markAllAsTouched();

      return;
    }

    const request: CreateDriverRequest = {
      ...this.form.getRawValue()
    };

    this.dialogRef.close({ mode: this.isEditing ? 'edit' : 'create', request });
  }
}