import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { Alerta } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

@Component({ selector: 'app-pedidos', standalone: true, imports: [CommonModule, ButtonModule], template: `
  <section class="orders-page"><p class="eyebrow">TU MESA, AL DÍA</p><h1>Mis pedidos</h1><p class="hero-copy">Aquí aparecen los pedidos listos para recoger.</p>
    @if (error()) { <div class="notice error">{{ error() }}</div> }
    @if (alertas().length) { <div class="orders-list">@for (alerta of alertas(); track alerta.pedidoId) { <article class="order-card"><div class="order-icon"><i class="pi pi-bell"></i></div><div class="order-info"><span class="status-pill">Listo para recoger</span><h2>Pedido #{{ alerta.pedidoId }}</h2><p>Listo desde {{ alerta.fechaListo | date:'shortTime' }} · {{ alerta.numeroRecordatorios }} avisos</p></div><i class="pi pi-arrow-right order-arrow"></i></article> }</div> }
    @else if (!error()) { <div class="empty-state"><span>🛎️</span><h3>Todo tranquilo por aquí</h3><p>Cuando un pedido esté listo, lo verás en esta lista.</p><p-button label="Actualizar" icon="pi pi-refresh" [text]="true" (onClick)="cargar()" /></div> }
  </section>
` })
export class PedidosComponent implements OnInit {
  private readonly api = inject(RestauranteApiService);
  readonly alertas = signal<Alerta[]>([]);
  readonly error = signal('');
  ngOnInit(): void { this.cargar(); }
  cargar(): void {
    this.error.set('');
    this.api.alertas().subscribe({
      next: value => this.alertas.set(value),
      error: () => this.error.set('No se pudieron cargar las alertas. Revisa tu sesión y tu conexión.')
    });
  }
}
