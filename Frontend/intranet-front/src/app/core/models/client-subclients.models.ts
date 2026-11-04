export interface ClientSubclient {
  id: number;
  clientId: number;
  clientName: string;
  subclientId: number;
  subclientName: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ClientSubclientRequest {
  subclientId: number;
}
