import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alerta, Categoria, Pedido, Producto } from './models';

@Injectable({ providedIn: 'root' })
export class RestauranteApiService {
  private readonly http = inject(HttpClient);
  categorias(): Observable<Categoria[]> { return this.http.get<Categoria[]>('/api/categorias'); }
  productos(): Observable<Producto[]> { return this.http.get<Producto[]>('/api/productos'); }
  crearPedido(): Observable<Pedido> { return this.http.post<Pedido>('/api/pedidos', {}); }
  agregarDetalle(pedidoId: number, productoId: number, cantidad: number, notas = ''): Observable<unknown> {
    return this.http.post('/api/detalles-pedido', { pedidoId, productoId, cantidad, notas });
  }
  alertas(): Observable<Alerta[]> { return this.http.get<Alerta[]>('/api/pedidos/alertas'); }
}
