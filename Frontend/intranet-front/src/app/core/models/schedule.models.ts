export interface ScheduleFclDetail {
  containerNumber: string | null;
  cargoTypeName: string | null;
  cubicMeters: number | null;
  emptyWarehouseLocationId: number | null;
  emptyWarehouseLocationName: string | null;
  returnDueDate: string | null;
  memo: string | null;
}

export interface ScheduleLclDetail {
  packages: number | null;
  weightKg: number | null;
  cubicMeters: number | null;
}

export interface ScheduleOrder {
  id: number;
  internalOrderNumber: number;
  clientId: number;
  clientName: string;
  clientServiceOrderId: number | null;
  clientOrderNumber: string | null;
  serviceType: 'FCL' | 'LCL' | string;
  reference: string | null;
  originLocationId: number | null;
  originName: string | null;
  destinationLocationId: number | null;
  destinationName: string | null;
  providerId: number | null;
  providerName: string | null;
  scheduledDate: string | null;
  statusId: number | null;
  statusName: string | null;
  vehicleId: number | null;
  assignedVehiclePlate: string | null;
  assignedVehicleType: string | null;
  assignedTrailerPlate: string | null;
  billingStatus: string | null;
  observations: string | null;
  fclDetail: ScheduleFclDetail | null;
  lclDetail: ScheduleLclDetail | null;
  clientSubclientId?: number | null;
  subclientName?: string | null;
}

export interface ScheduleBoardResponse {
  startDate: string;
  endDate: string;
  scheduled: ScheduleOrder[];
  available: ScheduleOrder[];
}


export interface ScheduleVersionSummary {
  id: number;
  versionNumber: number;
  startDate: string;
  endDate: string;
  createdByUsername: string;
  createdByName: string;
  createdAt: string;
  scheduledOrderCount: number;
}

export interface ScheduleVersionDetail extends ScheduleVersionSummary {
  snapshot: ScheduleOrder[];
}


export interface ScheduleStatusOption {
  id: number;
  processCode: string;
  name: string;
  description: string | null;
  color: string | null;
  active: boolean;
  initial: boolean;
  finalStatus: boolean;
  autoTransition: boolean;
  allowsScheduling: boolean;
}
