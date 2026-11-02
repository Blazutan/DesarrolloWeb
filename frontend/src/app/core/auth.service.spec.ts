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

  it('consulta la identidad autenticada sin inferir el rol desde datos de negocio', () => {
    service.login({ usuario: 'mesero@test.com', clave: 'clave' }).subscribe();
    const request = http.expectOne('/api/auth/me');
    expect(request.request.headers.get('Authorization')).toMatch(/^Basic /);
    expect(service.isAuthenticated()).toBeFalse();
    request.flush({ correo: 'mesero@test.com', rol: 'MESERO' });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.role()).toBe('MESERO');
    expect(JSON.parse(sessionStorage.getItem('poli-auth') ?? '{}').usuario).toBe('mesero@test.com');
  });

  it('guarda el rol de cocina reportado por la API', () => {
    service.login({ usuario: 'cocina@test.com', clave: 'clave' }).subscribe();
    http.expectOne('/api/auth/me').flush({ correo: 'cocina@test.com', rol: 'COCINA' });
    expect(service.role()).toBe('COCINA');
  });

  it('guarda el rol de administración reportado por la API', () => {
    service.login({ usuario: 'admin@test.com', clave: 'clave' }).subscribe();
    http.expectOne('/api/auth/me').flush({ correo: 'admin@test.com', rol: 'ADMINISTRADOR' });
    expect(service.role()).toBe('ADMINISTRADOR');
  });

  it('rechaza roles desconocidos', () => {
    let fallo: unknown;
    service.login({ usuario: 'usuario@test.com', clave: 'clave' }).subscribe({ error: error => fallo = error });
    http.expectOne('/api/auth/me').flush({ correo: 'usuario@test.com', rol: 'DESCONOCIDO' });
    expect(fallo).toBeTruthy();
    expect(service.isAuthenticated()).toBeFalse();
  });

  it('cierra la sesión y elimina las credenciales', () => {
    sessionStorage.setItem('poli-auth', JSON.stringify({ usuario: 'mesero@test.com', clave: 'clave', rol: 'MESERO' }));
    service.logout();
    expect(service.isAuthenticated()).toBeFalse();
    expect(sessionStorage.getItem('poli-auth')).toBeNull();
  });
});
