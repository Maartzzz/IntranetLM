import { Component, inject } from '@angular/core';

import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef
} from '@angular/material/dialog';

import { MatButtonModule } from '@angular/material/button';

import { Driver }
  from '../../../core/models/drivers.models';

@Component({
  selector: 'app-driver-detail-dialog',
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule
  ],
  templateUrl: './driver-detail-dialog.component.html',
  styleUrl: './driver-detail-dialog.component.scss'
})
export class DriverDetailDialog {

  readonly data = inject(
    MAT_DIALOG_DATA
  ) as Driver;

  private readonly dialogRef =
    inject(MatDialogRef<DriverDetailDialog>);

  get licenseStatus():
    'VALID' | 'EXPIRING' | 'EXPIRED' {

    const expiryDate =
      new Date(this.data.licenseExpiryDate);

    const today = new Date();

    today.setHours(0, 0, 0, 0);
    expiryDate.setHours(0, 0, 0, 0);

    if (expiryDate < today) {
      return 'EXPIRED';
    }

    const difference =
      expiryDate.getTime() -
      today.getTime();

    const days =
      difference /
      (1000 * 60 * 60 * 24);

    if (days <= 30) {
      return 'EXPIRING';
    }

    return 'VALID';
  }

  close(): void {
    this.dialogRef.close();
  }
}