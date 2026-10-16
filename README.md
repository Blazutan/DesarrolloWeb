# PoliRestaurante

Backend REST para el sistema de pedidos de PoliRestaurante, desarrollado con Java 21, Spring Boot, Spring Data JPA y MySQL.

## Requisitos JV implementados

1. **Guardado de tiempos:** se registran las fechas de creación, inicio de preparación, pedido listo y entrega.
2. **Asociación del pedido con el mesero:** se obtiene de la identidad autenticada, no de un identificador enviado por el cliente.
3. **Alertas de pedido:** los pedidos listos aparecen en las alertas del mesero responsable. Se generan hasta tres avisos a intervalos de 60 segundos y cesan al confirmar la recepción o entregar el pedido.
4. **Visualización de cola:** cocina consulta pedidos pendientes del más antiguo al más reciente.
5. **Gestión de mesas y usuarios:** el administrador puede cambiar estados de mesas y crear, consultar, actualizar o eliminar cuentas de usuario.

La cancelación de pedidos (requisito JC) no forma parte de esta implementación. Los cambios de estado se mantienen como mecanismo para registrar tiempos y disparar las alertas JV.

## Configuración

Se necesita Java 21 y MySQL. Configura `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. `DB_URL` debe ser una URL JDBC, por ejemplo `jdbc:mysql://localhost:3306/polirestaurante`.

Las contraseñas nuevas se almacenan con BCrypt. Las cuentas preexistentes con contraseñas en texto plano deben migrarse a BCrypt antes de iniciar sesión.

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

La vista de alertas para meseros está disponible en `/alertas.html`; consulta el API cada cinco segundos y permite confirmar la recepción.

## Pruebas

```bash
./mvnw test
```