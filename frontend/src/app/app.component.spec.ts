import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { AppComponent } from './app.component';
import { routes } from './app.routes';

describe('AppComponent', () => {
  it('renderiza navegación a menú y pedidos', async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter(routes), providePrimeNG({ theme: { preset: Aura } })]
    }).compileComponents();

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();

    const links = Array.from(fixture.nativeElement.querySelectorAll('nav a')) as HTMLAnchorElement[];
    expect(links.map(link => link.textContent?.trim())).toEqual(['Menú', 'Mis pedidos']);
    expect(links.map(link => link.getAttribute('href'))).toEqual(['/menu', '/pedidos']);
  });
});
