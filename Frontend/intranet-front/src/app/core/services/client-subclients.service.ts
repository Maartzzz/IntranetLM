import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClientSubclient, ClientSubclientRequest } from '../models/client-subclients.models';

@Injectable({ providedIn: 'root' })
export class ClientSubclientsService {
  private readonly http = inject(HttpClient);
  private readonly clientsUrl = '/api/clients';
  private readonly relationsUrl = '/api/client-subclients';

  getByClient(clientId: number): Observable<ClientSubclient[]> {
    return this.http.get<ClientSubclient[]>(`${this.clientsUrl}/${clientId}/subclients`);
  }

  create(clientId: number, request: ClientSubclientRequest): Observable<ClientSubclient> {
    return this.http.post<ClientSubclient>(`${this.clientsUrl}/${clientId}/subclients`, request);
  }

  changeStatus(relationId: number, active: boolean): Observable<ClientSubclient> {
    return this.http.patch<ClientSubclient>(`${this.relationsUrl}/${relationId}/status`, { active });
  }
}
