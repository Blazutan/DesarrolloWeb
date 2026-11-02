# PoliRestaurante

Sistema de pedidos con API Spring Boot y cliente Angular + PrimeNG.

## Estructura

- `backend/`: API REST Java 21 / Spring Boot, acceso a MySQL, Swagger/OpenAPI y pruebas del servidor.
- `frontend/`: interfaz Angular, login por rol en `/login`, vistas de mesero en `/menu` y `/pedidos`, rutas protegidas `/cocina` y `/administracion`, pruebas unitarias y proxy local a la API.

## Configuración y ejecución

La conexión MySQL se configura en `backend/.env` con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Usa [backend/.env.example](backend/.env.example) como plantilla y mantén el archivo `.env` privado y fuera de Git. El backend no crea automáticamente un administrador desde variables de entorno; las cuentas deben existir en la base de datos.

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

Abre `http://localhost:4200`. El proxy de desarrollo envía `/api` al backend en `http://localhost:8080`. HTTP Basic envía credenciales en cada solicitud; usa HTTPS fuera del entorno local.

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

Las pruebas usan una base H2 aislada en modo MySQL y no se conectan a la URL configurada en `.env`.

## Swagger

Con el backend activo, abre `http://localhost:8080/swagger-ui/index.html`. Autoriza con HTTP Basic y usa una cuenta con el rol apropiado para cada operación. Detalles en [backend/README.md](backend/README.md) y [frontend/README.md](frontend/README.md).
