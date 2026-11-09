import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RestauranteApiService } from './restaurante-api.service';

describe('RestauranteApiService', () => {
  let service: RestauranteApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(RestauranteApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envía la solicitud de pedido con mesa y detalles en el contrato de la API', () => {
    const body = { mesaId: 6, detalles: [{ productoId: 3, cantidad: 2, notas: 'sin sal' }] };
    service.crearPedido(body).subscribe();
    const request = http.expectOne('/api/pedidos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush({ id: 11 });
  });

  it('codifica el estado como parámetro de la actualización de cocina', () => {
    service.cambiarEstadoPedido(11, 'EN PREPARACIÓN').subscribe();
    const request = http.expectOne(request =>
      request.url === '/api/pedidos/11/estado' && request.params.get('estado') === 'EN PREPARACIÓN');
    expect(request.request.method).toBe('PATCH');
    request.flush({ id: 11 });
  });

  it('cancela el pedido desde la operación autorizada para cocina', () => {
    service.cancelarPedidoCocina(11, 'Producto no disponible').subscribe();
    const request = http.expectOne('/api/pedidos/11/cancelacion-cocina');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ motivo: 'Producto no disponible' });
    request.flush({ id: 11, estado: 'CANCELADO' });
  });

  it('envía el periodo seleccionado en la consulta del reporte', () => {
    service.reporteVentas('2026-01-01', '2026-01-31').subscribe();
    const request = http.expectOne(request =>
      request.url === '/api/administracion/reportes/ventas'
      && request.params.get('desde') === '2026-01-01'
      && request.params.get('hasta') === '2026-01-31');
    expect(request.request.method).toBe('GET');
    request.flush({ desde: '2026-01-01', hasta: '2026-01-31', totalPedidos: 0, totalVentas: 0, ticketPromedio: 0, pedidos: [] });
  });
});
