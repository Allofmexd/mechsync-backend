# Catálogos de solo lectura

## Alcance

Este módulo expone catálogos existentes de MechSync sin crear, modificar ni sembrar datos. Los
identificadores devueltos pertenecen a la base de datos del ambiente consultado y nunca deben
hardcodearse en el frontend.

## Estados por contexto

| Método | Ruta | Roles |
|---|---|---|
| GET | `/api/v1/catalogs/statuses?context={context}` | ADMINISTRADOR, TECNICO |

`context` es obligatorio. Los valores admitidos corresponden a los contextos reales del esquema:
`USERS`, `SERVICES`, `PARTS`, `VEHICLE_INTAKES`, `WORK_ORDERS`, `JOBS` y `SERVICE_REPORTS`.
Un valor desconocido devuelve HTTP 400. `CLIENTE` no tiene acceso a este catálogo administrativo.

Para registrar un ingreso de vehículo se debe consultar:

```bash
curl "http://localhost:8080/api/v1/catalogs/statuses?context=VEHICLE_INTAKES" \
  -H "Authorization: Bearer $TOKEN"
```

Respuesta de ejemplo; los IDs son ilustrativos y deben tomarse siempre de la respuesta actual:

```json
{
  "status": "OK",
  "data": [
    {
      "id": 7,
      "code": "EN_DIAGNOSTICO",
      "name": "En diagnostico",
      "context": "VEHICLE_INTAKES",
      "description": "Vehiculo en revision inicial y diagnostico."
    }
  ]
}
```

`code`, `context` y `description` proceden de `status_catalog`. `name` es una etiqueta de
presentación derivada de `code`. Al crear un ingreso, el frontend debe enviar como `statusId` el
`id` de la opción elegida.

## Errores

- `400`: falta el contexto o no corresponde a un contexto válido.
- `401`: falta un JWT válido.
- `403`: el rol autenticado no tiene permiso.

