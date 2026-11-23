import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuditFilters, AuditLog } from '../models/audit-log.models';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/audit';

  getAudit(filters: AuditFilters = {}): Observable<AuditLog[]> {
    let params = new HttpParams();
    if (filters.module) params = params.set('module', filters.module);
    if (filters.entityType) params = params.set('entityType', filters.entityType);
    if (filters.entityId !== undefined) params = params.set('entityId', String(filters.entityId));
    if (filters.entityKey) params = params.set('entityKey', filters.entityKey);
    return this.http.get<AuditLog[]>(this.apiUrl, { params });
  }
}
