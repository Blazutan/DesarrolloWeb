import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { finalize, forkJoin } from 'rxjs';
import { Categoria, Mesa, PedidoItemRequest, Producto } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

interface LineaPedido extends PedidoItemRequest {
  producto: Producto;
}

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, CardModule, SelectModule],
  template: `
    <section class="hero"><div><p class="eyebrow">COCINA FRESCA · BUENOS MOMENTOS</p>
      <h1>Algo rico está<br><em>por suceder.</em></h1>
      <p class="hero-copy">Explora nuestro menú y arma tu pedido favorito.</p></div>
      <div class="hero-art" aria-hidden="true"><span>✳</span><div class="plate"><div class="plate-food">🍲</div></div><small>PREPARADO AL MOMENTO</small></div>
    </section>
    <section class="menu-section">
      <div class="section-heading"><div><p class="eyebrow">NUESTRA CARTA</p><h2>El menú</h2></div><p class="muted">Ingredientes frescos, recetas con cariño.</p></div>
      @if (error()) { <div class="notice error" role="alert">{{ error() }}</div> }
      <div class="filter-row">
        <p-select [options]="categorias()" [ngModel]="categoriaSeleccionada()" (ngModelChange)="categoriaSeleccionada.set($event)" optionLabel="nombre" optionValue="id"
          placeholder="Todas las categorías" [showClear]="true" />
        <span class="result-count">{{ productosFiltrados().length }} opciones</span>
      </div>
      @if (cargandoMenu()) { <p class="loading-state" role="status">Cargando menú y mesas disponibles...</p> }
      @if (productosFiltrados().length) {
        <div class="product-grid">@for (producto of productosFiltrados(); track producto.id) {
          <p-card styleClass="product-card">
            <ng-template #header><div class="product-art" [class.unavailable]="!producto.disponibilidad">
              <span>{{ emoji(producto.categoriaNombre) }}</span><small>{{ producto.categoriaNombre || 'De la casa' }}</small>
            </div></ng-template>
            <div class="product-info"><h3>{{ producto.nombre }}</h3>
              <p>{{ producto.descripcion || 'Una preparación hecha con ingredientes frescos.' }}</p>
              <div class="product-bottom"><strong>{{ producto.precio | currency:'COP':'symbol-narrow':'1.0-0' }}</strong>
                <p-button [label]="producto.disponibilidad ? 'Agregar' : 'Agotado'"
                  [icon]="producto.disponibilidad ? 'pi pi-plus' : 'pi pi-ban'"
                  [disabled]="!producto.disponibilidad || cargandoPedido()" (onClick)="agregar(producto)" />
              </div>
            </div>
          </p-card>
        }</div>
      } @else if (!cargandoMenu() && !error()) {
        <div class="empty-state"><span aria-hidden="true">☕</span><h3>No hay productos para mostrar</h3><p>Vuelve a intentarlo en un momento.</p></div>
      }

      <section class="cart-panel" aria-labelledby="cart-title">
        <div class="cart-heading"><div><p class="eyebrow">CONFIRMACIÓN</p><h2 id="cart-title">Tu pedido</h2></div>
          <span>{{ carrito().length }} productos distintos</span></div>
        @if (carrito().length) {
          <label for="mesa-pedido">Mesa disponible</label>
          <select id="mesa-pedido" [(ngModel)]="mesaSeleccionada" [ngModelOptions]="{standalone: true}" required>
            <option [ngValue]="null" disabled>Selecciona una mesa</option>
            @for (mesa of mesas(); track mesa.id) { <option [ngValue]="mesa.id">Mesa {{ mesa.numero }}</option> }
          </select>
          <ul class="cart-lines">@for (linea of carrito(); track linea.producto.id) {
            <li><div class="cart-product"><strong>{{ linea.producto.nombre }}</strong>
              <small>{{ linea.producto.precio * linea.cantidad | currency:'COP':'symbol-narrow':'1.0-0' }}</small>
              <input [attr.aria-label]="'Notas para ' + linea.producto.nombre" [(ngModel)]="linea.notas"
                [ngModelOptions]="{standalone: true}" maxlength="255" placeholder="Notas (opcional)" />
            </div>
              <div class="quantity-control">
                <button type="button" [attr.aria-label]="'Quitar una unidad de ' + linea.producto.nombre" (click)="cambiarCantidad(linea, -1)">−</button>
                <span aria-live="polite">{{ linea.cantidad }}</span>
                <button type="button" [attr.aria-label]="'Agregar una unidad de ' + linea.producto.nombre" (click)="cambiarCantidad(linea, 1)">+</button>
                <button type="button" class="remove-line" [attr.aria-label]="'Eliminar ' + linea.producto.nombre" (click)="quitar(linea)">Quitar</button>
              </div>
            </li>
          }</ul>
          <div class="cart-total"><span>Total</span><strong>{{ total() | currency:'COP':'symbol-narrow':'1.0-0' }}</strong></div>
          @if (!mesas().length) { <p class="notice error">No hay mesas libres. Consulta con administración.</p> }
          <p-button label="Confirmar pedido" icon="pi pi-check" [fluid]="true" [loading]="cargandoPedido()"
            [disabled]="!mesaSeleccionada || !carrito().length || cargandoPedido()"
            (onClick)="confirmarPedido()" />
        } @else {
          <p class="cart-empty">Agrega productos desde la carta para comenzar.</p>
        }
      </section>
    </section>
  `,
  styles: [`
    .loading-state { padding: 18px 0; color: #777e76; text-align: center; font-size: 13px; }
    .cart-panel { max-width: 720px; margin: 42px auto 0; padding: 22px; background: #fff; border: 1px solid #e8e7e2; border-radius: 14px; }
    .cart-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
    .cart-heading .eyebrow { margin-bottom: 4px; }
    .cart-heading h2 { margin: 0; color: #283d30; font: 500 26px 'Playfair Display', serif; }
    .cart-heading > span { color: #82877f; font-size: 11px; }
    .cart-panel > label { display: block; margin-bottom: 6px; color: #525d52; font-size: 12px; font-weight: 600; }
    .cart-panel > select, .cart-product input { width: 100%; padding: 10px; border: 1px solid #dcded6; border-radius: 7px; background: white; }
    .cart-lines { display: grid; gap: 12px; margin: 18px 0; padding: 0; list-style: none; }
    .cart-lines li { display: flex; align-items: center; justify-content: space-between; gap: 14px; padding: 12px 0; border-bottom: 1px solid #efeee9; }
    .cart-product { display: grid; flex: 1; gap: 6px; font-size: 13px; }
    .cart-product small { color: #315d46; font-weight: 700; }
    .cart-product input { max-width: 300px; font-size: 12px; }
    .quantity-control { display: flex; align-items: center; gap: 8px; }
    .quantity-control button { width: 30px; height: 30px; border: 1px solid #dcded6; border-radius: 50%; color: #315d46; background: #fff; cursor: pointer; }
    .quantity-control .remove-line { width: auto; padding: 0 8px; border: 0; color: #9b422d; text-decoration: underline; }
    .cart-total { display: flex; justify-content: space-between; margin: 16px 0; color: #315d46; }
    .cart-empty { margin: 0; color: #82877f; font-size: 13px; }
    @media (max-width: 560px) {
      .cart-panel { padding: 17px; }
      .cart-lines li { align-items: flex-start; flex-direction: column; }
      .quantity-control { width: 100%; }
    }
  `]
})
export class MenuComponent implements OnInit {
  private readonly api = inject(RestauranteApiService);
  private readonly messages = inject(MessageService);
  readonly categorias = signal<Categoria[]>([]);
  readonly productos = signal<Producto[]>([]);
  readonly mesas = signal<Mesa[]>([]);
  readonly carrito = signal<LineaPedido[]>([]);
  readonly error = signal('');
  readonly cargandoMenu = signal(false);
  readonly cargandoPedido = signal(false);
  readonly categoriaSeleccionada = signal<number | null>(null);
  mesaSeleccionada: number | null = null;
  readonly productosFiltrados = computed(() => this.productos()
    .filter(producto => this.categoriaSeleccionada() == null || producto.categoriaId === this.categoriaSeleccionada()));
  readonly total = computed(() => this.carrito().reduce((suma, linea) =>
    suma + linea.producto.precio * linea.cantidad, 0));

  ngOnInit(): void {
    this.cargarMenu();
  }

  private cargarMenu(): void {
    this.cargandoMenu.set(true);
    this.error.set('');
    forkJoin({
      categorias: this.api.categorias(),
      productos: this.api.productos(),
      mesas: this.api.mesasDisponibles()
    }).pipe(finalize(() => this.cargandoMenu.set(false))).subscribe({
      next: resultado => {
        this.categorias.set(resultado.categorias);
        this.productos.set(resultado.productos);
        this.mesas.set(resultado.mesas);
      },
      error: () => this.error.set('No se pudo cargar el menú o las mesas disponibles. Comprueba tu conexión y tus permisos.'),
    });
  }

  emoji(categoria: string | null): string {
    const name = (categoria || '').toLowerCase();
    if (name.includes('bebida')) return '🥤';
    if (name.includes('postre')) return '🍰';
    if (name.includes('entrada')) return '🥗';
    if (name.includes('hamburg') || name.includes('carne')) return '🍔';
    return '🍽️';
  }

  agregar(producto: Producto): void {
    this.carrito.update(lineas => {
      const actual = lineas.find(linea => linea.producto.id === producto.id);
      return actual
        ? lineas.map(linea => linea.producto.id === producto.id ? { ...linea, cantidad: linea.cantidad + 1 } : linea)
        : [...lineas, { producto, productoId: producto.id, cantidad: 1, notas: '' }];
    });
  }

  cambiarCantidad(linea: LineaPedido, cambio: number): void {
    this.carrito.update(lineas => lineas
      .map(actual => actual.producto.id === linea.producto.id
        ? { ...actual, cantidad: actual.cantidad + cambio }
        : actual)
      .filter(actual => actual.cantidad > 0));
  }

  quitar(linea: LineaPedido): void {
    this.carrito.update(lineas => lineas.filter(actual => actual.producto.id !== linea.producto.id));
  }

  confirmarPedido(): void {
    if (this.mesaSeleccionada === null || this.carrito().length === 0 || this.cargandoPedido()) return;
    this.cargandoPedido.set(true);
    this.api.crearPedido({
      mesaId: this.mesaSeleccionada,
      detalles: this.carrito().map(({ productoId, cantidad, notas }) => ({ productoId, cantidad, notas }))
    }).subscribe({
      next: pedido => {
        this.messages.add({ severity: 'success', summary: `Pedido #${pedido.id} creado`, detail: 'La cocina ya recibió tu pedido.' });
        this.carrito.set([]);
        this.mesaSeleccionada = null;
        this.cargarMenu();
        this.cargandoPedido.set(false);
      },
      error: () => {
        this.error.set('No se pudo confirmar el pedido. La mesa puede haber sido ocupada o algún producto ya no estar disponible.');
        this.cargandoPedido.set(false);
        this.cargarMenu();
      }
    });
  }
}
