import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Operation, OperationRequest, OperationRequirement, OperationRequirementRequest, RequirementCatalog, RequirementCatalogRequest, Status } from '../models/operations.models';
import { OperationActivity } from '../models/operation-activity.models';

@Injectable({ providedIn: 'root' })
export class OperationsService {
  private readonly http = inject(HttpClient);
  private readonly operationsUrl = '/api/operations';
  private readonly catalogUrl = '/api/requirement-catalog';

  getOperations(clientId?: number): Observable<Operation[]> {
    let params = new HttpParams();
    if (clientId !== undefined) params = params.set('clientId', clientId);
    return this.http.get<Operation[]>(this.operationsUrl, { params });
  }

  getOperation(id: number): Observable<Operation> {
    return this.http.get<Operation>(`${this.operationsUrl}/${id}`);
  }

  createOperation(request: OperationRequest): Observable<Operation> {
    return this.http.post<Operation>(this.operationsUrl, request);
  }

  updateOperation(id: number, request: OperationRequest): Observable<Operation> {
    return this.http.put<Operation>(`${this.operationsUrl}/${id}`, request);
  }

  getRequirementCatalog(activeOnly = true): Observable<RequirementCatalog[]> {
    return this.http.get<RequirementCatalog[]>(this.catalogUrl, {
      params: new HttpParams().set('activeOnly', activeOnly)
    });
  }

    getActivity(operationId: number): Observable<OperationActivity[]> {
    return this.http.get<OperationActivity[]>(`${this.operationsUrl}/${operationId}/activity`);
  }

  createRequirementCatalog(
    request: RequirementCatalogRequest
  ): Observable<RequirementCatalog> {
    return this.http.post<RequirementCatalog>(this.catalogUrl, request);
  }

  updateRequirementCatalog(
    id: number,
    request: RequirementCatalogRequest
  ): Observable<RequirementCatalog> {
    return this.http.put<RequirementCatalog>(
      `${this.catalogUrl}/${id}`,
      request
    );
  }

  getRequirements(operationId: number): Observable<OperationRequirement[]> {
    return this.http.get<OperationRequirement[]>(`${this.operationsUrl}/${operationId}/requirements`);
  }

  createRequirement(operationId: number, request: OperationRequirementRequest): Observable<OperationRequirement> {
    return this.http.post<OperationRequirement>(`${this.operationsUrl}/${operationId}/requirements`, request);
  }

  updateRequirement(operationId: number, requirementId: number, request: OperationRequirementRequest): Observable<OperationRequirement> {
    return this.http.put<OperationRequirement>(`${this.operationsUrl}/${operationId}/requirements/${requirementId}`, request);
  }

  getStatuses(processCode: 'OPERACION' | 'REQUISITO'): Observable<Status[]> {
    return this.http.get<Status[]>('/api/statuses', {
      params: new HttpParams().set('processCode', processCode)
    });
  }
}