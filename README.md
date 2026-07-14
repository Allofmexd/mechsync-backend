# MechSync Backend

API REST de MechSync construida con Java 17 y Spring Boot.

## Requisitos locales

- JDK 17 o superior para ejecutar bytecode Java 17.
- Maven 3.8 o superior.
- MySQL con el esquema v1 de MechSync ya instalado.

## Levantar API localmente

La aplicación recibe la conexión mediante variables de entorno cargadas desde un archivo `.env`
local. Spring Boot no lee ese archivo directamente: `scripts/run-local.sh` lo carga antes de
iniciar Maven.

1. Crea el archivo local si todavía no existe:

```bash
cp .env.example .env
```

2. Edita `.env`:

```bash
nano .env
```

Reemplaza `NOMBRE_DB` y `CAMBIA_ESTA_PASSWORD`. Los scripts de `mechsync-database/v1` reciben el
nombre mediante `DB_NAME`; no definen un nombre fijo. Configura como mínimo:

- `MECHSYNC_DB_URL`
- `MECHSYNC_DB_USERNAME`
- `MECHSYNC_DB_PASSWORD`
- `MECHSYNC_JWT_SECRET`
- `MECHSYNC_JWT_EXPIRATION_MINUTES`
- `MECHSYNC_JWT_ISSUER`
- `MECHSYNC_CORS_ALLOWED_ORIGINS`

3. Levanta la API desde cualquier ruta:

```bash
./scripts/run-local.sh
```

4. En otra terminal ejecuta los health checks:

```bash
./scripts/check-health.sh
```

El script consulta `/api/v1/health` y `/api/v1/health/database`, mostrando el código HTTP y la
respuesta de cada endpoint. Usa `SERVER_PORT` desde `.env` o `8080` como valor predeterminado.

`.env` contiene configuración local y está excluido mediante `.gitignore`; nunca debe subirse a
Git. `.env.example` solo contiene placeholders y sí puede versionarse.

## Contraseñas locales

El mensaje `[sudo] password for mz:` solicita la contraseña del usuario Linux `mz`. Esa contraseña
no es necesariamente la contraseña de MySQL y no debe guardarse en `.env`.

`MECHSYNC_DB_PASSWORD` debe contener la contraseña del usuario MySQL de aplicación, por ejemplo
`mechsync_app`. Si no se conoce, debe revisarse o recrearse mediante acceso administrativo local.
La aplicación nunca debe conectarse como `root`.

`ddl-auto` está fijado en `none` y la inicialización SQL de Spring está desactivada. La aplicación
no crea ni modifica el esquema.

## CORS para el frontend local

Los orígenes permitidos se configuran como una lista separada por comas. El valor de desarrollo
predeterminado permite las dos direcciones habituales de Vite:

```dotenv
MECHSYNC_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://127.0.0.1:5173
```

La configuración acepta los métodos REST utilizados por la API y el header `Authorization` para
JWT Bearer, sin habilitar credenciales basadas en cookies. Los orígenes deben ser explícitos: el
backend rechaza una configuración que contenga `*`. En cada ambiente configura únicamente los
orígenes reales desde los que se servirá el frontend.

## Health checks

```bash
curl --fail-with-body http://localhost:8080/api/v1/health
curl --fail-with-body http://localhost:8080/api/v1/health/database
```

El health check general responde HTTP 200 cuando la API está activa. El health check de base de
datos ejecuta `SELECT 1`: responde HTTP 200 y `UP` cuando MySQL está disponible, o HTTP 503 y
`DOWN` cuando no puede establecer la conexión. Ninguna respuesta expone datos de conexión.

## Auth local

JWT requiere un secreto Base64 de al menos 32 bytes. El archivo `.env.example` contiene un
placeholder; genera un valor local y guárdalo únicamente en `.env`, nunca en Git. También puedes
ajustar la expiración y el issuer mediante:

- `MECHSYNC_JWT_SECRET`
- `MECHSYNC_JWT_EXPIRATION_MINUTES`
- `MECHSYNC_JWT_ISSUER`

La base v1 no crea usuarios automáticamente. Para crear o actualizar un administrador local con
contraseña BCrypt y asignarle el rol `ADMINISTRADOR`, ejecuta el script opcional:

```bash
cd /home/mz/Desktop/mech-sync/mechsync-database/v1
./scripts/create_local_admin_user.sh
```

El script solicita la contraseña de forma silenciosa, no la guarda en texto plano y no forma parte
de migraciones ni seeds automáticos.

Login por email:

```bash
curl --request POST http://localhost:8080/api/v1/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"email":"admin@example.com","password":"TU_PASSWORD_LOCAL"}'
```

Consulta del usuario autenticado:

```bash
curl http://localhost:8080/api/v1/auth/me \
  --header 'Authorization: Bearer TOKEN'
```

No existe registro público, refresh token, logout ni recuperación de contraseña en esta fase.

## Autorización por roles

Los roles actuales son `ADMINISTRADOR`, `TECNICO` y `CLIENTE`, alineados con la tabla `roles`.
El JWT conserva esos nombres limpios en el claim `roles`; el filtro de seguridad los convierte a
las authorities `ROLE_ADMINISTRADOR`, `ROLE_TECNICO` y `ROLE_CLIENTE` de Spring Security. El token
no contiene el hash de contraseña.

Las rutas públicas actuales son los dos health checks y `POST /api/v1/auth/login`.
`GET /api/v1/auth/me` y cualquier otra ruta requieren autenticación. Los controllers futuros
pueden habilitar reglas específicas con method security, por ejemplo
`@PreAuthorize("hasRole('ADMINISTRADOR')")`.

```bash
curl http://localhost:8080/api/v1/auth/me \
  --header 'Authorization: Bearer TOKEN'
```

Una petición sin autenticación válida recibe HTTP 401. Un usuario autenticado que no posea el rol
requerido recibe HTTP 403 con una respuesta JSON controlada.

## Validación

```bash
mvn clean test
mvn clean package
```
