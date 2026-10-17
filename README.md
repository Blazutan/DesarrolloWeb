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
| **Cancelación de pedido (JC)** | Pendiente | No hay una operación para cancelar pedidos ni para registrar y consultar el motivo. El campo `motivoCancelacion` del modelo, por sí solo, no implementa el requerimiento. La eliminación administrativa de un pedido tampoco equivale a cancelarlo. |
| **Visualización de cola de pedidos (JV)** | Implementado | Cocina consulta los pedidos pendientes ordenados del más antiguo al más reciente. |
| **Cambio de estado del pedido (JC)** | Implementado | Cocina puede avanzar un pedido de pendiente a en preparación y de en preparación a listo. |
| **Gestión de menú (JC)** | Implementado | El administrador puede crear, consultar, editar y eliminar productos, precios y categorías. |
| **Gestión de mesas y usuarios (JV)** | Implementado | El administrador puede gestionar mesas y su estado, y crear, consultar, actualizar o eliminar cuentas de usuario. |
| **Reportes de ventas y tiempos (JC)** | Implementado | El administrador puede consultar ventas y tiempos de pedidos entregados para un periodo. |

## Configuración

Se necesita Java 21 y MySQL. Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. `DB_URL` debe ser una URL JDBC, por ejemplo `jdbc:mysql://localhost:3306/polirestaurante`.

Las contraseñas nuevas se almacenan con BCrypt. Las cuentas preexistentes con contraseñas en texto plano deben migrarse a BCrypt antes de iniciar sesión.

La autenticación no tiene una ruta de inicio de sesión independiente: envía el correo y la contraseña mediante HTTP Basic en cada solicitud protegida.

## API

La autenticación usa HTTP Basic con correo y contraseña.

| Método | Ruta | Rol | Función |
|---|---|---|---|
| `POST` | `/api/pedidos` | `MESERO` | Crear un pedido ligado al mesero autenticado. |
| `GET` | `/api/pedidos/cola/cocina` | `COCINA` | Consultar pendientes ordenados por fecha. |
| `PATCH` | `/api/pedidos/{id}/estado?estado=EN PREPARACIÓN` | `COCINA` | Registrar preparación o estado listo y sus tiempos. |
| `PATCH` | `/api/pedidos/{id}/entrega` | `MESERO` responsable | Registrar la entrega y su hora. |
| `GET` | `/api/pedidos/alertas` | `MESERO` | Consultar pedidos listos sin acuse. |
| `PATCH` | `/api/pedidos/{id}/recibido` | `MESERO` responsable | Confirmar una alerta y detener recordatorios. |
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

La vista de alertas para meseros está disponible en `/alertas.html`; consulta el API cada cinco segundos y permite confirmar la recepción.

## Pruebas

```bash
./mvnw test
```