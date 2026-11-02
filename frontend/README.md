# PoliRestaurante cliente

Cliente Angular 20 + PrimeNG para la API Spring Boot. El mesero cuenta con menú, filtro por categoría, creación de pedidos y consulta de alertas. El acceso reconoce los roles Mesero, Cocina y Administrador.

## Rutas de navegación

| Ruta | Vista |
| --- | --- |
| `/login` | Inicio de sesión para los tres roles. |
| `/menu` | Carta, categorías y creación de pedidos (solo mesero). |
| `/pedidos` | Alertas de pedidos listos (solo mesero). |
| `/cocina` | Ruta protegida de cocina; panel funcional pendiente. |
| `/administracion` | Ruta protegida de administración; panel funcional pendiente. |

## Endpoints usados

| Método | Endpoint | Uso |
| --- | --- | --- |
| `GET` | `/api/categorias` | Cargar categorías y validar credenciales para todos los roles. |
| `GET` | `/api/productos` | Cargar la carta. |
| `POST` | `/api/pedidos` | Crear pedido con la identidad del mesero. |
| `POST` | `/api/detalles-pedido` | Añadir producto al pedido. |
| `GET` | `/api/pedidos/alertas` | Alertas de pedidos listos y detección de rol Mesero. |
| `GET` | `/api/pedidos/cola/cocina` | Detección de rol Cocina. |
| `GET` | `/api/administracion/mesas` | Detección de rol Administrador. |

El login valida credenciales con `GET /api/categorias` y consulta las rutas exclusivas de alertas (MESERO), cola (COCINA) y mesas (ADMINISTRADOR). La API no expone un endpoint de usuario actual/rol, por lo que el cliente infiere el perfil por la respuesta HTTP de esas rutas protegidas. Las rutas de menú y pedidos están limitadas al mesero; `/cocina` y `/administracion` ya aplican su guard de rol, pero sus páginas funcionales aún deben construirse. Las credenciales se conservan en `sessionStorage` y el interceptor envía HTTP Basic únicamente a rutas `/api/`.

## Desarrollo

Requiere Node.js 20.19+ y npm, además del backend y MySQL configurados según el README raíz.

```bash
npm install
npm start
```

Angular sirve la app en `http://localhost:4200` y reenvía `/api`, `/v3/api-docs` y `/swagger-ui` al backend `http://localhost:8080`. Para cambiar el destino, edita `proxy.conf.json`.

## Pruebas unitarias

Se usa Karma/Jasmine con Chrome headless. Las pruebas de `AppComponent`, `LoginComponent`, `AuthService`, `AuthGuard`, `MenuComponent` y `PedidosComponent` cubren el formulario, detección de rol, navegación protegida, menú, creación de pedido y presentación de alertas.

```bash
npm test
```

## Swagger / OpenAPI

Con Spring Boot iniciado, la documentación interactiva está en [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) y el documento OpenAPI en [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs). Pulsa **Authorize** en Swagger UI e ingresa correo y contraseña HTTP Basic para probar rutas protegidas. Cada operación aplica los roles existentes de la API.
