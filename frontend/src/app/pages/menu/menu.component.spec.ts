import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { RestauranteApiService } from '../../core/restaurante-api.service';
import { MenuComponent } from './menu.component';

describe('MenuComponent', () => {
  const api = {
    categorias: jasmine.createSpy().and.returnValue(of([{ id: 1, nombre: 'Platos' }])),
    productos: jasmine.createSpy().and.returnValue(of([{ id: 7, nombre: 'Sopa', descripcion: 'Del día', precio: 12000, disponibilidad: true, categoriaId: 1, categoriaNombre: 'Platos' }])),
    crearPedido: jasmine.createSpy().and.returnValue(of({ id: 41, estado: 'PENDIENTE', precioTotal: 0, fechaCreacion: '' })),
    agregarDetalle: jasmine.createSpy().and.returnValue(of({}))
  };

  beforeEach(() => {
    sessionStorage.setItem('poli-auth', JSON.stringify({ usuario: 'mesero@test.com', clave: 'secreto', rol: 'MESERO' }));
    api.categorias.calls.reset();
    api.productos.calls.reset();
    api.crearPedido.calls.reset();
    api.agregarDetalle.calls.reset();
  });

  it('carga productos y categorías y filtra por categoría', async () => {
    await TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [MessageService, providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const fixture = TestBed.createComponent(MenuComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(api.categorias).toHaveBeenCalledTimes(1);
    expect(api.productos).toHaveBeenCalledTimes(1);
    expect(component.productosFiltrados().length).toBe(1);
    component.categoriaSeleccionada.set(2);
    expect(component.productosFiltrados().length).toBe(0);
  });

  it('crea un pedido y añade el producto seleccionado', async () => {
    await TestBed.configureTestingModule({
      imports: [MenuComponent],
      providers: [MessageService, providePrimeNG({ theme: { preset: Aura } }), { provide: RestauranteApiService, useValue: api }]
    }).compileComponents();

    const component = TestBed.createComponent(MenuComponent).componentInstance;
    const producto = { id: 7, nombre: 'Sopa', descripcion: 'Del día', precio: 12000, disponibilidad: true, categoriaId: 1, categoriaNombre: 'Platos' };
    component.agregar(producto);

    expect(api.crearPedido).toHaveBeenCalledTimes(1);
    expect(api.agregarDetalle).toHaveBeenCalledWith(41, 7, 1);
  });
});
