import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { RouterLink } from '@angular/router';
import { Observable, Subscription, finalize, forkJoin, interval } from 'rxjs';
import { Alerta, Pedido } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, RouterLink],
  template: `
    <section class="orders-page">
      <p class="eyebrow">TU MESA, AL DÍA</p><h1>Mis pedidos</h1>
      <p class="hero-copy">Consulta el avance y las alertas de tus pedidos activos.</p>
      <div class="orders-toolbar"><span>Actualización automática cada 5 segundos</span>
        <p-button label="Actualizar" icon="pi pi-refresh" [text]="true" [loading]="cargando()" (onClick)="cargar()" />
      </div>
      @if (error()) { <div class="notice error" role="alert">{{ error() }}</div> }
      @if (cargando() && pedidos().length === 0) { <p class="loading-state" role="status">Cargando pedidos...</p> }
      @else if (pedidos().length) {
        <div class="orders-list">
          @for (pedido of pedidos(); track pedido.id) {
            <article class="order-card">
              <div class="order-icon" aria-hidden="true"><i class="pi" [class.pi-bell]="pedido.estado === 'LISTO'" [class.pi-receipt]="pedido.estado !== 'LISTO'"></i></div>
              <div class="order-info">
                <span class="status-pill" [class.preparing]="pedido.estado === 'EN PREPARACION'">{{ etiquetaEstado(pedido.estado) }}</span>
                <h2>Pedido #{{ pedido.id }} · Mesa {{ pedido.mesaNumero ?? '—' }}</h2>
                <p>{{ pedido.fechaCreacion | date:'short' }} · {{ pedido.precioTotal | currency:'COP':'symbol-narrow':'1.0-0' }}</p>
                @if (alertaActiva(pedido.id)) { <p class="alert-note" role="status">Listo para recoger · {{ alertaActiva(pedido.id)?.numeroRecordatorios }} avisos</p> }
                @if (pedido.estado === 'LISTO' && pedido.fechaListo) { <p>Listo desde {{ pedido.fechaListo | date:'shortTime' }}</p> }
                @if (pedidoCancelacion() === pedido.id) {
                  <form class="cancel-form" (ngSubmit)="confirmarCancelacion(pedido)">
                    <label [attr.for]="'motivo-' + pedido.id">Motivo de cancelación</label>
                    <input [id]="'motivo-' + pedido.id" name="motivo" [(ngModel)]="motivo" required maxlength="255" />
                    <div><p-button type="submit" label="Confirmar cancelación" severity="danger" [disabled]="!motivo.trim()" />
                      <p-button type="button" label="Volver" [text]="true" (onClick)="cerrarCancelacion()" /></div>
                  </form>
                }
              </div>
              <div class="order-actions">
                @if (alertaActiva(pedido.id)) {
                  <p-button label="Recibí el aviso" icon="pi pi-bell-slash" [outlined]="true"
                    [loading]="enCurso().has(pedido.id)" (onClick)="recibirAlerta(pedido)" />
                }
                @if (pedido.estado === 'LISTO') {
                  <p-button label="Marcar entregado" icon="pi pi-check" [loading]="enCurso().has(pedido.id)"
                    (onClick)="entregar(pedido)" />
                }
                <button type="button" class="cancel-link" (click)="iniciarCancelacion(pedido)">Cancelar pedido</button>
              </div>
            </article>
          }
        </div>
      } @else if (!error()) {
        <div class="empty-state"><span aria-hidden="true">🛎️</span><h2>Todo tranquilo por aquí</h2>
          <p>Cuando registres un pedido, su estado aparecerá en esta lista.</p>
          <a routerLink="/menu">Ir al menú</a></div>
      }
    </section>
  `,
  styles: [`
    .orders-toolbar { display: flex; align-items: center; justify-content: space-between; margin: 20px 0 10px; color: #777e76; font-size: 11px; }
    .order-card { align-items: flex-start; }
    .order-info { flex: 1; }
    .status-pill.preparing { color: #9a5c1f; background: #fff3df; }
    .alert-note { color: #a45e3b !important; font-weight: 700; }
    .order-actions { display: flex; flex-direction: column; align-items: stretch; gap: 8px; min-width: 170px; }
    .cancel-link { padding: 6px; border: 0; color: #9b422d; background: transparent; cursor: pointer; font: inherit; font-size: 11px; text-decoration: underline; }
    .cancel-form { display: grid; gap: 8px; max-width: 380px; margin-top: 12px; }
    .cancel-form input { padding: 9px; border: 1px solid #dcded6; border-radius: 7px; }
    .cancel-form > div { display: flex; flex-wrap: wrap; gap: 7px; }
    .loading-state { padding: 48px; color: #777e76; text-align: center; }
    .empty-state a { display: inline-block; margin-top: 8px; color: #315d46; font-weight: 700; text-decoration: underline; }
    @media (max-width: 650px) {
      .orders-page { padding: 36px 16px; }
      .order-card { flex-wrap: wrap; }
      .order-actions { width: 100%; margin-left: 60px; }
      .orders-toolbar { align-items: flex-start; }
    }
  `]
})
export class PedidosComponent implements OnInit, OnDestroy {
  private readonly api = inject(RestauranteApiService);
  private readonly timer = new Subscription();
  private readonly refresco = new Subscription();
  readonly pedidos = signal<Pedido[]>([]);
  readonly alertas = signal<Alerta[]>([]);
  readonly error = signal('');
  readonly cargando = signal(false);
  readonly enCurso = signal(new Set<number>());
  readonly pedidoCancelacion = signal<number | null>(null);
  motivo = '';

  ngOnInit(): void {
    this.cargar();
    this.timer.add(interval(5_000).subscribe(() => this.cargar()));
  }

  ngOnDestroy(): void {
    this.timer.unsubscribe();
    this.refresco.unsubscribe();
  }

  cargar(): void {
    this.error.set('');
    this.cargando.set(true);
    this.refresco.add(forkJoin({ pedidos: this.api.misPedidos(), alertas: this.api.alertas() })
      .pipe(finalize(() => this.cargando.set(false))).subscribe({
        next: resultado => {
          this.pedidos.set(resultado.pedidos);
          this.alertas.set(resultado.alertas);
        },
        error: () => this.error.set('No se pudieron cargar tus pedidos. Revisa tu sesión y tu conexión.')
      }));
  }

  alertaActiva(pedidoId: number): Alerta | undefined {
    return this.alertas().find(alerta => alerta.pedidoId === pedidoId);
  }

  etiquetaEstado(estado: string): string {
    return estado === 'EN PREPARACION' ? 'En preparación' : estado.charAt(0) + estado.slice(1).toLowerCase();
  }

  recibirAlerta(pedido: Pedido): void {
    this.ejecutarAccion(pedido, this.api.recibirAlerta(pedido.id));
  }

  entregar(pedido: Pedido): void {
    if (!window.confirm(`¿Confirmas que el pedido #${pedido.id} fue entregado?`)) return;
    this.ejecutarAccion(pedido, this.api.entregarPedido(pedido.id));
  }

  iniciarCancelacion(pedido: Pedido): void {
    this.pedidoCancelacion.set(pedido.id);
    this.motivo = '';
  }

  cerrarCancelacion(): void {
    this.pedidoCancelacion.set(null);
    this.motivo = '';
  }

  confirmarCancelacion(pedido: Pedido): void {
    const motivo = this.motivo.trim();
    if (!motivo) return;
    this.ejecutarAccion(pedido, this.api.cancelarPedido(pedido.id, motivo));
    this.cerrarCancelacion();
  }

  private ejecutarAccion(pedido: Pedido, operacion: Observable<Pedido>): void {
    this.enCurso.update(actual => new Set(actual).add(pedido.id));
    operacion.pipe(finalize(() => this.enCurso.update(actual => {
      const siguiente = new Set(actual);
      siguiente.delete(pedido.id);
      return siguiente;
    }))).subscribe({
      next: () => this.cargar(),
      error: () => this.error.set(`No se pudo actualizar el pedido #${pedido.id}. Actualiza la lista e inténtalo de nuevo.`)
    });
  }
}
