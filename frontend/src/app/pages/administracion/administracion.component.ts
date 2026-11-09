import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { Observable, finalize, forkJoin } from 'rxjs';
import { Categoria, Mesa, Producto, ProductoRequest, ReporteTiempos, ReporteVentas, Usuario } from '../../core/models';
import { RestauranteApiService } from '../../core/restaurante-api.service';

@Component({
  selector: 'app-administracion',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule],
  template: `
    <section class="admin-page">
      <header class="admin-heading">
        <div><p class="eyebrow">CONTROL DEL RESTAURANTE</p><h1>Administración</h1>
          <p>Gestiona la carta, las mesas, el personal y los resultados del restaurante.</p></div>
        <p-button label="Actualizar datos" icon="pi pi-refresh" [outlined]="true" [loading]="cargando()"
          (onClick)="cargar()" />
      </header>
      @if (error()) { <p class="notice error" role="alert">{{ error() }}</p> }
      @if (mensaje()) { <p class="notice success" role="status">{{ mensaje() }}</p> }
      @if (cargando() && !inicializado()) { <p class="loading-state" role="status">Cargando administración...</p> }
      @else {
        <nav class="section-nav" aria-label="Secciones de administración">
          <a href="#menu-admin">Menú</a><a href="#mesas-admin">Mesas</a>
          <a href="#usuarios-admin">Usuarios</a><a href="#reportes-admin">Reportes</a>
        </nav>

        <section id="menu-admin" class="admin-section">
          <div class="section-title"><div><p class="eyebrow">CARTA</p><h2>Categorías y productos</h2></div></div>
          <div class="admin-columns">
            <article class="admin-card">
              <h3>Categorías</h3>
              <form class="inline-form" (ngSubmit)="crearCategoria()" #categoriaForm="ngForm">
                <label for="nueva-categoria">Nombre</label>
                <input id="nueva-categoria" name="nuevaCategoria" [(ngModel)]="nuevaCategoria" required maxlength="80" />
                <button type="submit" [disabled]="categoriaForm.invalid || guardando()">Crear categoría</button>
              </form>
              <ul class="admin-list">
                @for (categoria of categorias(); track categoria.id) {
                  <li><span>{{ categoria.nombre }}</span><div class="row-actions">
                    <button type="button" class="text-button" (click)="editarCategoria(categoria)">Editar</button>
                    <button type="button" class="danger-button" (click)="eliminarCategoria(categoria)">Eliminar</button>
                  </div></li>
                } @empty { <li class="empty-row">No hay categorías.</li> }
              </ul>
            </article>
            <article class="admin-card">
              <h3>Nuevo producto</h3>
              <form class="admin-form" (ngSubmit)="crearProducto()" #productoForm="ngForm">
                <label for="producto-nombre">Nombre</label>
                <input id="producto-nombre" name="productoNombre" [(ngModel)]="nuevoProducto.nombre" required maxlength="120" />
                <label for="producto-descripcion">Descripción</label>
                <textarea id="producto-descripcion" name="productoDescripcion" [(ngModel)]="nuevoProducto.descripcion" maxlength="500"></textarea>
                <div class="form-pair">
                  <div><label for="producto-precio">Precio</label><input id="producto-precio" type="number" min="0" step="100" name="productoPrecio" [(ngModel)]="nuevoProducto.precio" required /></div>
                  <div><label for="producto-categoria">Categoría</label>
                    <select id="producto-categoria" name="productoCategoria" [(ngModel)]="nuevoProducto.categoriaId" required>
                      <option [ngValue]="0" disabled>Selecciona</option>
                      @for (categoria of categorias(); track categoria.id) { <option [ngValue]="categoria.id">{{ categoria.nombre }}</option> }
                    </select>
                  </div>
                </div>
                <label class="check-label"><input type="checkbox" name="productoDisponible" [(ngModel)]="nuevoProducto.disponibilidad" /> Disponible para pedidos</label>
                <button type="submit" [disabled]="productoForm.invalid || nuevoProducto.categoriaId < 1 || guardando()">Guardar producto</button>
              </form>
            </article>
          </div>
          <div class="table-wrap">
            <table><caption>Productos registrados</caption><thead><tr><th>Producto</th><th>Precio</th><th>Categoría</th><th>Disponibilidad</th><th>Acciones</th></tr></thead>
              <tbody>@for (producto of productos(); track producto.id) {
                <tr>
                  <td><input [attr.aria-label]="'Nombre de ' + producto.nombre" [(ngModel)]="producto.nombre" maxlength="120" /></td>
                  <td><input type="number" min="0" step="100" [attr.aria-label]="'Precio de ' + producto.nombre" [(ngModel)]="producto.precio" /></td>
                  <td><select [attr.aria-label]="'Categoría de ' + producto.nombre" [(ngModel)]="producto.categoriaId">
                    @for (categoria of categorias(); track categoria.id) { <option [ngValue]="categoria.id">{{ categoria.nombre }}</option> }
                  </select></td>
                  <td><input type="checkbox" [attr.aria-label]="'Disponibilidad de ' + producto.nombre" [(ngModel)]="producto.disponibilidad" /></td>
                  <td class="row-actions"><button type="button" (click)="actualizarProducto(producto)">Guardar</button>
                    <button type="button" class="danger-button" (click)="eliminarProducto(producto)">Eliminar</button></td>
                </tr>
              } @empty { <tr><td colspan="5" class="empty-row">No hay productos registrados.</td></tr> }</tbody>
            </table>
          </div>
        </section>

        <section id="mesas-admin" class="admin-section">
          <div class="section-title"><div><p class="eyebrow">SALÓN</p><h2>Mesas</h2></div></div>
          <form class="inline-form table-create" (ngSubmit)="crearMesa()" #mesaForm="ngForm">
            <label for="mesa-numero">Número</label><input id="mesa-numero" type="number" name="numeroMesa" min="1" [(ngModel)]="numeroMesa" required />
            <button type="submit" [disabled]="mesaForm.invalid || guardando()">Agregar mesa</button>
          </form>
          <div class="table-wrap"><table><caption>Estado de las mesas</caption>
            <thead><tr><th>Número</th><th>Estado</th><th>Acciones</th></tr></thead>
            <tbody>@for (mesa of mesas(); track mesa.id) {
              <tr><td><input type="number" min="1" [attr.aria-label]="'Número de mesa ' + mesa.numero" [(ngModel)]="mesa.numero" /></td>
                <td><span class="status-pill" [class.preparing]="mesa.estado === 'OCUPADA'">{{ mesa.estado }}</span></td>
                <td class="row-actions"><button type="button" (click)="guardarMesa(mesa)">Guardar número</button>
                  <button type="button" (click)="alternarMesa(mesa)">{{ mesa.estado === 'LIBRE' ? 'Marcar ocupada' : 'Marcar libre' }}</button>
                  <button type="button" class="danger-button" (click)="eliminarMesa(mesa)">Eliminar</button></td></tr>
            } @empty { <tr><td colspan="3" class="empty-row">No hay mesas registradas.</td></tr> }</tbody>
          </table></div>
        </section>

        <section id="usuarios-admin" class="admin-section">
          <div class="section-title"><div><p class="eyebrow">PERSONAL</p><h2>Usuarios</h2></div></div>
          <article class="admin-card">
            <h3>Crear cuenta</h3>
            <form class="admin-form user-create-form" (ngSubmit)="crearUsuario()" #usuarioForm="ngForm">
              <div><label for="usuario-nombre">Nombre</label><input id="usuario-nombre" name="usuarioNombre" [(ngModel)]="nuevoUsuario.nombre" required maxlength="120" /></div>
              <div><label for="usuario-correo">Correo</label><input id="usuario-correo" type="email" name="usuarioCorreo" [(ngModel)]="nuevoUsuario.correo" required email /></div>
              <div><label for="usuario-rol">Rol</label><select id="usuario-rol" name="usuarioRol" [(ngModel)]="nuevoUsuario.rol">
                <option value="MESERO">Mesero</option><option value="COCINA">Cocina</option><option value="ADMINISTRADOR">Administrador</option>
              </select></div>
              <div><label for="usuario-password">Contraseña inicial</label><input id="usuario-password" type="password" name="usuarioPassword" [(ngModel)]="nuevoUsuario.password" required minlength="8" autocomplete="new-password" /></div>
              <button type="submit" [disabled]="usuarioForm.invalid || guardando()">Crear usuario</button>
            </form>
          </article>
          <div class="table-wrap"><table><caption>Cuentas del sistema</caption><thead><tr><th>Nombre</th><th>Correo</th><th>Rol</th><th>Contraseña nueva (opcional)</th><th>Acciones</th></tr></thead>
            <tbody>@for (usuario of usuarios(); track usuario.rol + usuario.id) {
              <tr><td><input [attr.aria-label]="'Nombre de ' + usuario.correo" [(ngModel)]="usuario.nombre" required /></td>
                <td><input type="email" [attr.aria-label]="'Correo de ' + usuario.nombre" [(ngModel)]="usuario.correo" required /></td>
                <td>{{ usuario.rol }}</td><td><input type="password" [attr.aria-label]="'Nueva contraseña de ' + usuario.correo" [(ngModel)]="contrasenas[usuario.id]" autocomplete="new-password" /></td>
                <td class="row-actions"><button type="button" (click)="actualizarUsuario(usuario)">Guardar</button>
                  <button type="button" class="danger-button" (click)="eliminarUsuario(usuario)">Eliminar</button></td></tr>
            } @empty { <tr><td colspan="5" class="empty-row">No hay cuentas registradas.</td></tr> }</tbody>
          </table></div>
        </section>

        <section id="reportes-admin" class="admin-section">
          <div class="section-title"><div><p class="eyebrow">RESULTADOS</p><h2>Reportes</h2></div></div>
          <form class="inline-form report-filter" (ngSubmit)="consultarReportes()" #reporteForm="ngForm">
            <div><label for="reporte-desde">Desde</label><input id="reporte-desde" type="date" name="desde" [(ngModel)]="desde" required /></div>
            <div><label for="reporte-hasta">Hasta</label><input id="reporte-hasta" type="date" name="hasta" [(ngModel)]="hasta" required /></div>
            <button type="submit" [disabled]="reporteForm.invalid || guardando()">Consultar periodo</button>
          </form>
          @if (reporteError()) { <p class="notice error" role="alert">{{ reporteError() }}</p> }
          @if (ventas(); as reporte) {
            @if (reporte.totalPedidos === 0) { <p class="empty-row">No hay pedidos entregados en este periodo.</p> }
            <div class="report-cards">
              <article><span>Ventas</span><strong>{{ reporte.totalVentas | currency:'COP':'symbol-narrow':'1.0-0' }}</strong></article>
              <article><span>Pedidos entregados</span><strong>{{ reporte.totalPedidos }}</strong></article>
              <article><span>Ticket promedio</span><strong>{{ reporte.ticketPromedio | currency:'COP':'symbol-narrow':'1.0-0' }}</strong></article>
              <article><span>Preparación promedio</span><strong>{{ formatoTiempo(tiempos()?.promedioPreparacionSegundos) }}</strong></article>
              <article><span>Espera de entrega</span><strong>{{ formatoTiempo(tiempos()?.promedioEsperaEntregaSegundos) }}</strong></article>
              <article><span>Tiempo total</span><strong>{{ formatoTiempo(tiempos()?.promedioTotalSegundos) }}</strong></article>
            </div>
            <div class="table-wrap"><table><caption>Ventas por pedido</caption>
              <thead><tr><th>Pedido</th><th>Entregado</th><th>Mesa</th><th>Total</th><th>Detalle</th></tr></thead>
              <tbody>@for (pedido of reporte.pedidos; track pedido.pedidoId) {
                <tr>
                  <td>#{{ pedido.pedidoId }}</td>
                  <td>{{ pedido.fechaEntregado | date:'short' }}</td>
                  <td>{{ pedido.mesaNumero ?? '—' }}</td>
                  <td>{{ pedido.total | currency:'COP':'symbol-narrow':'1.0-0' }}</td>
                  <td><details class="order-details">
                    <summary>Ver detalle del pedido ({{ pedido.detalles.length }})</summary>
                    <ul>@for (detalle of pedido.detalles; track $index) {
                      <li>
                        <span>{{ detalle.cantidad }} × {{ detalle.producto }}</span>
                        <span>{{ detalle.precioUnitario | currency:'COP':'symbol-narrow':'1.0-0' }} c/u</span>
                        <strong>{{ detalle.subtotal | currency:'COP':'symbol-narrow':'1.0-0' }}</strong>
                      </li>
                    } @empty { <li class="empty-row">El pedido no tiene detalles registrados.</li> }</ul>
                  </details></td>
                </tr>
              } @empty { <tr><td colspan="5" class="empty-row">No hay pedidos entregados en este periodo.</td></tr> }</tbody>
            </table></div>
          }
        </section>
      }
    </section>
  `,
  styles: [`
    .admin-page { max-width: 1120px; min-height: 70vh; margin: auto; padding: 42px 24px 70px; }
    .admin-heading, .section-title { display: flex; align-items: center; justify-content: space-between; gap: 20px; }
    .admin-heading { margin-bottom: 24px; }
    .admin-heading h1 { margin: 7px 0; color: #283d30; font: 500 clamp(34px, 5vw, 48px)/1.1 'Playfair Display', serif; }
    .admin-heading p:not(.eyebrow) { color: #777e76; font-size: 13px; }
    .section-nav { display: flex; flex-wrap: wrap; gap: 10px; padding: 12px 0 18px; border-bottom: 1px solid #e8e7e2; }
    .section-nav a { padding: 8px 12px; border-radius: 20px; color: #315d46; background: #edf1e9; font-size: 12px; font-weight: 600; }
    .admin-section { padding-top: 38px; scroll-margin-top: 20px; }
    .section-title { margin-bottom: 17px; }
    .section-title .eyebrow { margin-bottom: 5px; }
    .section-title h2 { margin: 0; color: #283d30; font: 500 28px 'Playfair Display', serif; }
    .admin-columns { display: grid; grid-template-columns: minmax(230px, .75fr) minmax(320px, 1.25fr); gap: 16px; }
    .admin-card { padding: 20px; background: #fff; border: 1px solid #ecece5; border-radius: 12px; }
    .admin-card h3 { margin: 0 0 16px; color: #344538; font: 500 19px 'Playfair Display', serif; }
    label { display: block; margin: 0 0 5px; color: #525d52; font-size: 12px; font-weight: 600; }
    input, select, textarea { min-width: 0; width: 100%; padding: 9px 10px; border: 1px solid #dcded6; border-radius: 7px; color: #27352d; background: #fff; font: inherit; font-size: 13px; }
    input:focus, select:focus, textarea:focus { outline: 2px solid #78a187; outline-offset: 1px; }
    textarea { min-height: 65px; resize: vertical; }
    .admin-form { display: grid; gap: 9px; }
    .form-pair { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
    .check-label { display: flex; align-items: center; gap: 8px; }
    .check-label input { width: auto; }
    .admin-form button, .inline-form button, .row-actions button { padding: 8px 11px; border: 0; border-radius: 7px; color: white; background: #315d46; cursor: pointer; font: inherit; font-size: 12px; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    .inline-form { display: flex; align-items: end; gap: 10px; }
    .inline-form input { max-width: 240px; }
    .admin-list { display: grid; gap: 5px; margin: 16px 0 0; padding: 0; list-style: none; }
    .admin-list li { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 9px 0; border-bottom: 1px solid #f0f0eb; font-size: 13px; }
    .row-actions { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
    .row-actions button { padding: 6px 8px; }
    .row-actions .danger-button { color: #9b422d; background: #fbeae6; }
    .empty-row { padding: 16px; color: #858980; text-align: center; font-size: 12px; }
    .table-create { margin: 0 0 14px; }
    .table-create input { max-width: 130px; }
    .table-wrap { margin-top: 16px; overflow-x: auto; background: white; border: 1px solid #ecece5; border-radius: 10px; }
    table { width: 100%; border-collapse: collapse; font-size: 12px; }
    caption { padding: 12px; color: #6f756b; text-align: left; font-weight: 700; }
    th, td { padding: 10px; border-top: 1px solid #efeee9; text-align: left; }
    th { color: #657365; background: #f8f8f5; font-size: 11px; }
    td input:not([type=checkbox]), td select { min-width: 110px; padding: 7px; }
    .status-pill.preparing { color: #9a5c1f; background: #fff3df; }
    .user-create-form { grid-template-columns: repeat(4, minmax(140px, 1fr)) auto; align-items: end; }
    .user-create-form button { min-height: 36px; }
    .report-filter { align-items: end; flex-wrap: wrap; }
    .report-filter input { min-width: 160px; }
    .report-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(155px, 1fr)); gap: 12px; margin-top: 18px; }
    .report-cards article { display: grid; gap: 8px; padding: 16px; background: #fff; border: 1px solid #ecece5; border-radius: 10px; }
    .report-cards span { color: #777e76; font-size: 11px; }
    .report-cards strong { color: #315d46; font-size: 17px; }
    .order-details summary { color: #315d46; cursor: pointer; font-weight: 600; white-space: nowrap; }
    .order-details ul { display: grid; gap: 6px; min-width: 250px; margin: 8px 0 0; padding: 0; list-style: none; }
    .order-details li { display: flex; flex-wrap: wrap; justify-content: space-between; gap: 6px; padding: 6px 0; border-top: 1px solid #efeee9; }
    .notice.success { background: #edf5ec; color: #315d46; }
    .loading-state { padding: 38px; text-align: center; color: #777e76; }
    @media (max-width: 760px) {
      .admin-page { padding: 30px 16px 50px; }
      .admin-heading { align-items: flex-start; flex-direction: column; }
      .admin-columns { grid-template-columns: 1fr; }
      .user-create-form { grid-template-columns: repeat(2, minmax(0, 1fr)); }
      .user-create-form button { grid-column: 1 / -1; }
      .inline-form { align-items: flex-start; flex-direction: column; }
      .inline-form input { max-width: none; }
    }
  `]
})
export class AdministracionComponent implements OnInit {
  private readonly api = inject(RestauranteApiService);
  readonly categorias = signal<Categoria[]>([]);
  readonly productos = signal<Producto[]>([]);
  readonly mesas = signal<Mesa[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly ventas = signal<ReporteVentas | null>(null);
  readonly tiempos = signal<ReporteTiempos | null>(null);
  readonly error = signal('');
  readonly reporteError = signal('');
  readonly mensaje = signal('');
  readonly cargando = signal(false);
  readonly guardando = signal(false);
  readonly inicializado = signal(false);
  nuevaCategoria = '';
  nuevoProducto: ProductoRequest = { nombre: '', descripcion: '', precio: 0, disponibilidad: true, categoriaId: 0 };
  numeroMesa = 1;
  nuevoUsuario: { nombre: string; correo: string; password: string; rol: Usuario['rol'] } =
    { nombre: '', correo: '', password: '', rol: 'MESERO' };
  readonly contrasenas: Record<number, string> = {};
  desde = this.fechaMesActual();
  hasta = new Date().toISOString().slice(0, 10);

  ngOnInit(): void {
    this.cargar();
    this.consultarReportes();
  }

  cargar(): void {
    this.error.set('');
    this.cargando.set(true);
    forkJoin({
      categorias: this.api.categorias(),
      productos: this.api.productos(),
      mesas: this.api.administrarMesas(),
      usuarios: this.api.usuarios()
    }).pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: datos => {
        this.categorias.set(datos.categorias);
        this.productos.set(datos.productos);
        this.mesas.set(datos.mesas);
        this.usuarios.set(datos.usuarios);
        this.inicializado.set(true);
      },
      error: () => this.error.set('No se pudieron cargar todos los datos de administración. Verifica la conexión y tus permisos.'),
    });
  }

  crearCategoria(): void {
    const nombre = this.nuevaCategoria.trim();
    if (!nombre) return;
    this.ejecutar(this.api.crearCategoria(nombre), 'Categoría creada.', () => this.nuevaCategoria = '');
  }

  editarCategoria(categoria: Categoria): void {
    const nombre = window.prompt('Nuevo nombre de categoría', categoria.nombre)?.trim();
    if (!nombre || nombre === categoria.nombre) return;
    this.ejecutar(this.api.actualizarCategoria(categoria.id, nombre), 'Categoría actualizada.');
  }

  eliminarCategoria(categoria: Categoria): void {
    if (!window.confirm(`¿Eliminar la categoría "${categoria.nombre}"?`)) return;
    this.ejecutar(this.api.eliminarCategoria(categoria.id), 'Categoría eliminada.');
  }

  crearProducto(): void {
    this.ejecutar(this.api.crearProducto(this.nuevoProducto), 'Producto creado.', () => {
      this.nuevoProducto = { nombre: '', descripcion: '', precio: 0, disponibilidad: true, categoriaId: 0 };
    });
  }

  actualizarProducto(producto: Producto): void {
    if (producto.categoriaId === null) {
      this.error.set('Selecciona una categoría válida para el producto.');
      return;
    }
    this.ejecutar(this.api.actualizarProducto(producto.id, {
      nombre: producto.nombre,
      descripcion: producto.descripcion ?? '',
      precio: producto.precio,
      disponibilidad: producto.disponibilidad,
      categoriaId: producto.categoriaId
    }), 'Producto actualizado.');
  }

  eliminarProducto(producto: Producto): void {
    if (!window.confirm(`¿Eliminar el producto "${producto.nombre}"?`)) return;
    this.ejecutar(this.api.eliminarProducto(producto.id), 'Producto eliminado.');
  }

  crearMesa(): void {
    this.ejecutar(this.api.crearMesa({ numero: this.numeroMesa, estado: 'LIBRE' }), 'Mesa creada.');
  }

  guardarMesa(mesa: Mesa): void {
    this.ejecutar(this.api.actualizarMesa(mesa.id, { numero: mesa.numero, estado: mesa.estado }), 'Mesa actualizada.');
  }

  alternarMesa(mesa: Mesa): void {
    const estado = mesa.estado === 'LIBRE' ? 'OCUPADA' : 'LIBRE';
    this.ejecutar(this.api.cambiarEstadoMesa(mesa.id, estado), 'Estado de mesa actualizado.');
  }

  eliminarMesa(mesa: Mesa): void {
    if (!window.confirm(`¿Eliminar la mesa ${mesa.numero}?`)) return;
    this.ejecutar(this.api.eliminarMesa(mesa.id), 'Mesa eliminada.');
  }

  crearUsuario(): void {
    const { nombre, correo, password, rol } = this.nuevoUsuario;
    this.ejecutar(this.api.crearUsuario(rol, { nombre, correo, password }), 'Usuario creado.', () => {
      this.nuevoUsuario = { nombre: '', correo: '', password: '', rol: 'MESERO' };
    });
  }

  actualizarUsuario(usuario: Usuario): void {
    const password = this.contrasenas[usuario.id]?.trim();
    this.ejecutar(this.api.actualizarUsuario(usuario.rol, usuario.id, {
      nombre: usuario.nombre,
      correo: usuario.correo,
      ...(password ? { password } : {})
    }), 'Usuario actualizado.');
    delete this.contrasenas[usuario.id];
  }

  eliminarUsuario(usuario: Usuario): void {
    if (!window.confirm(`¿Eliminar la cuenta de ${usuario.correo}?`)) return;
    this.ejecutar(this.api.eliminarUsuario(usuario.rol, usuario.id), 'Usuario eliminado.');
  }

  consultarReportes(): void {
    if (!this.desde || !this.hasta || this.desde > this.hasta) {
      this.reporteError.set('Selecciona un rango de fechas válido.');
      return;
    }
    this.reporteError.set('');
    this.guardando.set(true);
    forkJoin({
      ventas: this.api.reporteVentas(this.desde, this.hasta),
      tiempos: this.api.reporteTiempos(this.desde, this.hasta)
    }).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: reportes => {
        this.ventas.set(reportes.ventas);
        this.tiempos.set(reportes.tiempos);
      },
      error: () => this.reporteError.set('No se pudieron consultar los reportes para el periodo seleccionado.'),
    });
  }

  formatoTiempo(segundos: number | null | undefined): string {
    if (segundos === null || segundos === undefined) return 'Sin datos';
    const minutos = Math.floor(segundos / 60);
    const resto = segundos % 60;
    return minutos ? `${minutos} min ${resto} s` : `${resto} s`;
  }

  private ejecutar<T>(operacion: Observable<T>, confirmacion: string, alCompletar?: () => void): void {
    this.error.set('');
    this.mensaje.set('');
    this.guardando.set(true);
    operacion.pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: () => {
        this.mensaje.set(confirmacion);
        alCompletar?.();
        this.cargar();
      },
      error: () => this.error.set('La operación no se pudo completar. Revisa los datos y vuelve a intentarlo.'),
    });
  }

  private fechaMesActual(): string {
    const hoy = new Date();
    return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-01`;
  }
}
