import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { CocinaComponent } from './cocina.component';

describe('CocinaComponent', () => {
  const pedido = {
    id: 5, meseroId: 2, estado: 'PENDIENTE', fechaCreacion: '2026-01-01T12:00:00',
    fechaUltimaActualizacion: '2026-01-01T12:00:00', fechaEnPreparacion: null,
    fechaListo: null, fechaEntregado: null, precioTotal: 14000, numeroRecordatorios: 0,
    alertaRecibida: false, motivoCancelacion: null, mesaId: 1, mesaNumero: 3
  };
  const api = {
    pedidos: jasmine.createSpy().and.returnValue(of([pedido])),
    detallesPedido: jasmine.createSpy().and.returnValue(of([{
      id: 9, pedidoId: 5, productoId: 4, productoNombre: 'Sopa', cantidad: 2,
      notas: 'Sin sal', precioUnitario: 7000, subtotal: 14000
    }])),
    cambiarEstadoPedido: jasmine.createSpy().and.returnValue(of({ ...pedido, estado: 'EN PREPARACION' })),
    cancelarPedidoCocina: jasmine.createSpy().and.returnValue(of({ ...pedido, estado: 'CANCELADO' }))
  };

  beforeEach(() => {
    api.pedidos.calls.reset();
    api.detallesPedido.calls.reset();
    api.cambiarEstadoPedido.calls.reset();
    api.cancelarPedidoCocina.calls.reset();
  });

  it('muestra la cola con detalles y estado vacío de pedidos no activos', async () => {
    await TestBed.configureTestingModule({
      imports: [CocinaComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(CocinaComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Mesa 3');
    expect(fixture.nativeElement.textContent).toContain('2 × Sopa');
    expect(fixture.nativeElement.textContent).toContain('Tomar pedido');
  });

  it('avanza un pedido pendiente a preparación', async () => {
    await TestBed.configureTestingModule({
      imports: [CocinaComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const component = TestBed.createComponent(CocinaComponent).componentInstance;
    component.cambiarEstado(pedido, 'EN PREPARACIÓN');
    expect(api.cambiarEstadoPedido).toHaveBeenCalledWith(5, 'EN PREPARACIÓN');
  });

  it('cancela un pedido con motivo y lo quita de la cola activa', async () => {
    await TestBed.configureTestingModule({
      imports: [CocinaComponent],
      providers: [{ provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    spyOn(window, 'confirm').and.returnValue(true);
    const component = TestBed.createComponent(CocinaComponent).componentInstance;
    component.pedidos.set([pedido, { ...pedido, id: 6, estado: 'LISTO' }]);
    component.motivosCancelacion[pedido.id] = '  Producto no disponible  ';
    component.cancelarPedido(pedido);

    expect(api.cancelarPedidoCocina).toHaveBeenCalledWith(5, 'Producto no disponible');
    expect(component.pedidosActivos().map(item => item.id)).toEqual([6]);
    expect(component.motivosCancelacion[pedido.id]).toBeUndefined();
  });
});
