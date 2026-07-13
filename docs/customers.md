# Módulo Customers

## Resumen

Customers administra los perfiles de cliente definidos por la tabla `customers`. El esquema v1
mantiene los datos de identidad en `users`; por ello, este módulo recibe un `userId` existente y no
duplica nombre, teléfono, email ni credenciales.

Todos los endpoints requieren un JWT válido. `ADMINISTRADOR` tiene acceso completo y `TECNICO`
solo puede listar y consultar. `CLIENTE` no tiene acceso al CRUD administrativo en esta fase.

## Endpoints

| Método | Ruta | Descripción | Roles |
| --- | --- | --- | --- |
| `GET` | `/api/v1/customers?page=0&size=20` | Lista perfiles paginados. | `ADMINISTRADOR`, `TECNICO` |
| `GET` | `/api/v1/customers/{id}` | Consulta un perfil por id. | `ADMINISTRADOR`, `TECNICO` |
| `POST` | `/api/v1/customers` | Crea el perfil para un usuario existente. | `ADMINISTRADOR` |
| `PUT` | `/api/v1/customers/{id}` | Actualiza la dirección del perfil. | `ADMINISTRADOR` |
| `DELETE` | `/api/v1/customers/{id}` | Elimina un perfil sin vehículos asociados. | `ADMINISTRADOR` |

Los parámetros de paginación son base cero. `page` debe ser mayor o igual que 0 y `size` debe
estar entre 1 y 100.

## Modelo de respuesta

```json
{
  "id": 1,
  "userId": 2,
  "address": "Av. Principal 100",
  "registeredAt": "2026-01-01T12:00:00",
  "createdAt": "2026-01-01T12:00:00",
  "updatedAt": null
}
```

`address` es opcional porque la columna permite `NULL`; si se envía como texto debe contener al
menos un carácter distinto de espacio y no superar 255 caracteres. La asociación `userId` no se
cambia durante una actualización.

## Listar clientes

```bash
curl 'http://localhost:8080/api/v1/customers?page=0&size=20' \
  --header 'Authorization: Bearer TOKEN'
```

Respuesta HTTP 200:

```json
{
  "status": "OK",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

## Consultar cliente

```bash
curl http://localhost:8080/api/v1/customers/1 \
  --header 'Authorization: Bearer TOKEN'
```

Devuelve HTTP 200 con el perfil o HTTP 404 si no existe.

## Crear cliente

El usuario referenciado debe existir en `users` y todavía no debe tener un perfil en `customers`.

```bash
curl --request POST http://localhost:8080/api/v1/customers \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{"userId":2,"address":"Av. Principal 100"}'
```

Una creación correcta devuelve HTTP 201, el encabezado `Location` y el perfil creado dentro de
`ApiResponse`.

## Actualizar cliente

```bash
curl --request PUT http://localhost:8080/api/v1/customers/1 \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{"address":"Nueva dirección 200"}'
```

Enviar `address: null` elimina la dirección opcional. El `userId` permanece inmutable.

## Eliminar cliente

```bash
curl --request DELETE http://localhost:8080/api/v1/customers/1 \
  --header 'Authorization: Bearer TOKEN'
```

La tabla no tiene una columna de estado, por lo que no existe desactivación lógica. El endpoint
realiza borrado físico del perfil y devuelve HTTP 204. Rechaza con HTTP 409 los clientes que tengan
vehículos asociados. El registro de `users` y sus roles no se eliminan porque pertenecen al módulo
de usuarios.

## Errores esperados

| Código | Motivo |
| --- | --- |
| `400 Bad Request` | Body, id o parámetros de paginación inválidos. |
| `401 Unauthorized` | Token ausente, inválido o expirado. |
| `403 Forbidden` | El usuario autenticado no posee el rol requerido. |
| `404 Not Found` | Cliente inexistente o `userId` referenciado inexistente. |
| `409 Conflict` | El usuario ya tiene perfil Customer o el cliente conserva vehículos. |
| `500 Internal Server Error` | Error inesperado controlado sin detalles SQL ni stack trace. |

Los errores usan el formato común:

```json
{
  "status": "ERROR",
  "data": {
    "message": "Customer not found: 1",
    "timestamp": "2026-01-01T00:00:00Z"
  }
}
```

## Alcance actual

- No crea ni modifica registros de `users`.
- No administra credenciales ni roles.
- No implementa Vehicles, Vehicle Intake, Work Orders, Jobs ni Service Reports.
- No expone entidades JPA.
- Hibernate permanece con `ddl-auto: none`.
