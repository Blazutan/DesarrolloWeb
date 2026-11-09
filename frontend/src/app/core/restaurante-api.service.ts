import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Alerta, Categoria, DetallePedido, Mesa, Pedido, PedidoRequest, Producto, ProductoRequest,
  ReporteTiempos, ReporteVentas, Usuario, UsuarioRequest
} from './models';

@Injectable({ providedIn: 'root' })
export class RestauranteApiService {
  private readonly http = inject(HttpClient);
  usuarioActual(): Observable<{ correo: string; rol: string }> {
    return this.http.get<{ correo: string; rol: string }>('/api/auth/me');
  }
  categorias(): Observable<Categoria[]> { return this.http.get<Categoria[]>('/api/categorias'); }
  productos(): Observable<Producto[]> { return this.http.get<Producto[]>('/api/productos'); }
  crearPedido(datos: PedidoRequest): Observable<Pedido> { return this.http.post<Pedido>('/api/pedidos', datos); }
  agregarDetalle(pedidoId: number, productoId: number, cantidad: number, notas = ''): Observable<unknown> {
    return this.http.post('/api/detalles-pedido', { pedidoId, productoId, cantidad, notas });
  }
  alertas(): Observable<Alerta[]> { return this.http.get<Alerta[]>('/api/pedidos/alertas'); }
  misPedidos(): Observable<Pedido[]> { return this.http.get<Pedido[]>('/api/pedidos/mis-pedidos'); }
  pedidos(): Observable<Pedido[]> { return this.http.get<Pedido[]>('/api/pedidos'); }
  detallesPedido(): Observable<DetallePedido[]> { return this.http.get<DetallePedido[]>('/api/detalles-pedido'); }
  cambiarEstadoPedido(id: number, estado: 'EN PREPARACIÓN' | 'LISTO'): Observable<Pedido> {
    return this.http.patch<Pedido>(`/api/pedidos/${id}/estado`, null, { params: { estado } });
  }
  entregarPedido(id: number): Observable<Pedido> { return this.http.patch<Pedido>(`/api/pedidos/${id}/entrega`, null); }
  recibirAlerta(id: number): Observable<Pedido> { return this.http.patch<Pedido>(`/api/pedidos/${id}/recibido`, null); }
  cancelarPedido(id: number, motivo: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`/api/pedidos/${id}/cancelacion`, { motivo });
  }
  cancelarPedidoCocina(id: number, motivo: string): Observable<Pedido> {
    return this.http.patch<Pedido>(`/api/pedidos/${id}/cancelacion-cocina`, { motivo });
  }
  mesasDisponibles(): Observable<Mesa[]> { return this.http.get<Mesa[]>('/api/mesas/disponibles'); }
  administrarMesas(): Observable<Mesa[]> { return this.http.get<Mesa[]>('/api/administracion/mesas'); }
  crearMesa(datos: { numero: number; estado: string }): Observable<Mesa> {
    return this.http.post<Mesa>('/api/administracion/mesas', datos);
  }
  actualizarMesa(id: number, datos: { numero: number; estado: string }): Observable<Mesa> {
    return this.http.put<Mesa>(`/api/administracion/mesas/${id}`, datos);
  }
  cambiarEstadoMesa(id: number, estado: Mesa['estado']): Observable<Mesa> {
    return this.http.patch<Mesa>(`/api/administracion/mesas/${id}/estado`, null, { params: { estado } });
  }
  eliminarMesa(id: number): Observable<void> { return this.http.delete<void>(`/api/administracion/mesas/${id}`); }
  crearCategoria(nombre: string): Observable<Categoria> {
    return this.http.post<Categoria>('/api/categorias', { nombre });
  }
  actualizarCategoria(id: number, nombre: string): Observable<Categoria> {
    return this.http.put<Categoria>(`/api/categorias/${id}`, { nombre });
  }
  eliminarCategoria(id: number): Observable<void> { return this.http.delete<void>(`/api/categorias/${id}`); }
  crearProducto(datos: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>('/api/productos', datos);
  }
  actualizarProducto(id: number, datos: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`/api/productos/${id}`, datos);
  }
  eliminarProducto(id: number): Observable<void> { return this.http.delete<void>(`/api/productos/${id}`); }
  usuarios(): Observable<Usuario[]> { return this.http.get<Usuario[]>('/api/administracion/usuarios'); }
  crearUsuario(rol: Usuario['rol'], datos: UsuarioRequest): Observable<Usuario> {
    return this.http.post<Usuario>(`/api/administracion/usuarios/${rol}`, datos);
  }
  actualizarUsuario(rol: Usuario['rol'], id: number, datos: UsuarioRequest): Observable<Usuario> {
    return this.http.put<Usuario>(`/api/administracion/usuarios/${rol}/${id}`, datos);
  }
  eliminarUsuario(rol: Usuario['rol'], id: number): Observable<void> {
    return this.http.delete<void>(`/api/administracion/usuarios/${rol}/${id}`);
  }
  reporteVentas(desde: string, hasta: string): Observable<ReporteVentas> {
    return this.http.get<ReporteVentas>('/api/administracion/reportes/ventas', {
      params: new HttpParams().set('desde', desde).set('hasta', hasta)
    });
  }
  reporteTiempos(desde: string, hasta: string): Observable<ReporteTiempos> {
    return this.http.get<ReporteTiempos>('/api/administracion/reportes/tiempos', {
      params: new HttpParams().set('desde', desde).set('hasta', hasta)
    });
  }
}
