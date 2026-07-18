# CORS para frontend Vercel

## Objetivo

MechSync mantiene la API bajo `/api/v1` y el frontend productivo bajo
`https://mechsync-frontend.vercel.app`. Vercel reescribe las solicitudes del navegador desde
`/api/v1/**` hacia la API HTTP desplegada. Esto evita Mixed Content, pero el proxy conserva el
encabezado `Origin`; por tanto, Spring debe aceptar explícitamente el origen del frontend.

## Orígenes permitidos

La configuración por defecto permite únicamente:

- `http://localhost:5173`;
- `http://127.0.0.1:5173`;
- `https://mechsync-frontend.vercel.app`.

La variable `MECHSYNC_CORS_ALLOWED_ORIGINS` puede reemplazar esta lista mediante valores separados
por comas. Si se configura en el entorno de despliegue, debe conservar el origen productivo de
Vercel; de lo contrario, Spring responderá `403 Invalid CORS request` antes de ejecutar el
controlador.

No se habilitó `https://*.vercel.app`. Ese patrón facilitaría previews, pero permitiría cualquier
subdominio de Vercel y amplía innecesariamente la superficie aceptada. Cada preview que requiera
acceso real debe agregarse explícitamente o habilitarse de forma temporal y controlada.

## Política aplicada

- Métodos: `GET`, `POST`, `PUT`, `PATCH`, `DELETE` y `OPTIONS`.
- Headers permitidos: `Authorization`, `Content-Type`, `Accept`, `Origin` y `X-Requested-With`.
- Headers expuestos: `Authorization` y `Content-Type`.
- `allowCredentials` permanece en `false` porque la API usa JWT en `Authorization: Bearer` y no
  cookies de sesión.
- CSRF permanece deshabilitado para la API stateless.
- `POST /api/v1/auth/login`, `GET /api/v1/health`, `GET /api/v1/health/database` y
  `OPTIONS /api/v1/**` son públicos.
- El filtro JWT omite `OPTIONS` y login. En las demás rutas solo valida cuando recibe un header
  `Authorization` con prefijo `Bearer `.

## Validación desde PowerShell

### Preflight directo contra EC2

```powershell
curl.exe -i -X OPTIONS "http://3.212.179.142:8080/api/v1/auth/login" `
  -H "Origin: https://mechsync-frontend.vercel.app" `
  -H "Access-Control-Request-Method: POST" `
  -H "Access-Control-Request-Headers: content-type, authorization"
```

Debe responder `200` o `204` e incluir:

```text
Access-Control-Allow-Origin: https://mechsync-frontend.vercel.app
```

### Preflight mediante proxy Vercel

```powershell
curl.exe -i -X OPTIONS "https://mechsync-frontend.vercel.app/api/v1/auth/login" `
  -H "Origin: https://mechsync-frontend.vercel.app" `
  -H "Access-Control-Request-Method: POST" `
  -H "Access-Control-Request-Headers: content-type, authorization"
```

### Login con credenciales deliberadamente inválidas

```powershell
$body = @{
  email = "adminpruebasmechsync@gmail.com"
  password = "PASSWORD_INCORRECTA"
} | ConvertTo-Json

curl.exe -i -X POST "https://mechsync-frontend.vercel.app/api/v1/auth/login" `
  -H "Origin: https://mechsync-frontend.vercel.app" `
  -H "Content-Type: application/json" `
  --data $body
```

Debe responder `401 Unauthorized`, no `403 Forbidden` por CORS.

### Health

```powershell
curl.exe -i "https://mechsync-frontend.vercel.app/api/v1/health"
```

Debe responder `200 OK`.

## Comportamiento de seguridad esperado

- Credenciales válidas en login: `200 OK` y JWT.
- Credenciales inválidas en login: `401 Unauthorized`.
- Ruta protegida sin JWT: `401 Unauthorized`.
- JWT válido sin el rol requerido: `403 Forbidden`.
- Origen no permitido: preflight rechazado o respuesta sin headers CORS utilizables por el
  navegador.

El cambio requiere desplegar nuevamente el backend. Modificar únicamente Vercel o el frontend no
actualiza la configuración CORS que ejecuta Spring en EC2.
