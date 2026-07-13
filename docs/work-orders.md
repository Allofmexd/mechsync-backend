# Módulo Work Orders

## Alcance

Una Work Order es la planificación o cotización posterior al ingreso del vehículo. No representa
trabajo ejecutado: esa responsabilidad corresponde a `Job`, que se implementará posteriormente.
La relación raíz es `vehicleIntakeId`; no se almacenan ni duplican `customer_id` ni `vehicle_id`.

## Endpoints

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/work-orders` | Lista paginada | ADMINISTRADOR, TECNICO |
| GET | `/api/v1/work-orders/{id}` | Consulta por ID | ADMINISTRADOR, TECNICO |
| POST | `/api/v1/work-orders` | Crea una planificación/cotización | ADMINISTRADOR, TECNICO |
| PUT | `/api/v1/work-orders/{id}` | Actualiza planificación/cotización | ADMINISTRADOR, TECNICO |
| DELETE | `/api/v1/work-orders/{id}` | Elimina si no tiene dependencias | ADMINISTRADOR |

## Campos y validaciones

- `vehicleIntakeId`: obligatorio al crear e inmutable al actualizar.
- `technicianId`: obligatorio y debe existir.
- `workOrderDate`: opcional; al omitirla se usa la fecha/hora actual.
- `estimatedStartDate`, `estimatedDeliveryDate`: opcionales; entrega no puede anteceder al inicio.
- `estimatedHours`: opcional y no negativo.
- `estimatedSubtotal`, `estimatedIva`, `estimatedTotal`: obligatorios y no negativos.
- `technicalObservations`: opcional, pero no acepta texto vacío.
- `statusId`: obligatorio y perteneciente al contexto `WORK_ORDERS`.

Estados existentes: `PENDIENTE`, `APROBADO`, `RECHAZADO`, `EN_PROCESO` y `CANCELADO`.

## Ejemplo

```bash
curl -i "http://localhost:8080/api/v1/work-orders?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"

curl -i -X POST http://localhost:8080/api/v1/work-orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicleIntakeId": 1,
    "technicianId": 1,
    "estimatedStartDate": "2026-07-15T09:00:00",
    "estimatedDeliveryDate": "2026-07-20T18:00:00",
    "estimatedHours": 8.00,
    "estimatedSubtotal": 8500.00,
    "estimatedIva": 1360.00,
    "estimatedTotal": 9860.00,
    "technicalObservations": "Revisión de cuerpo de válvulas y cambio de aceite",
    "statusId": 12
  }'
```

## Errores

- `400`: request, importes o fechas inválidos.
- `401`: falta JWT válido.
- `403`: rol insuficiente.
- `404`: Work Order, Vehicle Intake, técnico o estado inexistente/no aplicable.
- `409`: existen Jobs o detalles planificados que impiden el borrado.

## Pendientes

Aunque existen `work_order_services` y `work_order_parts`, sus subrecursos no se exponen todavía.
Antes de reemplazarlos debe definirse si el backend calcula autoritativamente subtotales, IVA y total,
incluidas reglas de redondeo. Tampoco existe aún consulta propia para `CLIENTE`.
