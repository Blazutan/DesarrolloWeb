import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { Categoria, Producto } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

@Component({ selector: 'app-menu', standalone: true, imports: [CommonModule, FormsModule, ButtonModule, CardModule, SelectModule], template: `
  <section class="hero"><div><p class="eyebrow">COCINA FRESCA · BUENOS MOMENTOS</p><h1>Algo rico está<br><em>por suceder.</em></h1><p class="hero-copy">Explora nuestro menú y arma tu pedido favorito.</p></div><div class="hero-art"><span>✳</span><div class="plate"><div class="plate-food">🍲</div></div><small>PREPARADO AL MOMENTO</small></div></section>
  <section class="menu-section"><div class="section-heading"><div><p class="eyebrow">NUESTRA CARTA</p><h2>El menú</h2></div><p class="muted">Ingredientes frescos, recetas con cariño.</p></div>
    <div class="filter-row"><p-select [options]="categorias()" [ngModel]="categoriaSeleccionada()" (ngModelChange)="categoriaSeleccionada.set($event)" optionLabel="nombre" optionValue="id" placeholder="Todas las categorías" [showClear]="true" /><span class="result-count">{{ productosFiltrados().length }} opciones</span></div>
    @if (error()) { <div class="notice error">{{ error() }}</div> }
    @if (productosFiltrados().length) { <div class="product-grid">@for (producto of productosFiltrados(); track producto.id) {
      <p-card styleClass="product-card"><ng-template #header><div class="product-art" [class.unavailable]="!producto.disponibilidad"><span>{{ emoji(producto.categoriaNombre) }}</span><small>{{ producto.categoriaNombre || 'De la casa' }}</small></div></ng-template>
        <div class="product-info"><h3>{{ producto.nombre }}</h3><p>{{ producto.descripcion || 'Una preparación hecha con ingredientes frescos.' }}</p><div class="product-bottom"><strong>{{ producto.precio | currency:'COP':'symbol-narrow':'1.0-0' }}</strong><p-button [label]="producto.disponibilidad ? 'Agregar' : 'Agotado'" [icon]="producto.disponibilidad ? 'pi pi-plus' : 'pi pi-ban'" [disabled]="!producto.disponibilidad" (onClick)="agregar(producto)" /></div></div>
      </p-card>
    }</div> } @else if (!error()) { <div class="empty-state"><span>☕</span><h3>Estamos preparando la carta</h3><p>Vuelve a intentarlo en un momento.</p></div> }
  </section>
` })
export class MenuComponent implements OnInit {
  private readonly api = inject(RestauranteApiService);
  private readonly messages = inject(MessageService);
  readonly categorias = signal<Categoria[]>([]);
  readonly productos = signal<Producto[]>([]);
  readonly error = signal('');
  private pedidoId: number | null = null;
  readonly categoriaSeleccionada = signal<number | null>(null);
  readonly productosFiltrados = computed(() => this.productos().filter(p => this.categoriaSeleccionada() == null || p.categoriaId === this.categoriaSeleccionada()));
  ngOnInit(): void {
    this.cargarMenu();
  }
  private cargarMenu(): void {
    this.api.categorias().subscribe({ next: value => this.categorias.set(value), error: () => this.error.set('No se pudieron cargar las categorías. Comprueba la conexión y tu usuario de mesero.') });
    this.api.productos().subscribe({ next: value => this.productos.set(value), error: () => this.error.set('No se pudieron cargar los productos. Comprueba la conexión y tu usuario de mesero.') });
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
    if (this.pedidoId !== null) { this.agregarAlPedido(this.pedidoId, producto); return; }
    this.api.crearPedido().subscribe({
      next: pedido => { this.pedidoId = pedido.id; this.agregarAlPedido(pedido.id, producto); },
      error: () => this.messages.add({ severity: 'error', summary: 'No se pudo crear el pedido', detail: 'Verifica que tu cuenta tenga el rol de mesero.' })
    });
  }
  private agregarAlPedido(pedidoId: number, producto: Producto): void {
    this.api.agregarDetalle(pedidoId, producto.id, 1).subscribe({
      next: () => this.messages.add({ severity: 'success', summary: `Pedido #${pedidoId}`, detail: `${producto.nombre} se agregó al pedido.` }),
      error: () => this.messages.add({ severity: 'error', summary: 'No se pudo agregar', detail: `No se pudo agregar ${producto.nombre} al pedido #${pedidoId}.` })
    });
  }
}
