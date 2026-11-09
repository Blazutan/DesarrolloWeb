import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { Subscription, finalize, forkJoin, interval } from 'rxjs';
import { DetallePedido, Pedido } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

@Component({
  selector: 'app-cocina',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule],
  template: `
    <section class="role-page">
      <header class="role-heading">
        <div><p class="eyebrow">PRODUCCIÓN</p><h1>Cola de cocina</h1>
          <p>Pedidos pendientes, en preparación y listos. Se actualizan cada 10 segundos.</p></div>
        <p-button label="Actualizar" icon="pi pi-refresh" [outlined]="true" [loading]="cargando()"
          (onClick)="cargar()" />
      </header>
      @if (error()) { <p class="notice error" role="alert">{{ error() }}</p> }
      @if (cargando() && pedidos().length === 0) {
        <p class="loading-state" role="status">Cargando pedidos...</p>
      } @else if (pedidosActivos().length === 0 && !error()) {
        <div class="empty-state"><span aria-hidden="true">🍳</span><h2>No hay pedidos por preparar</h2>
          <p>Los pedidos nuevos aparecerán aquí.</p></div>
      } @else {
        <div class="kitchen-grid">
          @for (pedido of pedidosActivos(); track pedido.id) {
            <article class="kitchen-card" [attr.aria-label]="'Pedido ' + pedido.id + ', ' + pedido.estado">
              <div class="kitchen-card-heading">
                <div><p class="eyebrow">PEDIDO #{{ pedido.id }}</p>
                  <h2>Mesa {{ pedido.mesaNumero ?? '—' }}</h2></div>
                <span class="status-pill" [class.preparing]="pedido.estado === 'EN PREPARACION'"
                  [class.ready]="pedido.estado === 'LISTO'">
                  {{ pedido.estado === 'EN PREPARACION' ? 'En preparación' : pedido.estado === 'LISTO' ? 'Listo' : 'Pendiente' }}
                </span>
              </div>
              <p class="order-time">Recibido {{ pedido.fechaCreacion | date:'shortTime' }}</p>
              <ul class="kitchen-items">
                @for (detalle of detallesDe(pedido.id); track detalle.id) {
                  <li><strong>{{ detalle.cantidad }} × {{ detalle.productoNombre }}</strong>
                    @if (detalle.notas) { <span>{{ detalle.notas }}</span> }
                  </li>
                } @empty { <li class="muted">Sin detalles disponibles.</li> }
              </ul>
              <footer class="kitchen-card-footer">
                <strong>{{ pedido.precioTotal | currency:'COP':'symbol-narrow':'1.0-0' }}</strong>
                @if (pedido.estado === 'PENDIENTE') {
                  <p-button label="Tomar pedido" icon="pi pi-play" [loading]="enCurso().has(pedido.id)"
                    (onClick)="cambiarEstado(pedido, 'EN PREPARACIÓN')" />
                } @else if (pedido.estado === 'EN PREPARACION') {
                  <p-button label="Marcar listo" icon="pi pi-check" [loading]="enCurso().has(pedido.id)"
                    (onClick)="cambiarEstado(pedido, 'LISTO')" />
                }
              </footer>
              <details class="cancel-order">
                <summary>Cancelar pedido</summary>
                <form (ngSubmit)="cancelarPedido(pedido)">
                  <label [attr.for]="'cancel-reason-' + pedido.id">Motivo de cancelación</label>
                  <textarea [id]="'cancel-reason-' + pedido.id" [name]="'cancel-reason-' + pedido.id"
                    [(ngModel)]="motivosCancelacion[pedido.id]" required maxlength="255"></textarea>
                  <button type="submit" [disabled]="!motivosCancelacion[pedido.id]?.trim() || enCurso().has(pedido.id)">
                    Confirmar cancelación
                  </button>
                </form>
              </details>
            </article>
          }
        </div>
      }
    </section>
  `,
  styles: [`
    .role-page { max-width: 1080px; min-height: 70vh; margin: auto; padding: 48px 24px 70px; }
    .role-heading { display: flex; align-items: center; justify-content: space-between; gap: 20px; margin-bottom: 28px; }
    h1 { margin: 8px 0; color: #283d30; font: 500 clamp(32px, 5vw, 46px)/1.1 'Playfair Display', serif; }
    .role-heading p:not(.eyebrow) { color: #777e76; font-size: 13px; }
    .loading-state { padding: 48px; text-align: center; color: #777e76; }
    .kitchen-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 18px; }
    .kitchen-card { padding: 20px; background: white; border: 1px solid #ecece5; border-radius: 14px; box-shadow: 0 5px 15px #243b2910; }
    .kitchen-card-heading, .kitchen-card-footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
    .kitchen-card h2 { margin: 5px 0 0; font: 500 21px 'Playfair Display', serif; }
    .status-pill.preparing { color: #9a5c1f; background: #fff3df; }
    .status-pill.ready { color: #315d46; background: #edf5ec; }
    .order-time { color: #858980; font-size: 12px; }
    .kitchen-items { display: grid; gap: 11px; min-height: 64px; margin: 18px 0; padding: 16px 0; border-top: 1px solid #efeee9; border-bottom: 1px solid #efeee9; list-style: none; }
    .kitchen-items li { display: flex; flex-direction: column; gap: 4px; font-size: 13px; }
    .kitchen-items li span, .muted { color: #858980; font-size: 12px; }
    .kitchen-card-footer strong { color: #315d46; }
    .cancel-order { margin-top: 16px; padding-top: 12px; border-top: 1px solid #efeee9; }
    .cancel-order summary { color: #9b422d; cursor: pointer; font-size: 12px; font-weight: 600; }
    .cancel-order form { display: grid; gap: 8px; margin-top: 10px; }
    .cancel-order label { color: #525d52; font-size: 12px; font-weight: 600; }
    .cancel-order textarea { min-height: 64px; padding: 8px; border: 1px solid #dcded6; border-radius: 7px; font: inherit; resize: vertical; }
    .cancel-order button { justify-self: start; padding: 8px 11px; border: 0; border-radius: 7px; color: #9b422d; background: #fbeae6; cursor: pointer; font: inherit; font-size: 12px; }
    .cancel-order button:disabled { opacity: .55; cursor: not-allowed; }
    @media (max-width: 560px) { .role-page { padding: 32px 16px 50px; }.role-heading { align-items: flex-start; flex-direction: column; } }
  `]
})
export class CocinaComponent implements OnInit, OnDestroy {
  private readonly api = inject(RestauranteApiService);
  private readonly timer = new Subscription();
  private readonly refresco = new Subscription();
  readonly pedidos = signal<Pedido[]>([]);
  readonly detalles = signal<DetallePedido[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');
  readonly enCurso = signal(new Set<number>());
  readonly motivosCancelacion: Record<number, string> = {};
  readonly pedidosActivos = computed(() => this.pedidos()
    .filter(pedido => ['PENDIENTE', 'EN PREPARACION', 'LISTO'].includes(pedido.estado))
    .sort((a, b) => a.fechaCreacion.localeCompare(b.fechaCreacion)));

  ngOnInit(): void {
    this.cargar();
    this.timer.add(interval(10_000).subscribe(() => this.cargar()));
  }

  ngOnDestroy(): void {
    this.timer.unsubscribe();
    this.refresco.unsubscribe();
  }

  cargar(): void {
    this.error.set('');
    this.cargando.set(true);
    this.refresco.add(forkJoin({
      pedidos: this.api.pedidos(),
      detalles: this.api.detallesPedido()
    }).pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: resultado => {
        this.pedidos.set(resultado.pedidos);
        this.detalles.set(resultado.detalles);
      },
      error: () => this.error.set('No se pudo cargar la cola. Verifica la conexión y los permisos de cocina.')
    }));
  }

  detallesDe(pedidoId: number): DetallePedido[] {
    return this.detalles().filter(detalle => detalle.pedidoId === pedidoId);
  }

  cambiarEstado(pedido: Pedido, estado: 'EN PREPARACIÓN' | 'LISTO'): void {
    this.enCurso.update(actual => new Set(actual).add(pedido.id));
    this.api.cambiarEstadoPedido(pedido.id, estado).pipe(finalize(() => {
      this.enCurso.update(actual => {
        const siguiente = new Set(actual);
        siguiente.delete(pedido.id);
        return siguiente;
      });
    })).subscribe({
      next: actualizado => this.pedidos.update(pedidos => pedidos.map(item =>
        item.id === actualizado.id ? actualizado : item)),
      error: () => this.error.set(`No se pudo cambiar el estado del pedido #${pedido.id}. Actualiza la cola e inténtalo otra vez.`)
    });
  }

  cancelarPedido(pedido: Pedido): void {
    const motivo = this.motivosCancelacion[pedido.id]?.trim();
    if (!motivo || motivo.length > 255) return;
    if (!window.confirm(`¿Cancelar el pedido #${pedido.id}?`)) return;

    this.enCurso.update(actual => new Set(actual).add(pedido.id));
    this.api.cancelarPedidoCocina(pedido.id, motivo).pipe(finalize(() => {
      this.enCurso.update(actual => {
        const siguiente = new Set(actual);
        siguiente.delete(pedido.id);
        return siguiente;
      });
    })).subscribe({
      next: actualizado => {
        this.pedidos.update(pedidos => pedidos.map(item =>
          item.id === actualizado.id ? actualizado : item));
        delete this.motivosCancelacion[pedido.id];
      },
      error: () => this.error.set(`No se pudo cancelar el pedido #${pedido.id}. Actualiza la cola e inténtalo otra vez.`)
    });
  }
}
