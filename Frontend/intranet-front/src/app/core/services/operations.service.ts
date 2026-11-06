import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Operation, OperationRequest, OperationRequirement, OperationRequirementRequest, RequirementCatalog, RequirementCatalogRequest, Status } from '../models/operations.models';
import { OperationActivity } from '../models/operation-activity.models';
import { ClientServiceOrder, ClientServiceOrderRequest } from '../models/client-service-orders.models';
import { ClientServiceOrderDistribution, InternalServiceOrder, InternalServiceOrderRequest } from '../models/internal-service-orders.models';

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


  getClientServiceOrders(clientId: number): Observable<ClientServiceOrder[]> {
    return this.http.get<ClientServiceOrder[]>('/api/client-service-orders', {
      params: new HttpParams().set('clientId', clientId)
    });
  }

  createClientServiceOrder(request: ClientServiceOrderRequest): Observable<ClientServiceOrder> {
    return this.http.post<ClientServiceOrder>('/api/client-service-orders', request);
  }

  getInternalServiceOrders(clientId: number): Observable<InternalServiceOrder[]> {
    return this.http.get<InternalServiceOrder[]>('/api/internal-service-orders', {
      params: new HttpParams().set('clientId', clientId)
    });
  }

  createInternalServiceOrder(request: InternalServiceOrderRequest): Observable<InternalServiceOrder> {
    return this.http.post<InternalServiceOrder>('/api/internal-service-orders', request);
  }

  getClientServiceOrderDistribution(clientServiceOrderId: number): Observable<ClientServiceOrderDistribution> {
    return this.http.get<ClientServiceOrderDistribution>(
      `/api/client-service-orders/${clientServiceOrderId}/distribution`
    );
  }

  getStatuses(processCode: 'OPERACION' | 'REQUISITO' | 'OS_INTERNA'): Observable<Status[]> {
    return this.http.get<Status[]>('/api/statuses', {
      params: new HttpParams().set('processCode', processCode)
    });
  }
}