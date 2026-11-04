export type LocationType = 'ALMACEN' | 'DEPOSITO' | 'PUERTO' | 'PLANTA' | 'CENTRO_DISTRIBUCION' | 'OTRO';
export type LocationUsageType = 'RECOJO' | 'ENTREGA' | 'AMBOS';

export interface Location {
  id: number; name: string; locationType: LocationType; address: string;
  reference: string | null; district: string | null; province: string | null;
  department: string | null; latitude: number | null; longitude: number | null;
  active: boolean; createdAt: string; updatedAt: string;
}
export interface LocationRequest {
  name: string; locationType: LocationType; address: string;
  reference: string | null; district: string | null; province: string | null;
  department: string | null; latitude: number | null; longitude: number | null;
}
export interface ClientSubclient {
  id: number; clientId: number; clientName: string; subclientId: number;
  subclientName: string; active: boolean; createdAt: string; updatedAt: string;
}
export interface ClientSubclientLocation {
  id: number; clientSubclientId: number; locationId: number;
  locationName: string; address: string; locationType: string;
  usageType: LocationUsageType; contactName: string | null; contactPhone: string | null;
  operatingHours: string | null; instructions: string | null; active: boolean;
  createdAt: string; updatedAt: string;
}
export interface ClientSubclientLocationRequest {
  locationId: number; usageType: LocationUsageType; contactName: string | null;
  contactPhone: string | null; operatingHours: string | null; instructions: string | null;
}
