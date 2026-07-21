# Módulo Work Orders

## Alcance

Una Work Order es la planificación o cotización posterior al ingreso del vehículo. No representa
trabajo ejecutado: esa responsabilidad corresponde a `Job`, que se implementará posteriormente.
La relación raíz es `vehicleIntakeId`; no se almacenan ni duplican `customer_id` ni `vehicle_id`.

## Endpoints

| Método | Ruta | Descripción | Roles |
|---|---|---|---|
| GET | `/api/v1/work-orders` | Lista paginada global | ADMINISTRADOR |
| GET | `/api/v1/work-orders/assigned-to-me` | Lista paginada asignada | TECNICO |
| GET | `/api/v1/work-orders/{id}` | Consulta global o asignada | ADMINISTRADOR, TECNICO |
| POST | `/api/v1/work-orders` | Crea una planificación/cotización | ADMINISTRADOR |
| PUT | `/api/v1/work-orders/{id}` | Actualiza planificación/cotización | ADMINISTRADOR |
| DELETE | `/api/v1/work-orders/{id}` | Elimina si no tiene dependencias | ADMINISTRADOR |

El listado técnico se filtra en repositorio por el Technician ID resuelto desde el usuario del JWT.
En el detalle, un ID asignado a otro técnico responde `404`. `TECNICO` no puede usar el listado
global ni enviar un Technician ID para ampliar su alcance. Un técnico sin perfil recibe `403`.

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

Las tablas legacy `work_order_services` y `work_order_parts` no se exponen como subrecursos. Las
nuevas líneas snapshot pertenecen a revisiones y sus importes se calculan autoritativamente en el
backend. Tampoco existe aún consulta propia para `CLIENTE`.

## Cotizaciones versionadas

Las nuevas cotizaciones se gestionan como revisiones inmutables mediante endpoints aditivos bajo
`/api/v1/work-orders/{workOrderId}/revisions`. La nueva API conserva snapshots de servicios y piezas,
calcula los importes en servidor y separa la revisión vigente de la aprobación final.

Los endpoints y campos de Work Orders existentes se mantienen por compatibilidad con el frontend.
No se abre una ruta `/api/v2`: “Work Orders v2” identifica únicamente la evolución interna del
modelo. El contrato completo está documentado en [Work Order Revisions](work-order-revisions.md).
