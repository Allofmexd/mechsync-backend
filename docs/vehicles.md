# Módulo Vehicles

## Alcance

Vehicles administra los vehículos asociados a perfiles Customer existentes. Es la etapa posterior
a Users y Customers y será la referencia para Vehicle Intake en una fase futura.

`ADMINISTRADOR` puede realizar todas las operaciones. `TECNICO` solo puede listar y consultar.
`CLIENTE` todavía no puede consultar sus propios vehículos porque falta diseñar explícitamente la
resolución segura JWT userId → Customer → Vehicles.

## Endpoints

| Método | Ruta | Descripción | Roles |
| --- | --- | --- | --- |
| `GET` | `/api/v1/vehicles?page=0&size=20` | Lista vehículos paginados. | `ADMINISTRADOR`, `TECNICO` |
| `GET` | `/api/v1/vehicles/{id}` | Consulta un vehículo. | `ADMINISTRADOR`, `TECNICO` |
| `POST` | `/api/v1/vehicles` | Crea un vehículo para un Customer existente. | `ADMINISTRADOR` |
| `PUT` | `/api/v1/vehicles/{id}` | Actualiza sus datos principales. | `ADMINISTRADOR` |
| `DELETE` | `/api/v1/vehicles/{id}` | Elimina un vehículo sin intakes asociados. | `ADMINISTRADOR` |

La paginación es base cero. `page` debe ser mayor o igual que 0 y `size` debe estar entre 1 y 100.

## Modelo

Los campos corresponden directamente al esquema v1:

- `customerId`: Customer propietario;
- `brand` y `model`;
- `year`;
- `color`, opcional;
- `licensePlate`, única;
- `vin`, opcional y único cuando existe;
- `currentMileage`, opcional y no negativo;
- timestamps de creación y actualización.

No existe una columna `transmissionType` en la tabla y por eso no forma parte del contrato.

## Crear vehículo

```bash
curl --request POST http://localhost:8080/api/v1/vehicles \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{
    "customerId":1,
    "brand":"Nissan",
    "model":"Sentra",
    "year":2005,
    "color":"Azul",
    "licensePlate":"ABC123",
    "vin":"VIN_DE_PRUEBA",
    "currentMileage":120000
  }'
```

La creación devuelve HTTP 201, el encabezado `Location` y el vehículo dentro de `ApiResponse`.
`customerId` debe existir. Placa y VIN se normalizan a mayúsculas.

## Respuesta

```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "customerId": 1,
    "brand": "Nissan",
    "model": "Sentra",
    "year": 2005,
    "color": "Azul",
    "licensePlate": "ABC123",
    "vin": "VIN_DE_PRUEBA",
    "currentMileage": 120000,
    "createdAt": "2026-01-01T12:00:00",
    "updatedAt": null
  }
}
```

## Listar y consultar

```bash
curl 'http://localhost:8080/api/v1/vehicles?page=0&size=20' \
  --header 'Authorization: Bearer TOKEN'

curl http://localhost:8080/api/v1/vehicles/1 \
  --header 'Authorization: Bearer TOKEN'
```

## Actualizar

```bash
curl --request PUT http://localhost:8080/api/v1/vehicles/1 \
  --header 'Authorization: Bearer TOKEN' \
  --header 'Content-Type: application/json' \
  --data '{
    "brand":"Nissan",
    "model":"Sentra",
    "year":2005,
    "color":"Negro",
    "licensePlate":"ABC123",
    "vin":"VIN_DE_PRUEBA",
    "currentMileage":125000
  }'
```

El `customerId` permanece inmutable durante PUT para evitar transferencias accidentales entre
clientes.

## Eliminar

```bash
curl --request DELETE http://localhost:8080/api/v1/vehicles/1 \
  --header 'Authorization: Bearer TOKEN'
```

La tabla no tiene estado para soft delete. Se permite borrado físico y se devuelve HTTP 204 solo
cuando el vehículo no tiene registros en `vehicle_intakes`. Si existen dependencias, responde
HTTP 409. Customer y User nunca se eliminan desde este módulo.

## Validaciones

- Customer requerido y existente.
- Marca y modelo obligatorios, máximo 80 caracteres.
- Año entre 1900 y el año actual más uno.
- Color opcional, máximo 50 caracteres.
- Placa obligatoria, única y máximo 20 caracteres.
- VIN opcional, único y máximo 100 caracteres.
- Kilometraje opcional y no negativo.
- Los strings enviados no pueden contener únicamente espacios.

## Errores

| Código | Motivo |
| --- | --- |
| `400 Bad Request` | Body, año, kilometraje, id o paginación inválidos. |
| `401 Unauthorized` | Token ausente, inválido o expirado. |
| `403 Forbidden` | El rol no permite la operación. |
| `404 Not Found` | Vehicle o Customer inexistente. |
| `409 Conflict` | Placa/VIN duplicados o Vehicle con intakes asociados. |

Los errores usan el formato común `ApiResponse` y no exponen SQL ni stack traces.

## Pendientes

- Consulta de vehículos propios para `CLIENTE`.
- Endpoint anidado `/api/v1/customers/{customerId}/vehicles`.
- Implementación de Vehicle Intake.

Hibernate permanece con `ddl-auto: none`; este módulo no modifica el esquema.
