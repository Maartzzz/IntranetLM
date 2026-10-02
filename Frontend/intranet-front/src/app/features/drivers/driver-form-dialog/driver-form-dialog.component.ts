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

    this.dialogRef.close(request);
  }
}