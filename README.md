# PoliRestaurante

Backend REST para la gestión del ciclo de pedidos de PoliRestaurante, desarrollado con Java 21, Spring Boot, Spring Data JPA y MySQL.

## Requisitos implementados

Este proyecto cubre cinco requisitos funcionales de la [wiki del proyecto](https://github.com/Blazutan/DesarrolloWeb/wiki):

1. **Asociación del pedido con el mesero:** al crear un pedido se exige el identificador de un mesero existente; el pedido inicia en `PENDIENTE`.
2. **Visualización de la cola de cocina:** los pedidos pendientes se consultan desde el más antiguo al más reciente.
3. **Cambio de estado del pedido:** cocina puede avanzar `PENDIENTE` → `EN PREPARACIÓN` → `LISTO`; el mesero puede avanzar `LISTO` → `ENTREGADO`. No se permiten saltos de estado.
4. **Cancelación de pedido:** se guarda el motivo de cancelación y no se permite cancelar pedidos entregados o ya cancelados.
5. **Guardado de tiempos:** se persisten las fechas de creación, inicio de preparación, pedido listo, entrega y cancelación para registrar cada cambio de estado y calcular la duración de cada etapa.

## Configuración

Se necesita Java 21 y una base de datos MySQL. Configura estas variables de entorno antes de ejecutar la aplicación:

| Variable | Descripción |
|---|---|
| `DB_URL` | URL JDBC de la base de datos, por ejemplo `jdbc:mysql://localhost:3306/polirestaurante` |
| `DB_USERNAME` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |

La base de datos debe contener los estados `PENDIENTE`, `EN PREPARACIÓN`, `LISTO`, `ENTREGADO` y `CANCELADO` en `estado_pedido`. Hibernate actualiza el esquema al iniciar.

## API de pedidos

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/pedidos?meseroId={id}` | Crea un pedido asociado al mesero indicado. |
| `GET` | `/api/pedidos/cola/cocina` | Lista los pedidos pendientes ordenados por creación. |
| `PATCH` | `/api/pedidos/{id}/estado?estado={estado}` | Avanza el estado del pedido y registra la fecha correspondiente. |
| `PATCH` | `/api/pedidos/{id}/cancelacion?motivo={motivo}` | Cancela un pedido no entregado y guarda el motivo. |

Los cambios de estado aceptados son `EN PREPARACIÓN`, `LISTO` y `ENTREGADO`, en ese orden. La cancelación requiere un motivo no vacío.

## Pruebas

Ejecuta las pruebas unitarias con:

```bash
./mvnw test
```