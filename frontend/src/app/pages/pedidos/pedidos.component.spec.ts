import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { PedidosComponent } from './pedidos.component';

describe('PedidosComponent', () => {
  const alertas = [{ pedidoId: 12, fechaListo: '2026-01-01T12:00:00', numeroRecordatorios: 1 }];
  const api = { alertas: jasmine.createSpy().and.returnValue(of(alertas)) };

  beforeEach(() => {
    sessionStorage.setItem('poli-auth', JSON.stringify({ usuario: 'mesero@test.com', clave: 'secreto' }));
    api.alertas.calls.reset();
  });

  it('consulta y muestra las alertas de pedidos listos', async () => {
    await TestBed.configureTestingModule({
      imports: [PedidosComponent],
      providers: [{ provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(PedidosComponent);
    fixture.detectChanges();

    expect(api.alertas).toHaveBeenCalledTimes(1);
    expect(fixture.nativeElement.textContent).toContain('Pedido #12');
    expect(fixture.nativeElement.textContent).toContain('Listo para recoger');
  });
});
