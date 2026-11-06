import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ScheduleBoardResponse,
  ScheduleVersionDetail,
  ScheduleVersionSummary
} from '../models/schedule.models';

export interface ScheduleIncludeRequest {
  scheduledDate: string;
  reason: string;
}

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/internal-service-orders';
  private readonly scheduleUrl = `${this.baseUrl}/schedule`;
  private readonly versionsUrl = `${this.baseUrl}/schedule-versions`;

  getSchedule(startDate: string, endDate: string): Observable<ScheduleBoardResponse> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<ScheduleBoardResponse>(this.scheduleUrl, { params });
  }

  includeOrder(orderId: number, request: ScheduleIncludeRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${orderId}/schedule/include`, request);
  }

  excludeOrder(orderId: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${orderId}/schedule/exclude`, {});
  }

  saveVersion(startDate: string, endDate: string): Observable<ScheduleVersionSummary> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.http.post<ScheduleVersionSummary>(this.versionsUrl, null, { params });
  }

  getVersions(startDate: string, endDate: string): Observable<ScheduleVersionSummary[]> {
    const params = new HttpParams().set('startDate', startDate).set('endDate', endDate);
    return this.http.get<ScheduleVersionSummary[]>(this.versionsUrl, { params });
  }

  getVersionDetail(versionId: number): Observable<ScheduleVersionDetail> {
    return this.http.get<ScheduleVersionDetail>(`${this.versionsUrl}/${versionId}`);
  }
}
