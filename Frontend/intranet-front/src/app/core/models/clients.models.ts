export type ClientDocumentType = 'RUC' | 'DNI' | 'CE';

export interface Client {
  id: number;
  documentType: ClientDocumentType;
  documentNumber: string;
  businessName: string;
  address: string | null;
  active: boolean;
  parentClientId: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClientRequest {
  documentType: ClientDocumentType;
  documentNumber: string;
  businessName: string;
  address: string | null;
}
