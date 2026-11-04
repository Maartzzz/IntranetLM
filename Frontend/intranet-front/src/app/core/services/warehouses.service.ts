import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Warehouse, WarehouseRequest, WarehouseLocation } from '../models/warehouses.models';

@Injectable({ providedIn: 'root' })
export class WarehousesService {
  private readonly http = inject(HttpClient);
  private readonly url = '/api/warehouses';
  listActive(): Observable<Warehouse[]> { return this.http.get<Warehouse[]>(this.url); }
  listAll(): Observable<Warehouse[]> { return this.http.get<Warehouse[]>(`${this.url}/admin`); }
  create(request: WarehouseRequest): Observable<Warehouse> { return this.http.post<Warehouse>(this.url, request); }
  update(id: number, request: WarehouseRequest): Observable<Warehouse> { return this.http.put<Warehouse>(`${this.url}/${id}`, request); }
  changeStatus(id: number, active: boolean): Observable<Warehouse> { return this.http.patch<Warehouse>(`${this.url}/${id}/status`, { active }); }
  listLocations(): Observable<WarehouseLocation[]> { return this.http.get<WarehouseLocation[]>('/api/locations'); }
  createLocation(request: {
    name: string;
    locationType: 'ALMACEN' | 'DEPOSITO' | 'PUERTO' | 'PLANTA' | 'CENTRO_DISTRIBUCION' | 'OTRO';
    address: string;
    reference: string | null;
    district: string | null;
    province: string | null;
    department: string | null;
    latitude: number | null;
    longitude: number | null;
  }): Observable<WarehouseLocation> {
    return this.http.post<WarehouseLocation>('/api/locations', request);
  }
}
