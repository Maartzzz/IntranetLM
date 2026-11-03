import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
    Course,
    CourseRequest
    } from '../models/courses.models';

    @Injectable({
    providedIn: 'root'
    })
    export class CourseService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/courses';

    getCourses(): Observable<Course[]> {
        return this.http.get<Course[]>(this.apiUrl);
    }

    getCourse(id: number): Observable<Course> {
        return this.http.get<Course>(
        `${this.apiUrl}/${id}`
        );
    }

    createCourse(
        request: CourseRequest
    ): Observable<Course> {

        return this.http.post<Course>(
        this.apiUrl,
        request
        );
    }

    updateCourse(
        id: number,
        request: CourseRequest
    ): Observable<Course> {

        return this.http.put<Course>(
        `${this.apiUrl}/${id}`,
        request
        );
    }

    toggleActive(
        id: number
    ): Observable<Course> {

        return this.http.patch<Course>(
        `${this.apiUrl}/${id}/active`,
        {}
        );
    }

    getAssignedCourses(): Observable<Course[]> {

        return this.http.get<Course[]>(
            `${this.apiUrl}/assigned`
        );

    }

    getAvailableEvaluationCourses(): Observable<Course[]> {
        return this.http.get<Course[]>(
            '/api/evaluations/available'
        );
    }

}