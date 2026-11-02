import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../core/auth.service';
import { defaultRoute } from '../../core/auth.guard';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, ButtonModule, InputTextModule, PasswordModule],
  template: `
    <section class="login-page">
      <div class="login-aside">
        <p class="eyebrow">POLIRESTAURANTE</p>
        <div class="login-illustration" aria-hidden="true">🍽️</div>
        <h1>Qué bueno<br><em>verte de nuevo.</em></h1>
        <p>Entra a tu espacio para tomar y seguir tus pedidos.</p>
      </div>
      <div class="login-panel">
        <span class="login-mark"><i class="pi pi-user"></i></span>
        <p class="eyebrow">ACCESO DE PERSONAL</p>
        <h2>Iniciar sesión</h2>
        <p class="login-intro">Usa el correo y la contraseña de tu cuenta.</p>
        <form (ngSubmit)="iniciarSesion()" #form="ngForm" novalidate>
          <label for="correo">Correo</label>
          <input pInputText id="correo" name="correo" type="email" autocomplete="username" placeholder="nombre@restaurante.com" [(ngModel)]="correo" required email />
          <label for="clave">Contraseña</label>
          <p-password inputId="clave" name="clave" autocomplete="current-password" placeholder="Tu contraseña" [(ngModel)]="clave" [feedback]="false" [toggleMask]="true" [fluid]="true" required />
          @if (error()) { <p class="login-error" role="alert">{{ error() }}</p> }
          <p-button type="submit" label="Entrar" icon="pi pi-arrow-right" iconPos="right" [fluid]="true" [loading]="cargando()" [disabled]="form.invalid || cargando()" />
        </form>
        <p class="login-help">El acceso se valida con el servidor y se mantiene solo durante esta sesión.</p>
      </div>
    </section>
  `,
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly cargando = signal(false);
  readonly error = signal('');
  correo = '';
  clave = '';

  iniciarSesion(): void {
    if (this.cargando() || !this.correo.trim() || !this.clave) return;
    this.error.set('');
    this.cargando.set(true);
    this.auth.login({ usuario: this.correo.trim(), clave: this.clave }).subscribe({
      next: () => {
        this.cargando.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const role = this.auth.role();
        const roleRoute = defaultRoute(role);
        const allowedPrefix = role === 'MESERO' ? ['/menu', '/pedidos']
          : role === 'COCINA' ? ['/cocina'] : ['/administracion'];
        const destination = returnUrl && allowedPrefix.some(path => returnUrl === path || returnUrl.startsWith(`${path}/`))
          ? returnUrl : roleRoute;
        void this.router.navigateByUrl(destination);
      },
      error: (response: HttpErrorResponse) => {
        this.cargando.set(false);
        this.error.set(response.status === 401 || response.status === 403
          ? 'Credenciales incorrectas o la cuenta no tiene un rol habilitado.'
          : 'No se pudo conectar con el servidor. Intenta de nuevo.');
      }
    });
  }
}
