export interface InternalServiceOrder {
  id: number;
  internalOrderNumber: number;
  clientId: number;
  clientName: string;
  clientServiceOrderId: number | null;
  clientOrderNumber: string;
  serviceType: 'FCL' | 'LCL';
  reference: string | null;
  originLocationId: number;
  originName: string;
  destinationLocationId: number;
  destinationName: string;
  providerId: number | null;
  providerName: string | null;
  scheduledDate: string | null;
  statusId: number | null;
  statusName: string | null;
  billingStatus: 'PENDIENTE' | 'FACTURADO';
  observations: string | null;
  detail: unknown;
  createdAt: string;
  updatedAt: string;
}

export interface ClientServiceOrderDistribution {
  clientServiceOrderId: number;
  clientOrderNumber: string;
  serviceType: 'FCL' | 'LCL';
  fcl: {
    cargoTypeId: number;
    cargoTypeName: string;
    requested: number;
    distributed: number;
    available: number;
  }[];
  requestedPackages: number | null;
  distributedPackages: number | null;
  availablePackages: number | null;
}

export interface InternalServiceOrderRequest {
  clientId: number;
  clientServiceOrderId: number;
  clientOrderNumber: string;
  serviceType: 'FCL' | 'LCL';
  reference: string | null;
  originLocationId: number;
  destinationLocationId: number;
  providerId: number | null;
  scheduledDate: string | null;
  statusId: number | null;
  billingStatus: 'PENDIENTE' | 'FACTURADO';
  observations: string | null;
  fclDetail: {
    containerNumber: string | null;
    cargoTypeId: number | null;
    cubicMeters: number | null;
    emptyWarehouseLocationId: number | null;
    returnDueDate: string | null;
    memo: string | null;
  } | null;
  lclDetail: {
    packages: number | null;
    weightKg: number | null;
    cubicMeters: number | null;
  } | null;
  scheduleReason: string | null;
}
