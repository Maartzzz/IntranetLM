
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { CommunicationService } from '../../core/services/communication.service';
import {
  CommunicationRequest,
  CommunicationResponse,
  CommunicationType
} from '../../core/models/communication.models';

@Component({
  selector: 'app-communication',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './communication.component.html',
  styleUrl: './communication.component.scss'
})
export class CommunicationComponent {

  private readonly communicationService = inject(CommunicationService);

  type: CommunicationType = 'SUGERENCIA';
  subject = '';
  description = '';

  sending = false;
  successMessage = '';
  errorMessage = '';
  communicationId: number | null = null;

  submit(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.communicationId = null;

    const normalizedSubject = this.subject.trim();
    const normalizedDescription = this.description.trim();

    if (!normalizedSubject || !normalizedDescription) {
      this.errorMessage = 'Completa el asunto y la descripción.';
      return;
    }

    if (normalizedSubject.length > 150) {
      this.errorMessage = 'El asunto no puede superar los 150 caracteres.';
      return;
    }

    if (normalizedDescription.length > 10000) {
      this.errorMessage = 'La descripción no puede superar los 10000 caracteres.';
      return;
    }

    const request: CommunicationRequest = {
      type: this.type,
      subject: normalizedSubject,
      description: normalizedDescription
    };

    this.sending = true;

    this.communicationService.submit(request).subscribe({
      next: (response: CommunicationResponse) => {
        this.communicationId = response.id;

        if (response.emailStatus === 'ENVIADO') {
          this.successMessage = response.message;
          this.resetForm();
        } else {
          this.errorMessage =
            `${response.message} Código de comunicación: ${response.id}.`;
        }

        this.sending = false;
      },
      error: (error) => {
        const responseMessage = error?.error?.message;

        this.errorMessage =
          typeof responseMessage === 'string'
            ? responseMessage
            : 'No se pudo registrar la comunicación. Inténtalo nuevamente.';

        this.sending = false;
      }
    });
  }

  private resetForm(): void {
    this.type = 'SUGERENCIA';
    this.subject = '';
    this.description = '';
  }
}
