import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Certificate } from '../models/certificate.models';

@Injectable({
	providedIn: 'root'
	})
	export class CertificateService {

	private readonly http = inject(HttpClient);

	private readonly apiUrl = '/api/certificates';

	downloadCertificate(attemptId: number): Observable<Blob> {
		return this.http.get(
		`${this.apiUrl}/attempts/${attemptId}`,
		{
			responseType: 'blob'
		}
		);
	}

	getCertificates(): Observable<Certificate[]> {
    return this.http.get<Certificate[]>(this.apiUrl);
	}

	downloadCertificateAsAdmin(attemptId: number): Observable<Blob> {
	return this.http.get(
		`${this.apiUrl}/attempts/${attemptId}/download`,
		{ responseType: 'blob' }
	);
	}
	
}