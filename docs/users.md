# Módulo administrativo Users

## Alcance

Este módulo permite que un usuario con rol `ADMINISTRADOR` cree y consulte cuentas, actualice sus
datos básicos, restablezca contraseñas y cambie el rol asignado. No es un mecanismo de registro
público: todos los endpoints requieren un JWT administrativo válido.

La API nunca devuelve passwords, hashes, tokens ni secretos. La contraseña recibida se transforma
con BCrypt antes de guardar el usuario.

## Endpoints

| Método | Ruta | Descripción | Rol requerido |
| --- | --- | --- | --- |
| `GET` | `/api/v1/users?page=0&size=20` | Lista usuarios paginados. | `ADMINISTRADOR` |
| `GET` | `/api/v1/users/{id}` | Consulta un usuario. | `ADMINISTRADOR` |
| `POST` | `/api/v1/users` | Crea usuario y asigna exactamente un rol. | `ADMINISTRADOR` |
| `PUT` | `/api/v1/users/{id}` | Actualiza nombre, apellido, teléfono y email. | `ADMINISTRADOR` |
| `PATCH` | `/api/v1/users/{id}/password` | Restablece administrativamente la contraseña. | `ADMINISTRADOR` |
| `PATCH` | `/api/v1/users/{id}/role` | Reemplaza los roles actuales por uno. | `ADMINISTRADOR` |

La paginación es base cero. `page` debe ser mayor o igual a 0 y `size` debe estar entre 1 y 100.

## Crear usuario

```bash
curl --request POST http://localhost:8080/api/v1/users \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{
    "firstName":"Juan",
    "lastName":"Pérez",
    "phone":"9610000000",
    "email":"juan@example.com",
    "password":"TU_PASSWORD_LOCAL",
    "role":"CLIENTE"
  }'
```

Los roles admitidos son `ADMINISTRADOR`, `TECNICO` y `CLIENTE`, y deben existir en la tabla
`roles`. El email se normaliza a minúsculas y debe ser único. Nombre, apellido, email, password y
rol son obligatorios. El password debe tener entre 8 y 200 caracteres.

Respuesta HTTP 201:

```json
{
  "status": "OK",
  "data": {
    "id": 2,
    "firstName": "Juan",
    "lastName": "Pérez",
    "phone": "9610000000",
    "email": "juan@example.com",
    "roles": ["CLIENTE"],
    "createdAt": "2026-01-01T12:00:00",
    "updatedAt": null
  }
}
```

## Listar y consultar

```bash
curl 'http://localhost:8080/api/v1/users?page=0&size=20' \
  --header 'Authorization: Bearer TOKEN'

curl http://localhost:8080/api/v1/users/2 \
  --header 'Authorization: Bearer TOKEN'
```

El listado devuelve `content`, `page`, `size`, `totalElements` y `totalPages` dentro de
`ApiResponse`.

## Actualizar datos básicos

```bash
curl --request PUT http://localhost:8080/api/v1/users/2 \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{
    "firstName":"Juan",
    "lastName":"Pérez López",
    "phone":null,
    "email":"juan@example.com"
  }'
```

Este endpoint no modifica password ni roles. El teléfono es opcional y admite hasta 20 caracteres.

## Reset administrativo de password

```bash
curl --request PATCH http://localhost:8080/api/v1/users/2/password \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{"newPassword":"NUEVO_PASSWORD_LOCAL"}'
```

Una operación correcta devuelve HTTP 204 y no incluye la contraseña ni su hash en la respuesta.

## Cambiar rol

```bash
curl --request PATCH http://localhost:8080/api/v1/users/2/role \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{"role":"TECNICO"}'
```

El cambio reemplaza todas las asignaciones existentes por exactamente un rol. Un administrador no
puede cambiar su propio rol; esto evita que el único administrador se quite accidentalmente el
acceso administrativo.

## Relación con Customers

El flujo administrativo para registrar un cliente es:

1. Crear un User con rol `CLIENTE` mediante `POST /api/v1/users`.
2. Tomar el `id` devuelto.
3. Crear el perfil mediante `POST /api/v1/customers`, enviando ese valor como `userId`.

Users no crea automáticamente perfiles Customer o Technician, manteniendo separadas las
responsabilidades y la normalización de datos.

## Errores

| Código | Motivo |
| --- | --- |
| `400 Bad Request` | Body, rol, password, id o paginación inválidos. |
| `401 Unauthorized` | Token ausente, inválido o expirado. |
| `403 Forbidden` | El token no pertenece a un `ADMINISTRADOR`. |
| `404 Not Found` | Usuario inexistente o rol configurado no encontrado. |
| `409 Conflict` | Email duplicado o intento de cambiar el rol propio. |

Los errores usan el formato común `ApiResponse` y no exponen SQL ni stack traces.

## Decisiones de seguridad

- No existe endpoint público de registro.
- No existe DELETE porque la tabla no tiene estado o desactivación lógica y `users` es referenciada
  por `user_roles`, `customers` y `technicians`.
- No se implementan recuperación de contraseña, refresh token ni logout.
- La API no crea Customer o Technician automáticamente.
- Hibernate permanece con `ddl-auto: none`.
