import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from './core/auth.service';

@Component({ selector: 'app-root', standalone: true, imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, ToastModule], providers: [MessageService], template: `
  <p-toast position="top-right" />
  <header class="topbar"><a [routerLink]="homeRoute" class="brand"><span class="brand-mark">P</span><span>poli<span class="brand-accent">restaurante</span><small>HECHO PARA COMPARTIR</small></span></a>
    @if (auth.isAuthenticated()) {
      @if (auth.role() === 'MESERO') {
        <nav><a routerLink="/menu" routerLinkActive="active">Menú</a><a routerLink="/pedidos" routerLinkActive="active">Mis pedidos</a></nav>
      } @else if (auth.role() === 'COCINA') {
        <nav><a routerLink="/cocina" routerLinkActive="active">Cocina</a></nav>
      } @else {
        <nav><a routerLink="/administracion" routerLinkActive="active">Administración</a></nav>
      }
      <p-button label="Cerrar sesión" icon="pi pi-sign-out" [text]="true" (onClick)="logout()" />
    } @else {
      <a class="login-link" routerLink="/login">Iniciar sesión <i class="pi pi-arrow-right"></i></a>
    }
  </header>
  <main><router-outlet /></main>
  <footer>PoliRestaurante <span>•</span> Sabores para disfrutar sin prisa</footer>
` })
export class AppComponent {
  readonly auth = inject(AuthService);
  get homeRoute(): string {
    const role = this.auth.role();
    return role === 'COCINA' ? '/cocina' : role === 'ADMINISTRADOR' ? '/administracion' : '/menu';
  }
  private readonly router = inject(Router);
  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
