# PoliRestaurante

Backend REST para el sistema de pedidos de PoliRestaurante, desarrollado con Java 21, Spring Boot, Spring Data JPA y MySQL.

## Requerimientos y estado de implementación

| Requerimiento | Estado | Implementación |
|---|---|---|
| **Validación de usuario** | Implementado | La API autentica con correo y contraseña mediante HTTP Basic y restringe las operaciones según el rol (`MESERO`, `COCINA` o `ADMINISTRADOR`). |
| **Guardado de tiempos (JV)** | Implementado | Se registran las fechas de creación, inicio de preparación, pedido listo y entrega. Los reportes calculan los tiempos desde preparación hasta listo, desde listo hasta entrega y el total de preparación a entrega. |
| **Relación de producto con precio (JC)** | Implementado | Cada producto tiene precio; al agregarlo al pedido se guarda el precio unitario en el detalle y se recalcula el total del pedido. |
| **Asociación del pedido con el mesero (JV)** | Implementado | El pedido se asocia al mesero autenticado que lo crea. Las operaciones de recepción y entrega verifican que las realice ese mesero. |
| **Alertas de pedido (JV)** | Implementado | El mesero consulta visualmente sus pedidos listos en `/alertas.html`. Se muestran hasta tres avisos, con intervalos de 60 segundos, hasta confirmar recepción o entregar el pedido. |
| **Cancelación de pedido (JC)** | Implementado | El mesero responsable puede cancelar un pedido pendiente, en preparación o listo indicando un motivo. No se permite cancelar pedidos entregados o ya cancelados. |
| **Visualización de cola de pedidos (JV)** | Implementado | Cocina consulta los pedidos pendientes ordenados del más antiguo al más reciente. |
| **Cambio de estado del pedido (JC)** | Implementado | Cocina puede avanzar un pedido de pendiente a en preparación y de en preparación a listo. |
| **Gestión de menú (JC)** | Implementado | El administrador puede crear, consultar, editar y eliminar productos, precios y categorías. |
| **Gestión de mesas y usuarios (JV)** | Implementado | El administrador puede gestionar mesas y su estado, y crear, consultar, actualizar o eliminar cuentas de usuario. |
| **Reportes de ventas y tiempos (JC)** | Implementado | El administrador puede consultar ventas y tiempos de pedidos entregados para un periodo. |

## Configuración

Se necesita Java 21 y MySQL. Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`; usa [`.env.example`](.env.example) como plantilla y no compartas `.env`. `DB_URL` debe ser una URL JDBC, por ejemplo `jdbc:mysql://localhost:3306/polirestaurante`.

Al ejecutar Maven desde esta carpeta, las variables locales también se leen desde `backend/.env`. Mantén ese archivo privado y fuera de Git.

Las contraseñas nuevas se almacenan con BCrypt. Las cuentas preexistentes con contraseñas en texto plano deben migrarse a BCrypt antes de iniciar sesión.

La identidad autenticada está disponible en `GET /api/auth/me` y solo devuelve el correo y el rol. El resto de solicitudes protegidas usa HTTP Basic. Las credenciales se envían en cada petición; configura HTTPS fuera del entorno local.

El perfil `prod` deshabilita Swagger/OpenAPI. Actívalo con `SPRING_PROFILES_ACTIVE=prod`; la documentación se mantiene disponible para desarrollo.

## API

La autenticación usa HTTP Basic con correo y contraseña.

| Método | Ruta | Rol | Función |
|---|---|---|---|
| `GET` | `/api/auth/me` | Todos los roles autenticados | Consultar correo y rol sin leer datos de negocio. |
| `GET` | `/api/mesas/disponibles` | `MESERO`, `ADMINISTRADOR` | Consultar mesas libres para crear un pedido. |
| `POST` | `/api/pedidos` | `MESERO` | Crear en una transacción un pedido con mesa y uno o más detalles: `{"mesaId":1,"detalles":[{"productoId":2,"cantidad":1,"notas":""}]}`. |
| `GET` | `/api/pedidos/mis-pedidos` | `MESERO` | Consultar los pedidos activos del mesero autenticado. |
| `GET` | `/api/pedidos/cola/cocina` | `COCINA` | Consultar pendientes ordenados por fecha. |
| `PATCH` | `/api/pedidos/{id}/estado?estado=EN PREPARACIÓN` | `COCINA` | Registrar preparación o estado listo y sus tiempos. |
| `PATCH` | `/api/pedidos/{id}/entrega` | `MESERO` responsable | Registrar la entrega y su hora. |
| `GET` | `/api/pedidos/alertas` | `MESERO` | Consultar pedidos listos sin acuse. |
| `PATCH` | `/api/pedidos/{id}/recibido` | `MESERO` responsable | Confirmar una alerta y detener recordatorios. |
| `PATCH` | `/api/pedidos/{id}/cancelacion` | `MESERO` responsable | Cancelar un pedido pendiente, en preparación o listo indicando el motivo en el cuerpo: `{"motivo":"..."}`. |
| `GET` | `/api/pedidos` y `/api/pedidos/{id}` | `COCINA`, `ADMINISTRADOR` | Consultar pedidos. |
| `PUT` | `/api/pedidos/{id}` | `ADMINISTRADOR` | Actualizar las asociaciones del pedido. |
| `DELETE` | `/api/pedidos/{id}` | `ADMINISTRADOR` | Eliminar un pedido. |
| `GET` | `/api/categorias` y `/api/categorias/{id}` | `MESERO`, `COCINA`, `ADMINISTRADOR` | Consultar categorías. |
| `POST` | `/api/categorias` | `ADMINISTRADOR` | Crear categoría. |
| `PUT`, `DELETE` | `/api/categorias/{id}` | `ADMINISTRADOR` | Actualizar o eliminar categoría. |
| `GET` | `/api/productos` y `/api/productos/{id}` | `MESERO`, `COCINA`, `ADMINISTRADOR` | Consultar menú, categoría y precio. |
| `POST` | `/api/productos` | `ADMINISTRADOR` | Crear producto con categoría y precio. |
| `PUT`, `DELETE` | `/api/productos/{id}` | `ADMINISTRADOR` | Actualizar o eliminar producto. |
| `GET` | `/api/detalles-pedido` y `/api/detalles-pedido/{id}` | `MESERO`, `COCINA`, `ADMINISTRADOR` | Consultar detalles; el mesero solo accede a sus pedidos. |
| `POST` | `/api/detalles-pedido` | `MESERO` responsable | Agregar detalle y congelar precio unitario. |
| `PUT`, `DELETE` | `/api/detalles-pedido/{id}` | `MESERO` responsable, `ADMINISTRADOR` | Actualizar o eliminar detalle y recalcular total. |
| `GET` | `/api/estados-pedido` y `/api/estados-pedido/{id}` | `COCINA`, `ADMINISTRADOR` | Consultar estados. |
| `POST` | `/api/estados-pedido` | `ADMINISTRADOR` | Crear estado. |
| `PUT`, `DELETE` | `/api/estados-pedido/{id}` | `ADMINISTRADOR` | Actualizar o eliminar estado. |
| `GET` | `/api/administracion/mesas` y `/api/administracion/mesas/{id}` | `ADMINISTRADOR` | Consultar mesas. |
| `POST` | `/api/administracion/mesas` | `ADMINISTRADOR` | Crear mesa. |
| `PUT`, `DELETE` | `/api/administracion/mesas/{id}` | `ADMINISTRADOR` | Actualizar o eliminar mesa. |
| `PATCH` | `/api/administracion/mesas/{id}/estado?estado=OCUPADA` | `ADMINISTRADOR` | Cambiar una mesa entre `LIBRE` y `OCUPADA`. |
| `GET` | `/api/administracion/usuarios` | `ADMINISTRADOR` | Consultar cuentas sin exponer contraseñas. |
| `GET` | `/api/administracion/usuarios/{rol}/{id}` | `ADMINISTRADOR` | Consultar cuenta por rol e identificador. |
| `POST` | `/api/administracion/usuarios/{rol}` | `ADMINISTRADOR` | Crear cuenta `MESERO`, `COCINA` o `ADMINISTRADOR`. |
| `PUT` | `/api/administracion/usuarios/{rol}/{id}` | `ADMINISTRADOR` | Actualizar una cuenta; la contraseña es opcional. |
| `DELETE` | `/api/administracion/usuarios/{rol}/{id}` | `ADMINISTRADOR` | Eliminar una cuenta. |
| `GET` | `/api/administracion/reportes/ventas?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | `ADMINISTRADOR` | Consultar ventas, ticket promedio y productos vendidos durante el periodo indicado. |
| `GET` | `/api/administracion/reportes/tiempos?desde=AAAA-MM-DD&hasta=AAAA-MM-DD` | `ADMINISTRADOR` | Consultar promedios de preparación, espera hasta entrega y tiempo total, en segundos. |

La vista web de meseros consulta pedidos y alertas; permite confirmar la recepción, entregar o cancelar con motivo. Cocina consulta pedidos y detalles y avanza sus estados. Al entregar o cancelar un pedido se libera la mesa asociada.

El estado `CANCELADO` debe existir en la tabla `estado_pedido`. La cancelación actualiza `motivoCancelacion` y `fechaUltimaActualizacion` del pedido, sin eliminarlo ni añadir columnas.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas usan `src/test/resources/application.properties` y H2 en memoria en modo compatible con MySQL; no se conectan a la URL de `.env`. La prueba de ciclo HTTP verifica pedido, autorización por rol, persistencia, avance de cocina, entrega y liberación de mesa. OpenAPI tiene pruebas de rutas, esquemas y `x-required-roles`.
## Swagger / OpenAPI

La documentación OpenAPI se sirve desde `/v3/api-docs` y Swagger UI desde `/swagger-ui/index.html`. Usa **Authorize** para ingresar correo y contraseña mediante HTTP Basic al probar rutas protegidas. Las operaciones conservan los roles y permisos de la API.
