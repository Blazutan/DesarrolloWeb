import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { map, Observable, tap } from 'rxjs';

export type AppRole = 'MESERO' | 'COCINA' | 'ADMINISTRADOR';

export interface AuthCredentials {
  usuario: string;
  clave: string;
}

const STORAGE_KEY = 'poli-auth';
interface StoredAuth extends AuthCredentials { rol: AppRole; }

export function encodeBasicCredentials(credentials: AuthCredentials): string {
  const bytes = new TextEncoder().encode(`${credentials.usuario}:${credentials.clave}`);
  let binary = '';
  bytes.forEach(byte => binary += String.fromCharCode(byte));
  return `Basic ${btoa(binary)}`;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly credentialsState = signal<StoredAuth | null>(this.readCredentials());
  readonly isAuthenticated = computed(() => this.credentialsState() !== null);
  readonly role = computed(() => this.credentialsState()?.rol ?? null);

  login(credentials: AuthCredentials): Observable<AppRole> {
    const headers = new HttpHeaders({ Authorization: encodeBasicCredentials(credentials) });
    return this.http.get<{ correo: string; rol: string }>('/api/auth/me', { headers }).pipe(
      map(profile => {
        if (!['MESERO', 'COCINA', 'ADMINISTRADOR'].includes(profile.rol)) {
          throw new HttpErrorResponse({ status: 403, statusText: 'Forbidden', error: 'Rol no reconocido' });
        }
        return profile.rol as AppRole;
      }),
      tap(rol => {
        const auth = { ...credentials, rol };
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(auth));
        this.credentialsState.set(auth);
      })
    );
  }

  logout(): void {
    sessionStorage.removeItem(STORAGE_KEY);
    this.credentialsState.set(null);
  }

  authorizationHeader(): string | null {
    const credentials = this.credentialsState();
    return credentials ? encodeBasicCredentials(credentials) : null;
  }

  private readCredentials(): StoredAuth | null {
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const value = JSON.parse(raw) as Partial<StoredAuth>;
      return typeof value.usuario === 'string' && typeof value.clave === 'string'
        && ['MESERO', 'COCINA', 'ADMINISTRADOR'].includes(value.rol ?? '')
        ? { usuario: value.usuario, clave: value.clave, rol: value.rol as AppRole }
        : null;
    } catch {
      sessionStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }
}
