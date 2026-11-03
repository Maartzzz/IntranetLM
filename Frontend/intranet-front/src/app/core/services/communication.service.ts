import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  CommunicationRequest,
  CommunicationResponse
} from '../models/communication.models';

@Injectable({
  providedIn: 'root'
})
export class CommunicationService {

  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/communications';

  submit(
    request: CommunicationRequest
  ): Observable<CommunicationResponse> {
    return this.http.post<CommunicationResponse>(
      this.apiUrl,
      request
    );
  }
}
