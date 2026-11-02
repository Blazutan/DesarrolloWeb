# PoliRestaurante

Sistema de pedidos con API Spring Boot y cliente Angular + PrimeNG.

## Estructura

- `backend/`: API REST Java 21 / Spring Boot, acceso a MySQL, Swagger/OpenAPI y pruebas del servidor.
- `frontend/`: interfaz Angular, rutas `/menu` y `/pedidos`, pruebas unitarias y proxy local a la API.

## Configuración y ejecución

La conexión MySQL se configura en `backend/.env` con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Mantén ese archivo privado y fuera de Git. El backend no crea automáticamente un administrador desde las variables `ADMIN_*`; las cuentas deben existir en la base de datos. Para usar el flujo del cliente se necesita una cuenta con rol `MESERO`.

Terminal 1, API:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Terminal 2, cliente:

```powershell
cd frontend
npm install
npm start
```

Abre `http://localhost:4200`. El proxy de desarrollo envía `/api` al backend en `http://localhost:8080`.

## Pruebas

```powershell
cd frontend
npm test
npm run build
```

```powershell
cd backend
.\mvnw.cmd test
```

## Swagger

Con el backend activo, abre `http://localhost:8080/swagger-ui/index.html`. Autoriza con HTTP Basic y usa una cuenta con el rol apropiado para cada operación. Detalles en [backend/README.md](backend/README.md) y [frontend/README.md](frontend/README.md).
