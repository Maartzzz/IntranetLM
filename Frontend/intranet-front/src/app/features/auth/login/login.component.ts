import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

import { AuthService } from '../../../core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  errorMessage = '';

  onSubmit(): void {

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.errorMessage = '';

    this.authService
      .login(this.loginForm.getRawValue())
      .subscribe({
        next: (response) => {

          this.authService
            .getCurrentUser()
            .subscribe({
              next: () => {
                if(this.authService.isAdmin())
                this.router.navigate(['/dashboard']);
                else if(this.authService.isDriver())
                  this.router.navigate(['/learning'])
                  else
                    this.router.navigate(['/dashboard']);


              },

              error: (error) => {
                console.error(
                  'Error obteniendo usuario:',
                  error
                );

                this.errorMessage =
                  'No se pudo obtener la información del usuario.';
              }
            });

        },

        error: (error) => {
          console.error('Error de login:', error);

          if (error.status === 401) {
            this.errorMessage = 'Usuario o contraseña incorrectos.';
          } else {
            this.errorMessage =
              'No se pudo conectar con el servidor.';
          }
        }
      });
  }
}