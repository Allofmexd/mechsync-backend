# Catálogo de servicios

## Alcance

Esta fase expone únicamente lectura paginada del catálogo real `services`. Su objetivo es
permitir que cotizaciones y líneas reales de Jobs seleccionen servicios existentes sin
hardcodear identificadores.

No incluye alta, edición, eliminación ni administración de estados.

## Endpoint

`GET /api/v1/services?page=0&size=20&search=aceite`

Parámetros:

- `page`: índice de página desde cero; default `0`.
- `size`: registros por página entre `1` y `100`; default `20`.
- `search`: texto opcional, máximo 100 caracteres, aplicado sin distinguir mayúsculas a
  `name` y `description`.

El orden es estable por `name` ascendente y luego por `id` ascendente.

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
        "name": "Diagnóstico electrónico",
        "description": "Revisión del sistema electrónico",
        "basePrice": 500.00,
        "estimatedHours": 1.50,
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

Los precios se mapean con `BigDecimal` desde `DECIMAL`; no se usan `float` ni `double`.

## Límites

- No existe CRUD de servicios en esta fase.
- No existe `/api/v2`.
- Este endpoint desbloquea el selector de servicios para líneas reales de Jobs.
