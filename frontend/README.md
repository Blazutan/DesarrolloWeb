# PoliRestaurante cliente

Cliente Angular 20 + PrimeNG para la API Spring Boot del repositorio. La interfaz incluye el menú, filtro por categoría, creación de pedidos y consulta de alertas.

## Rutas de navegación

| Ruta | Vista |
| --- | --- |
| `/menu` | Carta, categorías y agregar productos a un pedido. |
| `/pedidos` | Alertas de pedidos listos del mesero autenticado. |

## Endpoints usados (5)

| Método | Endpoint | Uso |
| --- | --- | --- |
| `GET` | `/api/categorias` | Filtro por categoría. |
| `GET` | `/api/productos` | Cargar la carta. |
| `POST` | `/api/pedidos` | Crear pedido con la identidad del mesero. |
| `POST` | `/api/detalles-pedido` | Añadir el producto seleccionado al pedido. |
| `GET` | `/api/pedidos/alertas` | Consultar pedidos listos para recoger. |

La API usa HTTP Basic con correo y contraseña. La primera acción que lo requiere solicita las credenciales y las conserva solo en `sessionStorage` durante la pestaña abierta. El interceptor las envía únicamente a rutas `/api/`. La cuenta debe tener rol `MESERO` para crear pedidos, agregarlos y consultar sus alertas.

## Desarrollo

Requiere Node.js 20.19+ y npm, además del backend y MySQL configurados según el README raíz.

```bash
npm install
npm start
```

Angular sirve la app en `http://localhost:4200` y reenvía `/api`, `/v3/api-docs` y `/swagger-ui` al backend `http://localhost:8080`. Para cambiar el destino, edita `proxy.conf.json`.

## Pruebas unitarias

Se usa Karma/Jasmine con Chrome headless. Las pruebas de `AppComponent`, `MenuComponent` y `PedidosComponent` cubren la navegación, carga y filtro del menú, creación de pedido y presentación de alertas.

```bash
npm test
```

## Swagger / OpenAPI

Se agregó `springdoc-openapi-starter-webmvc-ui` al backend. Con Spring Boot iniciado, la documentación interactiva está en [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html) y el documento OpenAPI en [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs). Pulsa **Authorize** en Swagger UI e ingresa correo y contraseña HTTP Basic para probar rutas protegidas. Cada operación aplica los roles existentes de la API.

Los cinco endpoints usados por este cliente están indicados arriba; la documentación de Swagger genera también el resto de las rutas existentes en los controladores.
