import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-role-home',
  standalone: true,
  template: `
    <section class="role-home">
      <p class="eyebrow">POLIRESTAURANTE</p>
      <h1>{{ title }}</h1>
      <p>Sesión iniciada con el perfil {{ roleLabel }}.</p>
      <p class="note">Esta vista ya está protegida por rol. Las funciones completas de este perfil aún están pendientes en el frontend.</p>
    </section>
  `,
  styles: [`
    .role-home { max-width: 760px; margin: 4rem auto; padding: 2rem; background: #fff; border: 1px solid #e8e7e2; border-radius: 1rem; }
    .eyebrow { color: #9a6b36; font-size: .75rem; font-weight: 700; letter-spacing: .14em; }
    h1 { margin: .5rem 0; color: #29261f; }
    .note { margin-top: 2rem; padding: 1rem; background: #f7f5f0; border-radius: .5rem; color: #625d52; }
  `]
})
export class RoleHomeComponent {
  private readonly auth = inject(AuthService);
  readonly role = this.auth.role();
  readonly title = this.role === 'COCINA' ? 'Panel de cocina' : 'Administración';
  readonly roleLabel = this.role === 'COCINA' ? 'cocina' : 'administrador';
}
