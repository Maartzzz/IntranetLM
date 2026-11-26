import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RolePermission {
  id: number; code: string; name: string; description: string | null;
  moduleCode: string; moduleName: string; moduleOrder: number;
}
export interface RoleItem {
  id: number; code: string; name: string; description: string | null;
  active: boolean; systemRole: boolean; protectedRole: boolean;
  userCount: number; permissionCount: number; permissionIds: number[];
}
export interface RolePayload {
  code?: string; name: string; description: string; permissionIds: number[];
}

@Injectable({ providedIn: 'root' })
export class RolesService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/roles';
  getRoles(): Observable<RoleItem[]> { return this.http.get<RoleItem[]>(this.apiUrl); }
  getPermissions(): Observable<RolePermission[]> { return this.http.get<RolePermission[]>(`${this.apiUrl}/permissions`); }
  create(payload: RolePayload): Observable<RoleItem> { return this.http.post<RoleItem>(this.apiUrl, payload); }
  update(id: number, payload: RolePayload): Observable<RoleItem> { return this.http.put<RoleItem>(`${this.apiUrl}/${id}`, payload); }
  setActive(id: number, active: boolean): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/active`, null, { params: { active } });
  }
}
