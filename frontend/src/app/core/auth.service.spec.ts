import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.removeItem('poli-auth');
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('detecta el rol de mesero antes de guardar la sesión', () => {
    service.login({ usuario: 'mesero@test.com', clave: 'clave' }).subscribe();
    const common = http.expectOne('/api/categorias');
    expect(common.request.headers.get('Authorization')).toMatch(/^Basic /);
    expect(service.isAuthenticated()).toBeFalse();
    common.flush([]);

    http.expectOne('/api/pedidos/alertas').flush([]);
    http.expectOne('/api/pedidos/cola/cocina').flush({}, { status: 403, statusText: 'Forbidden' });
    http.expectOne('/api/administracion/mesas').flush({}, { status: 403, statusText: 'Forbidden' });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('MESERO');
    expect(JSON.parse(sessionStorage.getItem('poli-auth') ?? '{}').usuario).toBe('mesero@test.com');
  });

  it('detecta el rol de cocina', () => {
    service.login({ usuario: 'cocina@test.com', clave: 'clave' }).subscribe();
    http.expectOne('/api/categorias').flush([]);
    http.expectOne('/api/pedidos/alertas').flush({}, { status: 403, statusText: 'Forbidden' });
    http.expectOne('/api/pedidos/cola/cocina').flush([]);
    http.expectOne('/api/administracion/mesas').flush({}, { status: 403, statusText: 'Forbidden' });
    expect(service.role()).toBe('COCINA');
  });

  it('detecta administración por la autorización del espacio protegido', () => {
    service.login({ usuario: 'admin@test.com', clave: 'clave' }).subscribe();
    http.expectOne('/api/categorias').flush([]);
    http.expectOne('/api/pedidos/alertas').flush({}, { status: 403, statusText: 'Forbidden' });
    http.expectOne('/api/pedidos/cola/cocina').flush({}, { status: 403, statusText: 'Forbidden' });
    http.expectOne('/api/administracion/mesas').flush([]);
    expect(service.role()).toBe('ADMINISTRADOR');
  });

  it('cierra la sesión y elimina las credenciales', () => {
    sessionStorage.setItem('poli-auth', JSON.stringify({ usuario: 'mesero@test.com', clave: 'clave', rol: 'MESERO' }));
    service.logout();
    expect(service.isAuthenticated()).toBeFalse();
    expect(sessionStorage.getItem('poli-auth')).toBeNull();
  });
});
