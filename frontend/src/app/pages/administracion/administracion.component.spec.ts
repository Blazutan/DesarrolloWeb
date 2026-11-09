import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { AdministracionComponent } from './administracion.component';

describe('AdministracionComponent', () => {
  const reporteVacio = {
    desde: '2026-01-01', hasta: '2026-01-31', totalPedidos: 0, totalVentas: 0,
    ticketPromedio: 0, pedidos: []
  };
  const api = {
    categorias: jasmine.createSpy().and.returnValue(of([{ id: 1, nombre: 'Platos' }])),
    productos: jasmine.createSpy().and.returnValue(of([])),
    administrarMesas: jasmine.createSpy().and.returnValue(of([])),
    usuarios: jasmine.createSpy().and.returnValue(of([])),
    reporteVentas: jasmine.createSpy().and.returnValue(of(reporteVacio)),
    reporteTiempos: jasmine.createSpy().and.returnValue(of({
      desde: '2026-01-01', hasta: '2026-01-31', pedidosAnalizados: 0,
      promedioPreparacionSegundos: null, promedioEsperaEntregaSegundos: null, promedioTotalSegundos: null
    })),
    crearCategoria: jasmine.createSpy().and.returnValue(of({ id: 2, nombre: 'Bebidas' }))
  };

  beforeEach(() => {
    Object.values(api).forEach(spy => spy.calls?.reset());
    api.reporteVentas.and.returnValue(of(reporteVacio));
  });

  it('carga las secciones de administración y muestra reportes vacíos', async () => {
    await TestBed.configureTestingModule({
      imports: [AdministracionComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(AdministracionComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Categorías y productos');
    expect(fixture.nativeElement.textContent).toContain('Mesas');
    expect(fixture.nativeElement.textContent).toContain('Usuarios');
    expect(fixture.nativeElement.textContent).toContain('No hay pedidos entregados en este periodo.');
  });

  it('valida el rango antes de pedir reportes', async () => {
    await TestBed.configureTestingModule({
      imports: [AdministracionComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const component = TestBed.createComponent(AdministracionComponent).componentInstance;
    api.reporteVentas.calls.reset();
    component.desde = '2026-02-02';
    component.hasta = '2026-02-01';
    component.consultarReportes();

    expect(component.reporteError()).toContain('rango de fechas válido');
    expect(api.reporteVentas).not.toHaveBeenCalled();
  });

  it('muestra las ventas agrupadas por pedido y permite desplegar sus detalles', async () => {
    api.reporteVentas.and.returnValue(of({
      desde: '2026-01-01',
      hasta: '2026-01-31',
      totalPedidos: 1,
      totalVentas: 14000,
      ticketPromedio: 14000,
      pedidos: [{
        pedidoId: 12,
        fechaEntregado: '2026-01-15T12:30:00',
        mesaNumero: 3,
        total: 14000,
        detalles: [{
          productoId: 4, producto: 'Sopa', cantidad: 2, precioUnitario: 7000, subtotal: 14000
        }]
      }]
    }));

    await TestBed.configureTestingModule({
      imports: [AdministracionComponent],
      providers: [providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(AdministracionComponent);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Ventas por pedido');
    expect(fixture.nativeElement.textContent).toContain('#12');
    expect(fixture.nativeElement.textContent).toContain('Ver detalle del pedido (1)');
    expect(fixture.nativeElement.textContent).toContain('2 × Sopa');
  });
});
