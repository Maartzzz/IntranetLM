import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../auth/auth.service';

export const reportAccessGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return authService.getCurrentUser().pipe(
    map(user => user.role === 'ADMIN' || user.role === 'SUPERVISOR'
      ? true
      : router.createUrlTree(['/dashboard']))
  );
};
