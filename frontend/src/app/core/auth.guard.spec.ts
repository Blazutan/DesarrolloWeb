import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { LoginComponent } from '../pages/login/login.component';
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
});
