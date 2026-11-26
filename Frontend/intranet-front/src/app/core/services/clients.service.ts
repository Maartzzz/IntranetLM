import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Client, ClientRequest } from '../models/clients.models';
import { Location, LocationRequest } from '../models/locations.models';

@Injectable({
  providedIn: 'root'
})
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/clients';

  getActiveClients(): Observable<Client[]> {
    return this.http.get<Client[]>(this.apiUrl);
  }

  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.apiUrl}/admin`);
  }

  createClient(request: ClientRequest): Observable<Client> {
    return this.http.post<Client>(this.apiUrl, request);
  }

  updateClient(id: number, request: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.apiUrl}/${id}`, request);
  }

  getClientLocations(id: number): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.apiUrl}/${id}/locations`);
  }

  addClientLocation(id: number, request: LocationRequest): Observable<Location> {
    return this.http.post<Location>(`${this.apiUrl}/${id}/locations`, request);
  }

  updateClientLocation(clientId: number, locationId: number, request: LocationRequest): Observable<Location> {
    return this.http.put<Location>(`${this.apiUrl}/${clientId}/locations/${locationId}`, request);
  }

  changeClientLocationStatus(clientId: number, locationId: number, active: boolean): Observable<Location> {
    return this.http.patch<Location>(`${this.apiUrl}/${clientId}/locations/${locationId}/status`, { active });
  }

  changeStatus(id: number, active: boolean): Observable<Client> {
    return this.http.patch<Client>(`${this.apiUrl}/${id}/status`, { active });
  }
}
