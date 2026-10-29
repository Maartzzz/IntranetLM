import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ExhaustedEvaluation } from '../models/exhausted-evaluation.models';

@Injectable({
  providedIn: 'root'
})
export class ExhaustedEvaluationService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/evaluations';

  getExhaustedEvaluations(): Observable<ExhaustedEvaluation[]> {
    return this.http.get<ExhaustedEvaluation[]>(
      `${this.apiUrl}/exhausted`
    );
  }

  rehabilitateEvaluation(
    courseId: number,
    username: string
  ): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/courses/${courseId}/users/${encodeURIComponent(username)}/rehabilitate`,
      {}
    );
  }
}
