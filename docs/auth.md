# Módulo Auth

## Resumen

El módulo Auth gestiona el inicio de sesión con email y contraseña, la emisión de JSON Web Tokens
(JWT) y la consulta de la identidad autenticada. La API usa sesiones stateless: cada petición
protegida debe enviar su JWT mediante el encabezado `Authorization`.

Esta fase no incluye registro público, refresh token, logout ni recuperación de contraseña. Los
usuarios se crean mediante mecanismos administrativos fuera de la API pública.

## Endpoints del módulo

| Método | Ruta | Descripción | Autenticación requerida | Roles requeridos |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | Valida las credenciales y genera un JWT. | No | Ninguno |
| `GET` | `/api/v1/auth/me` | Devuelve el usuario representado por el JWT. | Sí, Bearer JWT | Cualquier usuario autenticado |

## POST /api/v1/auth/login

Endpoint público que valida el email y la contraseña contra el usuario almacenado. La contraseña
se verifica con BCrypt. Una credencial incorrecta siempre produce el mismo mensaje genérico para
no revelar si el email existe.

- Content-Type: `application/json`
- El email es obligatorio, debe tener formato válido y admite hasta 150 caracteres.
- La contraseña es obligatoria y admite hasta 200 caracteres.

### Request body

```json
{
  "email": "admin@mechsync.local",
  "password": "TU_PASSWORD_LOCAL"
}
```

### Respuesta exitosa — HTTP 200

`expiresIn` se expresa en segundos y depende de la expiración configurada para el entorno.

```json
{
  "status": "OK",
  "data": {
    "token": "JWT_TOKEN",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "email": "admin@mechsync.local",
      "roles": [
        "ADMINISTRADOR"
      ]
    }
  }
}
```

### Credenciales inválidas — HTTP 401

```json
{
  "status": "ERROR",
  "data": {
    "message": "Invalid credentials",
    "timestamp": "2026-01-01T00:00:00Z"
  }
}
```

### Body inválido — HTTP 400

Cuando falla Bean Validation, `fields` contiene los campos rechazados. Un JSON mal formado utiliza
el mensaje `Invalid request body`.

```json
{
  "status": "ERROR",
  "data": {
    "message": "Validation failed",
    "fields": {
      "email": "must not be blank",
      "password": "must not be blank"
    },
    "timestamp": "2026-01-01T00:00:00Z"
  }
}
```

### Ejemplo curl

```bash
curl --request POST http://localhost:8080/api/v1/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"email":"admin@mechsync.local","password":"TU_PASSWORD_LOCAL"}'
```

## GET /api/v1/auth/me

Devuelve la identidad incluida en un JWT válido. No vuelve a exponer las credenciales del usuario.

Encabezado requerido:

```http
Authorization: Bearer JWT_TOKEN
```

### Respuesta exitosa — HTTP 200

```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "email": "admin@mechsync.local",
    "roles": [
      "ADMINISTRADOR"
    ]
  }
}
```

### Sin token — HTTP 401

```json
{
  "status": "ERROR",
  "data": {
    "message": "Unauthorized",
    "timestamp": "2026-01-01T00:00:00Z"
  }
}
```

Un token inválido, expirado, firmado con otra clave o con claims de roles no aceptados produce la
misma respuesta HTTP 401.

### Ejemplo curl

```bash
curl http://localhost:8080/api/v1/auth/me \
  --header 'Authorization: Bearer JWT_TOKEN'
```

## Formato del JWT

El token firmado contiene únicamente la información necesaria para autenticar y autorizar:

- subject: email del usuario;
- issuer;
- fecha de emisión (`issuedAt`);
- fecha de expiración (`expiration`);
- identificador del usuario (`userId`);
- roles sin prefijo (`roles`).

El JWT no contiene la contraseña, `password_hash`, secretos ni datos sensibles innecesarios. El
payload de un JWT puede ser decodificado por el cliente; la firma garantiza integridad, no cifrado.

## Roles y authorities

Los nombres válidos están alineados con la tabla `roles`:

| Rol en base de datos y JWT | Authority en Spring Security |
| --- | --- |
| `ADMINISTRADOR` | `ROLE_ADMINISTRADOR` |
| `TECNICO` | `ROLE_TECNICO` |
| `CLIENTE` | `ROLE_CLIENTE` |

El JWT siempre transporta roles sin prefijo. El filtro JWT valida cada nombre y agrega `ROLE_`
únicamente al construir las authorities internas. Un claim como `ROLE_ADMINISTRADOR` es rechazado
para impedir authorities incorrectas como `ROLE_ROLE_ADMINISTRADOR`.

Method Security está habilitado. Los endpoints futuros podrán declarar reglas como:

```java
@PreAuthorize("hasRole('ADMINISTRADOR')")
```

## Códigos de respuesta

| Código | Significado en Auth |
| --- | --- |
| `200 OK` | Login correcto o consulta exitosa del usuario autenticado. |
| `400 Bad Request` | Body mal formado o campos que incumplen Bean Validation. |
| `401 Unauthorized` | Credenciales inválidas, token ausente, inválido o expirado. |
| `403 Forbidden` | JWT válido, pero el usuario no posee el rol requerido por el endpoint. |

Las respuestas de error mantienen el formato `ApiResponse` con `status: "ERROR"`, mensaje y
timestamp. Un rechazo por roles utiliza el mensaje `Forbidden`.

## Ejemplo de flujo completo

El siguiente ejemplo requiere `jq`. El token se conserva únicamente en una variable de la sesión
actual de la terminal.

```bash
TOKEN=$(curl --silent --request POST http://localhost:8080/api/v1/auth/login \
  --header 'Content-Type: application/json' \
  --data '{"email":"admin@mechsync.local","password":"TU_PASSWORD_LOCAL"}' \
  | jq -r '.data.token')

curl --include http://localhost:8080/api/v1/auth/me \
  --header "Authorization: Bearer $TOKEN"
```

## Variables de entorno relacionadas

| Variable | Propósito |
| --- | --- |
| `MECHSYNC_JWT_SECRET` | Clave Base64 usada para firmar y validar tokens; debe contener al menos 256 bits. |
| `MECHSYNC_JWT_EXPIRATION_MINUTES` | Vigencia del token expresada en minutos. |
| `MECHSYNC_JWT_ISSUER` | Emisor requerido al generar y validar el JWT. |

Los valores reales pertenecen al `.env` local y no deben versionarse ni incluirse en documentación.

## Estado actual de seguridad

- `GET /api/v1/health` es público.
- `GET /api/v1/health/database` es público.
- `POST /api/v1/auth/login` es público.
- `GET /api/v1/auth/me` requiere un JWT válido.
- Cualquier otra ruta queda autenticada por defecto.
- La aplicación no mantiene sesión HTTP y utiliza `SessionCreationPolicy.STATELESS`.
- Las respuestas HTTP 401 y 403 son JSON controlado.
- Las respuestas no exponen stack traces ni información de conexión.

## Pendientes fuera de esta fase

No están implementados:

- registro público;
- refresh token;
- logout;
- recuperación de contraseña;
- CRUD de usuarios;
- gestión avanzada de permisos.

La autorización actual se basa únicamente en los roles existentes y sirve como base para proteger
los futuros CRUD con `@PreAuthorize`.
