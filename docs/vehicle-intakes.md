# Módulo Vehicle Intake

## Alcance

Registra el ingreso físico y documental de un vehículo al taller. Cada ingreso referencia a
`Vehicle`; el cliente se obtiene mediante `Vehicle → Customer → User`. El módulo no almacena ni
duplica `customer_id`.

## Endpoints y autorización

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/vehicle-intakes` | Lista paginada | ADMINISTRADOR, TECNICO |
| GET | `/api/v1/vehicle-intakes/{id}` | Consulta un ingreso | ADMINISTRADOR, TECNICO |
| POST | `/api/v1/vehicle-intakes` | Registra un ingreso | ADMINISTRADOR, TECNICO |
| PUT | `/api/v1/vehicle-intakes/{id}` | Actualiza un ingreso | ADMINISTRADOR, TECNICO |
| DELETE | `/api/v1/vehicle-intakes/{id}` | Elimina si no tiene Work Orders | ADMINISTRADOR |

`CLIENTE` no tiene acceso administrativo en esta fase. La consulta propia requiere diseñar la
cadena segura desde el `userId` del JWT.

## Campos

- `vehicleId`: obligatorio al crear e inmutable al actualizar.
- `technicianId`: opcional; debe identificar un técnico existente cuando se proporciona.
- `intakeDate`: opcional; si se omite se usa la fecha/hora actual.
- `intakeMileage`: opcional y no negativo.
- `reportedProblem`: obligatorio y no vacío.
- `initialObservations`: opcional, pero no acepta texto vacío.
- `statusId`: obligatorio y debe pertenecer al contexto `VEHICLE_INTAKES`.

Estados existentes para este contexto: `EN_DIAGNOSTICO`, `EN_PROCESO`, `EN_ESPERA_PIEZAS`,
`COMPLETADO` y `CANCELADO`. Debe enviarse el identificador existente, no el nombre.

## Integración con catálogos y técnicos

Antes de crear un ingreso, el frontend debe consultar
`GET /api/v1/catalogs/statuses?context=VEHICLE_INTAKES` y usar como `statusId` el `id` de la opción
seleccionada. Los IDs dependen del ambiente y no deben hardcodearse.

El selector opcional de técnico se carga mediante `GET /api/v1/technicians`. Cuando no se asigna
un técnico, `technicianId` puede omitirse o enviarse como `null`; cuando se asigna, debe utilizarse
el `id` devuelto por ese endpoint.

## Ejemplos

```bash
curl -i "http://localhost:8080/api/v1/vehicle-intakes?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

curl -i -X POST http://localhost:8080/api/v1/vehicle-intakes \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleId": 1,
    "technicianId": null,
    "intakeDate": "2026-07-12T10:30:00",
    "intakeMileage": 150000,
    "reportedProblem": "Patina al cambiar de segunda a tercera",
    "initialObservations": "Se escucha ruido al acelerar",
    "statusId": 7
  }'

curl -i -X DELETE http://localhost:8080/api/v1/vehicle-intakes/1 \
  -H "Authorization: Bearer $TOKEN"
```

Las respuestas exitosas usan `ApiResponse`; la creación devuelve HTTP 201 y una cabecera
`Location`. El listado incluye `content`, `page`, `size`, `totalElements` y `totalPages`.

## Errores

- `400`: request o parámetros inválidos.
- `401`: falta un JWT válido.
- `403`: rol insuficiente.
- `404`: ingreso, vehículo, técnico o estado aplicable inexistente.
- `409`: el ingreso ya tiene una Work Order y no puede eliminarse.

El borrado es físico únicamente cuando no existen Work Orders. Work Orders, Jobs, Service Reports,
el listado anidado por vehículo y la consulta propia de CLIENTE quedan fuera de esta fase.
