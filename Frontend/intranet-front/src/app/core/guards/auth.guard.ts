
import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router
} from '@angular/router';
import { map, catchError, of } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.getUser()) {
    return true;
  }

  return authService.getCurrentUser().pipe(
    map(() => true),
    catchError(() => {
      authService.logout();
      return of(router.createUrlTree(['/login']));
    })
  );
};
