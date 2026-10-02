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

    getDriver(username: string): Observable<Driver> {

        return this.http.get<Driver>(
            `${this.apiUrl}/${username}`
        );
    }
}