import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const permissionGuard: CanActivateFn = route => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const requiredAny = (route.data['permissions'] as string[] | undefined) ?? [];
  const requiredAll = (route.data['allPermissions'] as string[] | undefined) ?? [];

  const hasAllRequired = requiredAll.every(permission =>
    auth.hasPermission(permission)
  );
  const hasAnyRequired = requiredAny.length === 0 ||
    auth.hasAnyPermission(...requiredAny);

  if (hasAllRequired && hasAnyRequired) {
    return true;
  }

  // Evita una redirección circular si el usuario no tiene acceso al dashboard.
  return router.createUrlTree(
    auth.hasPermission('DASHBOARD.VIEW') ? ['/dashboard'] : ['/login']
  );
};
