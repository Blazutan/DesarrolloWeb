# PoliRestaurante cliente

Cliente Angular 20 + PrimeNG para la API Spring Boot. Incluye los módulos funcionales Mesero, Cocina y Administración.

## Rutas

| Ruta | Funcionalidad | Permiso |
|---|---|---|
| `/login` | Acceso del personal e identificación de rol con `GET /api/auth/me`. | Público |
| `/menu` | Menú, selección de mesa libre, carrito y creación atómica del pedido. | Mesero |
| `/pedidos` | Pedidos propios, alertas, acuse, entrega y cancelación con motivo. | Mesero |
| `/cocina` | Cola con productos y notas, toma de pedido y transición a listo. | Cocina |
| `/administracion` | CRUD de categorías, productos, mesas y cuentas; reportes de ventas y tiempos. | Administrador |

Las rutas usan guards por autenticación y rol. Las peticiones protegidas envían HTTP Basic solo a `/api/`. El backend identifica el perfil por `GET /api/auth/me`; el cliente no deduce el rol consultando operaciones de negocio.

## Contratos de la API usados

| Método | Endpoint | Uso |
|---|---|---|
| `GET` | `/api/auth/me` | Validar credenciales y consultar correo/rol. |
| `GET` | `/api/categorias`, `/api/productos` | Cargar carta y filtros. |
| `GET` | `/api/mesas/disponibles` | Cargar mesas libres. |
| `POST` | `/api/pedidos` | Crear pedido con `mesaId` y lista no vacía de detalles. |
| `GET` | `/api/pedidos/mis-pedidos`, `/api/pedidos/alertas` | Pedidos activos y alertas propios. |
| `PATCH` | `/api/pedidos/{id}/recibido`, `/entrega`, `/cancelacion` | Acusar, entregar o cancelar pedidos propios. |
| `GET` | `/api/pedidos`, `/api/detalles-pedido` | Cargar pedidos y detalles de cocina. |
| `PATCH` | `/api/pedidos/{id}/estado?estado=...` | Avanzar preparación y marcar listo. |
| `GET/POST/PUT/DELETE/PATCH` | `/api/administracion/**`, `/api/categorias/**`, `/api/productos/**` | Operaciones autorizadas de administración. |

## Desarrollo

Requiere Node.js 20.19+ y npm, además del backend y MySQL configurados según el README raíz.

```powershell
npm install
npm start
```

Angular sirve la app en `http://localhost:4200` y reenvía `/api`, `/v3/api-docs` y `/swagger-ui` al backend `http://localhost:8080`. Para cambiar el destino, edita `proxy.conf.json`.

## Pruebas unitarias

Karma/Jasmine con Chrome headless. Hay pruebas de autenticación, guards, interceptor, servicios HTTP y páginas de Mesero, Cocina y Administración.

```powershell
npm test
npm run build
```

Swagger/OpenAPI está en `http://localhost:8080/swagger-ui/index.html` durante desarrollo. Usa **Authorize** y credenciales de una cuenta de prueba.
