import { Injectable, inject } from '@angular/core';

import { HttpClient } from '@angular/common/http';

import { Observable } from 'rxjs';

import {
    CourseAudience,
    CourseAudienceRequest
    } from '../models/course-audience.model';

    @Injectable({
    providedIn: 'root'
    })
    export class CourseAudienceService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/courses';

    getByCourse(
        courseId: number
    ): Observable<CourseAudience[]> {

        return this.http.get<CourseAudience[]>(
        `${this.apiUrl}/${courseId}/audiences`
        );
    }

    add(
        courseId: number,
        request: CourseAudienceRequest
    ): Observable<CourseAudience> {

        return this.http.post<CourseAudience>(
        `${this.apiUrl}/${courseId}/audiences`,
        request
        );
    }

    remove(
        courseId: number,
        audienceId: number
    ): Observable<void> {

        return this.http.delete<void>(
        `${this.apiUrl}/${courseId}/audiences/${audienceId}`
        );
    }
}