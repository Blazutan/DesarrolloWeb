# Matriz funcional y de cobertura

Fuentes funcionales: páginas de requisitos, historias de usuario, enunciado y CRUD del wiki. Los diagramas UML del wiki se publican como imágenes y no tienen texto extraíble desde el acceso disponible.

| Rol | Requisito / historia | Página frontend | Operación HTTP y endpoint | Permiso API | Prueba relacionada |
|---|---|---|---|---|---|
| Mesero, Cocina, Administrador | HU-01 autenticación e identidad | `/login` | `GET /api/auth/me` | Cualquier usuario autenticado | `AuthControllerTest`, `auth.service.spec.ts` |
| Mesero | HU-02 crear pedido con mesa/productos y rechazar pedido vacío | `/menu` | `GET /api/categorias`, `GET /api/productos`, `GET /api/mesas/disponibles`, `POST /api/pedidos` | Menú: los tres roles; mesas disponibles/creación: Mesero (Admin puede consultar mesas) | `PedidoFlowIntegrationTest`, `PedidoCrudRestTest`, `menu.component.spec.ts`, `restaurante-api.service.spec.ts` |
| Mesero | HU-03 consultar estado y entregar pedidos propios | `/pedidos` | `GET /api/pedidos/mis-pedidos`, `PATCH /api/pedidos/{id}/entrega` | Mesero autenticado y responsable | `PedidoFlowIntegrationTest`, `pedidos.component.spec.ts` |
| Mesero | HU-04 alertas, acuse y recordatorios | `/pedidos` | `GET /api/pedidos/alertas`, `PATCH /api/pedidos/{id}/recibido` | Mesero autenticado y responsable | `PedidoCrudRestTest`, `PedidoServiceTest`, `pedidos.component.spec.ts` |
| Mesero | HU-05 cancelar antes de entregar con motivo | `/pedidos` | `PATCH /api/pedidos/{id}/cancelacion` | Mesero autenticado y responsable | `PedidoCrudRestTest`, `PedidoServiceTest` |
| Cocina | Cancelar un pedido activo indicando motivo | `/cocina` | `PATCH /api/pedidos/{id}/cancelacion-cocina` | Cocina | `PedidoSecurityTest`, `PedidoFlowIntegrationTest`, `PedidoServiceTest`, `cocina.component.spec.ts` |
| Cocina | HU-06 cola ordenada y detalles | `/cocina` | `GET /api/pedidos/cola/cocina`, `GET /api/pedidos`, `GET /api/detalles-pedido` | Cocina | `PedidoSecurityTest`, `PedidoFlowIntegrationTest`, `cocina.component.spec.ts` |
| Cocina | HU-07 tomar y finalizar preparación | `/cocina` | `PATCH /api/pedidos/{id}/estado?estado=EN PREPARACIÓN|LISTO` | Cocina | `PedidoSecurityTest`, `PedidoFlowIntegrationTest`, `PedidoServiceTest` |
| Administrador | HU-08 categorías, productos y precios | `/administracion` | `GET/POST/PUT/DELETE /api/categorias`; `GET/POST/PUT/DELETE /api/productos` | Lectura: los tres roles; escritura: Administrador | `CategoriaRestTest`, `ProductoRestTest`, `ProductoServiceTest`, `administracion.component.spec.ts` |
| Administrador | HU-09 mesas y usuarios | `/administracion` | `GET/POST/PUT/DELETE/PATCH /api/administracion/mesas`; `GET/POST/PUT/DELETE /api/administracion/usuarios` | Administrador | `AdministracionServiceTest`, `PedidoFlowIntegrationTest`, `administracion.component.spec.ts` |
| Administrador | HU-10 ventas y tiempos por periodo, detalle de cada pedido vendido | `/administracion` | `GET /api/administracion/reportes/ventas`, `GET /api/administracion/reportes/tiempos` | Administrador | `ReporteServiceTest`, `restaurante-api.service.spec.ts`, `administracion.component.spec.ts` |
| Cocina, Administrador | Consulta/mantenimiento de estados | No se ofrece como acción de operación en frontend | `GET/POST/PUT/DELETE /api/estados-pedido` | Lectura: Cocina/Admin; escritura: Admin | `EstadoPedidoRestTest` |
| Mesero, Cocina, Administrador | Consulta/edición de detalles | La creación inicial se hace desde `/menu`; no hay edición posterior desde la UI | `GET/POST/PUT/DELETE /api/detalles-pedido` | Lectura: los tres; crear: Mesero; editar/eliminar: Mesero propietario o Admin | `DetallePedidoRestTest`, `DetallePedidoServiceTest` |
| Cocina, Administrador | Consulta y mantenimiento administrativo de pedidos | Cocina no muestra acciones administrativas; Admin API sin pantalla específica | `GET /api/pedidos`, `GET /api/pedidos/{id}`, `PUT/DELETE /api/pedidos/{id}` | GET: Cocina/Admin; mutaciones: Admin | `PedidoCrudRestTest`, `PedidoSecurityTest` |

## Contratos y configuración ajustados

- `GET /api/auth/me` devuelve solo correo y rol y reemplaza la inferencia de rol desde resultados de negocio.
- `POST /api/pedidos` requiere `mesaId` y una lista no vacía de detalles; crea el pedido, congela precios y ocupa la mesa dentro de una transacción.
- `GET /api/mesas/disponibles` y `GET /api/pedidos/mis-pedidos` exponen los datos mínimos del flujo Mesero con autorización.
- Entregar o cancelar libera la mesa; el DTO de pedido devuelve identificador/número de mesa.
- OpenAPI publica `x-required-roles`; el perfil `prod` deshabilita Swagger.
- Pruebas de backend usan H2 en memoria y no consultan `.env`.

## Cobertura pendiente / límites

- Node.js/npm y un navegador local no están instalados en el entorno detectado: no se pudo ejecutar build/tests frontend ni Playwright/E2E.
- La integración ejecutable cubre H2 compatible con MySQL; no hay una instancia MySQL de prueba disponible.
- Los diagramas UML de la wiki son imágenes sin texto extraíble desde las herramientas disponibles. No se ejecutó el script de pruebas del wiki, pues requiere una aplicación y credenciales externas.
- No existe todavía una prueba HTTP para cada combinación de éxito, validación, no autenticado, rol incorrecto y recurso inexistente de todos los CRUD administrativos; el inventario existente y las pruebas añadidas cubren solo parte de esas combinaciones.
