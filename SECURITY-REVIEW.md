# Revisión de seguridad

Fecha: 2026-11-02

## Alcance

Revisión estática de autenticación y almacenamiento en el frontend, y comprobación de las páginas públicas del wiki enlazadas desde el repositorio. No equivale a una auditoría exhaustiva ni confirma si las credenciales publicadas siguen activas.

## Hallazgos

| # | Severity | File | Lines | Vulnerability | Confidence |
|---|----------|------|-------|---------------|------------|
| 1 | 🟡 MEDIUM | `frontend/src/app/core/auth.service.ts` | 51, 69-74 | La contraseña reutilizable se guarda en `sessionStorage` y puede ser leída por scripts ejecutados en el mismo origen. | 8/10 |
| 2 | 🟠 HIGH | Wiki público: `Diagrama-Relacional` y `CRUD-PRODUCTO` | N/A (contenido de wiki) | Las páginas incluyen valores con formato de credenciales de base de datos y cuentas. La vigencia no se verificó. | 9/10 |

### 1. Contraseña persistida en el navegador

**Impacto:** un XSS u otro script ejecutado en el origen podría leer la contraseña de sesión y reutilizarla para autenticarse.

**Corrección recomendada:** reemplazar HTTP Basic con una sesión de servidor mediante cookie `HttpOnly`, `Secure` y `SameSite`; como mitigación temporal, mantener las credenciales solo en memoria y exigir nuevo inicio de sesión tras recargar la página.

**Estado:** pendiente de corrección.

### 2. Credenciales expuestas en el wiki público

**Impacto:** si alguno de los valores publicados continúa activo, un tercero podría acceder a la base de datos o a cuentas de la aplicación.

**Corrección recomendada:** revocar y rotar inmediatamente las credenciales afectadas; sustituir los valores del wiki por marcadores ficticios y revisar el historial público. No se copiaron ni reproducen los valores en este informe.

**Estado:** pendiente de revocación/rotación y saneamiento del wiki. El acceso disponible permitió leer las páginas, pero no editar el wiki ni comprobar la validez de las credenciales.

## Resultado de verificación

La revisión fue de solo lectura. No se ejecutó una prueba de explotación ni se confirmó la vigencia de las credenciales externas. La inspección no permite afirmar que el sistema esté libre de vulnerabilidades.
