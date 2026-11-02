import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AuthService } from '../../core/auth.service';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  const auth = { login: jasmine.createSpy(), role: jasmine.createSpy().and.returnValue('MESERO') };

  beforeEach(() => {
    sessionStorage.removeItem('poli-auth');
    auth.login.calls.reset();
    auth.login.and.returnValue(of(undefined));
    auth.role.and.returnValue('MESERO');
  });

  it('muestra un formulario de acceso para el personal', async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('ACCESO DE PERSONAL');
    expect(fixture.nativeElement.querySelector('form')).toBeTruthy();
  });

  it('envía las credenciales y navega al menú al autenticar', async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(LoginComponent);
    const router = TestBed.inject(Router);
    const navigate = spyOn(router, 'navigateByUrl').and.returnValue(Promise.resolve(true));
    fixture.componentInstance.correo = 'mesero@test.com';
    fixture.componentInstance.clave = 'clave';
    fixture.componentInstance.iniciarSesion();

    expect(auth.login).toHaveBeenCalledWith({ usuario: 'mesero@test.com', clave: 'clave' });
    expect(navigate).toHaveBeenCalledWith('/menu');
  });

  it('muestra un mensaje si las credenciales son rechazadas', async () => {
    auth.login.and.returnValue(throwError(() => ({ status: 401 })));
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        providePrimeNG({ theme: { preset: Aura } }),
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    const component = TestBed.createComponent(LoginComponent).componentInstance;
    component.correo = 'mesero@test.com';
    component.clave = 'incorrecta';
    component.iniciarSesion();

    expect(component.error()).toContain('Credenciales incorrectas');
  });
});
