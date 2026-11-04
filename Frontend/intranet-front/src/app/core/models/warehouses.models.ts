export type WarehouseType = 'ALMACEN_DEVOLUCION' | 'ALMACEN_VACIOS' | 'ALMACEN_CARGA' | 'DEPOSITO_TEMPORAL' | 'OTRO';
export interface Warehouse {
  id: number; locationId: number; locationName: string; address: string;
  district: string | null; province: string | null; warehouseCode: string;
  warehouseName: string; warehouseType: WarehouseType; operatorName: string;
  active: boolean; observations: string | null; createdAt: string; updatedAt: string;
}
export interface WarehouseRequest {
  locationId: number; warehouseCode: string; warehouseName: string;
  warehouseType: WarehouseType; operatorName: string; observations: string | null;
}
export interface WarehouseLocation {
  id: number; name: string; address: string; district: string | null;
  province: string | null; active: boolean;
}
