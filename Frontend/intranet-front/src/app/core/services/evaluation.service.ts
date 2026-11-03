import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
    EvaluationStartResponse,
    EvaluationSubmitRequest,
    EvaluationResultResponse,
    EvaluationAttempt,
    PendingEvaluation,
    EvaluationAnswerProgressRequest,
    EvaluationResumeResponse,
    MyEvaluationResponse
} from '../models/evaluation.models';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class EvaluationService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/evaluations';

    startEvaluation(courseId: number): Observable<EvaluationStartResponse> {
        return this.http.post<EvaluationStartResponse>(
        `${this.apiUrl}/courses/${courseId}/start`,
        {}
        );
    }

    submitEvaluation(
        attemptId: number,
        request: EvaluationSubmitRequest
    ): Observable<EvaluationResultResponse> {
        return this.http.post<EvaluationResultResponse>(
        `${this.apiUrl}/attempts/${attemptId}/submit`,
        request
        );
    }

    cancelEvaluation(attemptId: number): Observable<void> {
        return this.http.delete<void>(
            `${this.apiUrl}/attempts/${attemptId}`
        );
    }

    getCompletedAttemptsByCourse(courseId: number):
        Observable<EvaluationAttempt[]> {
        return this.http.get<EvaluationAttempt[]>(
            `/api/evaluations/courses/${courseId}/attempts`
        );
    }

    
    getPendingEvaluations(): Observable<PendingEvaluation[]> {
        return this.http.get<PendingEvaluation[]>(
            `${this.apiUrl}/pending`
        );
    }
    
    saveAnswerProgress(
        attemptId: number,
        request: EvaluationAnswerProgressRequest
        ): Observable<void> {
        return this.http.put<void>(
            `${this.apiUrl}/attempts/${attemptId}/answers`,
            request
        );
    }

    resumeEvaluation(attemptId: number): Observable<EvaluationResumeResponse> {
        return this.http.get<EvaluationResumeResponse>(
            `${this.apiUrl}/attempts/${attemptId}/resume`
        );
    }

    getMyEvaluations(): Observable<MyEvaluationResponse[]> {
        return this.http.get<MyEvaluationResponse[]>(
            `${this.apiUrl}/mine`
        );
    }

    
    downloadCertificate(attemptId: number): Observable<Blob> {
        return this.http.get(
            `/api/certificates/attempts/${attemptId}`,
            { responseType: 'blob' }
        );
    }
}