import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Dashboard } from '../models/dashboard.models';

@Injectable({
	providedIn: 'root'
})
export class DashboardService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/dashboard';

	getDashboard(): Observable<Dashboard> {
		return this.http.get<Dashboard>(this.apiUrl);
	}
}