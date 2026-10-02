import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from '../models/users.models';

export interface UserRequest {
    username: string;
    password: string;
    email: string | null;
    firstName: string;
    lastName: string;
    documentType: string;
    documentNumber: string;
    phone: string | null;
    role: string;
}

export interface UpdateUserRequest {
    email: string | null;
    firstName: string;
    lastName: string;
    documentType: string;
    documentNumber: string;
    phone: string | null;
    role: string;
}

@Injectable({
    providedIn: 'root'
    })
    export class UserService {

    private readonly http = inject(HttpClient);

    private readonly apiUrl = '/api/users';

    getUsers(): Observable<User[]> {
        return this.http.get<User[]>(this.apiUrl);
    }

    getUser(username: string): Observable<User> {
        return this.http.get<User>(
        `${this.apiUrl}/${username}`
        );
    }

    createUser(request: UserRequest): Observable<User> {
        return this.http.post<User>(
            this.apiUrl,
            request
        );
    }

    updateUser(username: string, request: UpdateUserRequest
    ): Observable<User> {

        return this.http.put<User>(
            `${this.apiUrl}/${username}`,
            request
        );
    }

    setActive(username: string, active: boolean
    ): Observable<void> {

        return this.http.patch<void>(
            `${this.apiUrl}/${username}/active`,
            null,
            {
            params: {
                active
            }
            }
        );
    }
    
}