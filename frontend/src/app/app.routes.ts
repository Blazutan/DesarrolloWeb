import { Routes } from '@angular/router';
import { MenuComponent } from './pages/menu/menu.component';
import { PedidosComponent } from './pages/pedidos/pedidos.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'menu' },
  { path: 'menu', component: MenuComponent, title: 'Menú | PoliRestaurante' },
  { path: 'pedidos', component: PedidosComponent, title: 'Pedidos | PoliRestaurante' },
  { path: '**', redirectTo: 'menu' }
];
