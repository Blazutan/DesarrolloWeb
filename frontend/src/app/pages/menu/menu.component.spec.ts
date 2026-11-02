import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { MenuComponent } from './menu.component';

describe('MenuComponent', () => {
  const productos = [{ id: 7, nombre: 'Sopa', descripcion: 'Del día', precio: 12000, disponibilidad: true, categoriaId: 1, categoriaNombre: 'Platos' }];
  const api = {
    categorias: jasmine.createSpy().and.returnValue(of([{ id: 1, nombre: 'Platos' }])),
    productos: jasmine.createSpy().and.returnValue(of(productos)),
    mesasDisponibles: jasmine.createSpy().and.returnValue(of([{ id: 4, numero: 8, estado: 'LIBRE' }])),
    crearPedido: jasmine.createSpy().and.returnValue(of({ id: 41, estado: 'PENDIENTE', precioTotal: 12000, fechaCreacion: '' }))
  };

  beforeEach(() => {
    api.categorias.calls.reset();
    api.productos.calls.reset();
    api.mesasDisponibles.calls.reset();
    api.crearPedido.calls.reset();
  });

  it('carga menú y mesas y filtra productos por categoría', async () => {
    await TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [MessageService, providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(MenuComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(api.categorias).toHaveBeenCalledTimes(1);
    expect(api.productos).toHaveBeenCalledTimes(1);
    expect(api.mesasDisponibles).toHaveBeenCalledTimes(1);
    expect(component.productosFiltrados().length).toBe(1);
    component.categoriaSeleccionada.set(2);
    expect(component.productosFiltrados().length).toBe(0);
  });

  it('envía una sola operación atómica para crear pedido con mesa y productos', async () => {
    await TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [MessageService, providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const component = TestBed.createComponent(MenuComponent).componentInstance;
    component.agregar(productos[0]);
    component.cambiarCantidad(component.carrito()[0], 1);
    component.mesaSeleccionada = 4;
    component.confirmarPedido();

    expect(api.crearPedido).toHaveBeenCalledWith({
      mesaId: 4,
      detalles: [{ productoId: 7, cantidad: 2, notas: '' }]
    });
    expect(component.carrito()).toEqual([]);
  });
});
