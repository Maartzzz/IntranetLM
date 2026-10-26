export type CommunicationType = 'SUGERENCIA' | 'QUEJA' | 'OTRO';

export type CommunicationStatus = 'PENDIENTE' | 'ENVIADO' | 'ERROR';

export interface CommunicationRequest {
  type: CommunicationType;
  subject: string;
  description: string;
}

export interface CommunicationResponse {
  id: number;
  createdAt: string;
  emailStatus: CommunicationStatus;
  message: string;
}
