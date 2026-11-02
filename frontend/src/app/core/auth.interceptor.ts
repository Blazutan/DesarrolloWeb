import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const raw = sessionStorage.getItem('poli-auth');
  if (!raw || !request.url.startsWith('/api/')) return next(request);
  const { usuario, clave } = JSON.parse(raw) as { usuario: string; clave: string };
  return next(request.clone({ setHeaders: { Authorization: `Basic ${btoa(`${usuario}:${clave}`)}` } }));
};
