import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpHeaders, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    sessionStorage.setItem('poli-auth', JSON.stringify({
      usuario: 'mesero@test.com', clave: 'clave', rol: 'MESERO'
    }));
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()]
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.removeItem('poli-auth');
  });

  it('adjunta Basic solo a las rutas de API', () => {
    const client = TestBed.inject(HttpClient);
    client.get('/api/pedidos/mis-pedidos').subscribe();
    const apiRequest = http.expectOne('/api/pedidos/mis-pedidos');
    expect(apiRequest.request.headers.get('Authorization')).toMatch(/^Basic /);
    apiRequest.flush([]);

    client.get('/assets/logo.svg').subscribe();
    const assetRequest = http.expectOne('/assets/logo.svg');
    expect(assetRequest.request.headers.has('Authorization')).toBeFalse();
    assetRequest.flush('');

    client.get('/api/auth/me', { headers: new HttpHeaders({ Authorization: 'Bearer test-token' }) }).subscribe();
    const explicitRequest = http.expectOne('/api/auth/me');
    expect(explicitRequest.request.headers.get('Authorization')).toBe('Bearer test-token');
    explicitRequest.flush({ correo: 'mesero@test.com', rol: 'MESERO' });
  });
});
