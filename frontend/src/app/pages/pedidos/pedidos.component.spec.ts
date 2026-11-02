import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { PedidosComponent } from './pedidos.component';

describe('PedidosComponent', () => {
  const pedido = {
    id: 12, meseroId: 3, estado: 'LISTO', fechaCreacion: '2026-01-01T11:00:00',
    fechaUltimaActualizacion: '2026-01-01T12:00:00', fechaEnPreparacion: null,
    fechaListo: '2026-01-01T12:00:00', fechaEntregado: null, precioTotal: 12500,
    numeroRecordatorios: 1, alertaRecibida: false, motivoCancelacion: null, mesaId: 4, mesaNumero: 8
  };
  const api = {
    misPedidos: jasmine.createSpy().and.returnValue(of([pedido])),
    alertas: jasmine.createSpy().and.returnValue(of([{ pedidoId: 12, fechaListo: pedido.fechaListo, numeroRecordatorios: 1 }])),
    recibirAlerta: jasmine.createSpy().and.returnValue(of(pedido)),
    entregarPedido: jasmine.createSpy().and.returnValue(of(pedido)),
    cancelarPedido: jasmine.createSpy().and.returnValue(of(pedido))
  };

  beforeEach(() => {
    api.misPedidos.calls.reset();
    api.alertas.calls.reset();
    api.recibirAlerta.calls.reset();
    api.entregarPedido.calls.reset();
    api.cancelarPedido.calls.reset();
  });

  it('carga y muestra pedidos propios y alertas listas', async () => {
    await TestBed.configureTestingModule({
      imports: [PedidosComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(PedidosComponent);
    fixture.detectChanges();

    expect(api.misPedidos).toHaveBeenCalledTimes(1);
    expect(api.alertas).toHaveBeenCalledTimes(1);
    expect(fixture.nativeElement.textContent).toContain('Pedido #12');
    expect(fixture.nativeElement.textContent).toContain('Mesa 8');
    expect(fixture.nativeElement.textContent).toContain('Recibí el aviso');
  });

  it('permite recibir el aviso y marcar el pedido como entregado', async () => {
    await TestBed.configureTestingModule({
      imports: [PedidosComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const component = TestBed.createComponent(PedidosComponent).componentInstance;
    component.recibirAlerta(pedido);
    spyOn(window, 'confirm').and.returnValue(true);
    component.entregar(pedido);

    expect(api.recibirAlerta).toHaveBeenCalledWith(12);
    expect(api.entregarPedido).toHaveBeenCalledWith(12);
  });
});
