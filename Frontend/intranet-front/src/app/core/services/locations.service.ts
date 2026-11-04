import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientSubclient, ClientSubclientLocation, ClientSubclientLocationRequest, Location, LocationRequest } from '../models/locations.models';

@Injectable({ providedIn: 'root' })
export class LocationsService {
  private readonly http = inject(HttpClient);
  private readonly locationsUrl = '/api/locations';
  listActive(): Observable<Location[]> { return this.http.get<Location[]>(this.locationsUrl); }
  listAll(): Observable<Location[]> { return this.http.get<Location[]>(`${this.locationsUrl}/admin`); }
  create(request: LocationRequest): Observable<Location> { return this.http.post<Location>(this.locationsUrl, request); }
  update(id: number, request: LocationRequest): Observable<Location> { return this.http.put<Location>(`${this.locationsUrl}/${id}`, request); }
  changeStatus(id: number, active: boolean): Observable<Location> { return this.http.patch<Location>(`${this.locationsUrl}/${id}/status`, { active }); }
  addSubclient(clientId: number, subclientId: number): Observable<ClientSubclient> {
    return this.http.post<ClientSubclient>(`/api/clients/${clientId}/subclients`, { subclientId });
  }
  changeRelationStatus(relationId: number, active: boolean): Observable<ClientSubclient> {
    return this.http.patch<ClientSubclient>(`/api/client-subclients/${relationId}/status`, { active });
  }
  listClientSubclients(clientId: number): Observable<ClientSubclient[]> {
    return this.http.get<ClientSubclient[]>(`/api/clients/${clientId}/subclients`);
  }
  listRelationLocations(relationId: number): Observable<ClientSubclientLocation[]> {
    return this.http.get<ClientSubclientLocation[]>(`/api/client-subclients/${relationId}/locations`);
  }
  addRelationLocation(relationId: number, request: ClientSubclientLocationRequest): Observable<ClientSubclientLocation> {
    return this.http.post<ClientSubclientLocation>(`/api/client-subclients/${relationId}/locations`, request);
  }
  updateRelationLocation(id: number, request: ClientSubclientLocationRequest): Observable<ClientSubclientLocation> {
    return this.http.put<ClientSubclientLocation>(`/api/client-subclient-locations/${id}`, request);
  }
  changeRelationLocationStatus(id: number, active: boolean): Observable<ClientSubclientLocation> {
    return this.http.patch<ClientSubclientLocation>(`/api/client-subclient-locations/${id}/status`, { active });
  }
}
