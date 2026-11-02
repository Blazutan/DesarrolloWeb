import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { LoginComponent } from '../pages/login/login.component';
import { AdministracionComponent } from '../pages/administracion/administracion.component';
import { RestauranteApiService } from './restaurante-api.service';
import { of } from 'rxjs';
import { routes } from '../app.routes';

describe('authGuard', () => {
  beforeEach(() => sessionStorage.removeItem('poli-auth'));

  it('redirige al login cuando se intenta abrir una vista protegida sin sesión', async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        providePrimeNG({ theme: { preset: Aura } })
      ]
    }).compileComponents();

    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/menu', LoginComponent);

    expect(harness.routeNativeElement?.querySelector('form')).toBeTruthy();
    expect(harness.routeNativeElement?.textContent).toContain('ACCESO DE PERSONAL');
  });

  it('redirige a la página permitida cuando el rol intenta abrir otra', async () => {
    sessionStorage.setItem('poli-auth', JSON.stringify({
      usuario: 'admin@test.com', clave: 'clave', rol: 'ADMINISTRADOR'
    }));
    const api = {
      categorias: () => of([]),
      productos: () => of([]),
      administrarMesas: () => of([]),
      usuarios: () => of([]),
      reporteVentas: () => of({
        desde: '2026-01-01', hasta: '2026-01-31', totalPedidos: 0,
        totalVentas: 0, ticketPromedio: 0, productos: []
      }),
      reporteTiempos: () => of({
        desde: '2026-01-01', hasta: '2026-01-31', pedidosAnalizados: 0,
        promedioPreparacionSegundos: null, promedioEsperaEntregaSegundos: null, promedioTotalSegundos: null
      })
    };
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter(routes),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: RestauranteApiService, useValue: api }
      ]
    }).compileComponents();

    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/menu', AdministracionComponent);

    expect(TestBed.inject(Router).url).toBe('/administracion');
    expect(harness.routeNativeElement?.textContent).toContain('Administración');
  });
});
