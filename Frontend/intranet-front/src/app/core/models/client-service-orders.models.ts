export interface ClientServiceOrderFclQuantity {
  cargoTypeId: number;
  cargoTypeName: string;
  totalQuantity: number;
}

export interface ClientServiceOrder {
  id: number;
  clientOrderNumber: string;
  clientId: number;
  clientName: string;
  operationId: number | null;
  operationNumber: string | null;
  reference: string | null;
  serviceType: 'FCL' | 'LCL';
  receivedDate: string | null;
  estimatedArrivalDate: string | null;
  origin: string | null;
  destination: string | null;
  observation: string | null;
  statusId: number;
  statusName: string;
  statusColor: string | null;
  fclQuantities: ClientServiceOrderFclQuantity[];
  totalPackages: number | null;
  createdAt: string;
  updatedAt: string;
}

export interface ClientServiceOrderRequest {
  clientOrderNumber: string;
  clientId: number;
  operationId: number;
  reference: string | null;
  serviceType: 'FCL' | 'LCL';
  receivedDate: string | null;
  estimatedArrivalDate: string | null;
  origin: string | null;
  destination: string | null;
  observation: string | null;
  statusId: number | null;
  fclQuantities: { cargoTypeId: number; totalQuantity: number }[];
  totalPackages: number | null;
}
