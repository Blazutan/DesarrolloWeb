import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AuthService } from './core/auth.service';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

describe('AppComponent', () => {
  let authenticated: ReturnType<typeof signal<boolean>>;

  beforeEach(() => {
    sessionStorage.removeItem('poli-auth');
    authenticated = signal(false);
  });

  it('renderiza navegación a menú y pedidos', async () => {
    authenticated.set(true);
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter(routes),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: AuthService, useValue: { isAuthenticated: authenticated, role: () => 'MESERO', logout: jasmine.createSpy() } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const links = Array.from(fixture.nativeElement.querySelectorAll('nav a')) as HTMLAnchorElement[];
    expect(links.map(link => link.textContent?.trim())).toEqual(['Menú', 'Mis pedidos']);
    expect(links.map(link => link.getAttribute('href'))).toEqual(['/menu', '/pedidos']);
  });

  it('muestra el acceso cuando no hay sesión', async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter(routes),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: AuthService, useValue: { isAuthenticated: authenticated, role: () => null, logout: jasmine.createSpy() } }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.login-link')?.textContent).toContain('Iniciar sesión');
  });
});
