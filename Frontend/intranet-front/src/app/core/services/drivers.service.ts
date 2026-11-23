import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Driver } from '../models/drivers.models';

export interface CreateDriverRequest {
    username: string;
    licenseNumber: string;
    licenseCategory: string;
    licenseIssueDate: string;
    licenseExpiryDate: string;
    }

    @Injectable({
    providedIn: 'root'
    })
    export class DriverService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/drivers';

    getDrivers(): Observable<Driver[]> {
        return this.http.get<Driver[]>(
        this.apiUrl
        );
    }

    createDriver(
        request: CreateDriverRequest
    ): Observable<Driver> {

        return this.http.post<Driver>(
        this.apiUrl,
        request
        );
    }

    updateDriver(username: string, request: Omit<CreateDriverRequest, 'username'>): Observable<Driver> {
        return this.http.put<Driver>(`${this.apiUrl}/${encodeURIComponent(username)}`, request);
    }

    setDriverActive(username: string, active: boolean): Observable<void> {
        return this.http.patch<void>(
            `${this.apiUrl}/${encodeURIComponent(username)}/active`,
            null,
            { params: { active: String(active) } }
        );
    }

    getDriver(username: string): Observable<Driver> {

        return this.http.get<Driver>(
            `${this.apiUrl}/${username}`
        );
    }
}