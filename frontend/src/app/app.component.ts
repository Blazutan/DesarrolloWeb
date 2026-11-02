import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

@Component({ selector: 'app-root', standalone: true, imports: [RouterOutlet, RouterLink, RouterLinkActive, ButtonModule, ToastModule], providers: [MessageService], template: `
  <p-toast position="top-right" />
  <header class="topbar"><a routerLink="/menu" class="brand"><span class="brand-mark">P</span><span>poli<span class="brand-accent">restaurante</span><small>HECHO PARA COMPARTIR</small></span></a>
    <nav><a routerLink="/menu" routerLinkActive="active">Menú</a><a routerLink="/pedidos" routerLinkActive="active">Mis pedidos</a></nav>
    <a class="api-link" href="/swagger-ui/index.html" target="_blank" rel="noreferrer">API <i class="pi pi-arrow-up-right"></i></a>
  </header>
  <main><router-outlet /></main>
  <footer>PoliRestaurante <span>•</span> Sabores para disfrutar sin prisa</footer>
` })
export class AppComponent {}
