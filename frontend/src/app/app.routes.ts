import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { MenuComponent } from './pages/menu/menu.component';
import { PedidosComponent } from './pages/pedidos/pedidos.component';
import { CocinaComponent } from './pages/cocina/cocina.component';
import { AdministracionComponent } from './pages/administracion/administracion.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'menu' },
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión | PoliRestaurante' },
  { path: 'menu', component: MenuComponent, canActivate: [authGuard, roleGuard('MESERO')], title: 'Menú | PoliRestaurante' },
  { path: 'pedidos', component: PedidosComponent, canActivate: [authGuard, roleGuard('MESERO')], title: 'Pedidos | PoliRestaurante' },
  { path: 'cocina', component: CocinaComponent, canActivate: [authGuard, roleGuard('COCINA')], title: 'Cocina | PoliRestaurante' },
  { path: 'administracion', component: AdministracionComponent, canActivate: [authGuard, roleGuard('ADMINISTRADOR')], title: 'Administración | PoliRestaurante' },
  { path: '**', redirectTo: 'menu' }
];
