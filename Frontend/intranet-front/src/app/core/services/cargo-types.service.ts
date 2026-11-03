import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CargoType, CargoTypeRequest } from '../models/cargo-types.models';

@Injectable({ providedIn: 'root' })
export class CargoTypeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/cargo-types';

  getActiveCargoTypes(): Observable<CargoType[]> {
    return this.http.get<CargoType[]>(this.apiUrl);
  }

  getAllCargoTypes(): Observable<CargoType[]> {
    return this.http.get<CargoType[]>(`${this.apiUrl}/admin`);
  }

  createCargoType(request: CargoTypeRequest): Observable<CargoType> {
    return this.http.post<CargoType>(this.apiUrl, request);
  }

  updateCargoType(id: number, request: CargoTypeRequest): Observable<CargoType> {
    return this.http.put<CargoType>(`${this.apiUrl}/${id}`, request);
  }

  changeStatus(id: number, active: boolean): Observable<CargoType> {
    return this.http.patch<CargoType>(`${this.apiUrl}/${id}/status`, { active });
  }
}
