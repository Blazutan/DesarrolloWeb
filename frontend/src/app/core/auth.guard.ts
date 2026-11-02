import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AppRole, AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  if (inject(AuthService).isAuthenticated()) return true;
  return inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};

export const roleGuard = (...roles: AppRole[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const role = auth.role();
  return auth.isAuthenticated() && role !== null && roles.includes(role)
    ? true
    : inject(Router).createUrlTree([auth.isAuthenticated() ? defaultRoute(role) : '/login']);
};

export function defaultRoute(role: AppRole | null): string {
  if (role === 'COCINA') return '/cocina';
  if (role === 'ADMINISTRADOR') return '/administracion';
  return '/menu';
}
