import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import {
  LoginRequest,
  LoginResponse,
  CurrentUser
} from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = '/api/auth';
  private readonly tokenKey = 'access_token';

  private currentUser: CurrentUser | null = null;

  login(request: LoginRequest): Observable<LoginResponse> {

    return this.http
      .post<LoginResponse>(
        `${this.apiUrl}/login`,
        request
      )
      .pipe(
        tap(response => {
          localStorage.setItem(
            this.tokenKey,
            response.accessToken
          );
        })
      );
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getCurrentUser(): Observable<CurrentUser> {

    return this.http
      .get<CurrentUser>(`${this.apiUrl}/me`)
      .pipe(
        tap(user => {
          this.currentUser = user;
        })
      );
  }

  getUser(): CurrentUser | null {
    return this.currentUser;
  }

  isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  isDriver(): boolean {
    return this.currentUser?.role === 'CHOFER';
  }

  isSupervisor(): boolean {
    return this.currentUser?.role === 'SUPERVISOR';
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUser = null;
  }
}