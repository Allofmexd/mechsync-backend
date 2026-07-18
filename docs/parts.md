# Catálogo de piezas

## Alcance

Esta fase expone únicamente lectura paginada del catálogo real `parts` y su unidad de
medida. Su objetivo es permitir que cotizaciones y líneas reales de Jobs seleccionen piezas
existentes sin hardcodear identificadores.

No incluye alta, edición, eliminación, inventario, movimientos de stock ni proveedores.

## Endpoint

`GET /api/v1/parts?page=0&size=20&search=filtro`

Parámetros:

- `page`: índice de página desde cero; default `0`.
- `size`: registros por página entre `1` y `100`; default `20`.
- `search`: texto opcional, máximo 100 caracteres, aplicado sin distinguir mayúsculas a
  `name` y `description`.

El orden es estable por `name` ascendente y luego por `id` ascendente. La unidad se obtiene
de la relación real `parts.unit_id -> measurement_units.id_measurement_units`.

## Autorización

- `ADMINISTRADOR`: permitido.
- `TECNICO`: permitido para lectura operativa.
- `CLIENTE`: `403 Forbidden`.
- Sin token válido: `401 Unauthorized`.

## Respuesta

```json
{
  "status": "OK",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Filtro de transmisión",
        "description": "Filtro compatible",
        "unitPrice": 800.00,
        "measurementUnitId": 1,
        "measurementUnitName": "PIEZA",
        "measurementUnitAbbreviation": "PZA",
        "createdAt": "2026-07-18T12:00:00",
        "updatedAt": null
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

El precio se mapea con `BigDecimal` desde `DECIMAL`. `available_stock` no se expone porque
inventario está fuera del alcance de esta fase.

## Límites

- No existe CRUD de piezas en esta fase.
- No se descuenta ni administra stock.
- No existe `/api/v2`.
- Este endpoint desbloquea el selector de piezas para líneas reales de Jobs.
