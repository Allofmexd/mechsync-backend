# Jobs / trabajos reales

## Alcance

El módulo Jobs registra el trabajo realmente ejecutado a partir de una cotización autorizada. En el
MVP, un Job solo puede crearse desde la revisión `APPROVED` indicada por
`work_orders.final_approved_revision_id`. La creación no modifica la Work Order Revision ni sus
líneas snapshot.

Todos los endpoints públicos permanecen bajo `/api/v1`; no existe una API `/api/v2`.

## Dependencia de base de datos

Antes de desplegar este backend, el ambiente debe tener aplicados y validados, en este orden:

1. `v2/seeds/003_jobs_status_catalog.sql`;
2. `v2/migrations/006_prepare_jobs_for_authorized_revisions.sql`;
3. `v2/backfills/002_backfill_jobs_authorized_revisions.sql`;
4. assertions `009_assert_jobs_schema.sql` y `010_assert_jobs_workflow.sql` o sus validaciones
   operativas equivalentes.

La implementación Java no ejecuta migraciones. Un backend Jobs desplegado antes de ese gate puede
fallar por columnas, estados o triggers ausentes.

## Autorización

`ADMINISTRADOR` conserva listados, detalles y mutaciones globales. `TECNICO` puede listar mediante
`GET /api/v1/jobs/assigned-to-me` y consultar `GET /api/v1/jobs/{id}` únicamente cuando el Job está
asignado a su perfil. El filtro se aplica en repositorio; un Job ajeno responde `404`.

- Sin JWT: `401 Unauthorized`.
- `CLIENTE`: `403 Forbidden`.
- `TECNICO` sin perfil operativo: `403 Forbidden`.
- El listado global y todas las mutaciones continúan reservados a `ADMINISTRADOR`.

## Endpoints

### Listar Jobs

`GET /api/v1/jobs?page=0&size=20`

`page` inicia en cero y `size` admite de 1 a 100. La respuesta incluye `content`, `page`, `size`,
`totalElements` y `totalPages`.

Para `TECNICO`, `GET /api/v1/jobs/assigned-to-me?page=0&size=20` devuelve la misma estructura
paginada, limitada por `jobs.technician_id` al perfil derivado del JWT.

### Consultar Job

`GET /api/v1/jobs/{id}`

Devuelve `404` cuando el Job no existe.

### Crear Job

`POST /api/v1/jobs`

```json
{
  "workOrderId": 1,
  "initialApprovedRevisionId": 7,
  "technicianId": 3,
  "scheduledStartDate": "2026-07-20T09:00:00",
  "notes": "Trabajo creado a partir de cotización aprobada."
}
```

`workOrderId`, `initialApprovedRevisionId` y `technicianId` son obligatorios. Aunque la revisión
puede contener un técnico, el Job exige su propio técnico porque `jobs.technician_id` es `NOT NULL`.
La fecha programada es opcional y usa fecha-hora ISO-8601.

Validaciones:

- Work Order y técnico existentes;
- revisión existente y perteneciente a la Work Order;
- estado de revisión `APPROVED`;
- coincidencia con `final_approved_revision_id`;
- ausencia de otro Job para la Work Order o revisión autorizada;
- estado inicial obtenido del catálogo `JOBS/PENDIENTE`, sin IDs hardcodeados.

Responde `201 Created` y `Location: /api/v1/jobs/{id}`. Los importes reales nacen en `0.00`.

### Iniciar Job

`PATCH /api/v1/jobs/{id}/start`

No requiere body. Ejecuta `PENDIENTE → EN_PROCESO` y asigna `startDate` con hora del servidor.

### Completar Job

`PATCH /api/v1/jobs/{id}/complete`

```json
{
  "realSubtotalAmount": 1000.00,
  "realIvaAmount": 160.00,
  "realTotalAmount": 1160.00,
  "notes": "Trabajo completado."
}
```

Solo permite `EN_PROCESO → COMPLETADO`, exige inicio previo y asigna `completionDate`. Los importes
son obligatorios, no negativos, se normalizan a dos decimales y el total debe ser exactamente
subtotal más IVA. Cero es válido para garantía o trabajo sin cargo. Java usa `BigDecimal` y MySQL
`DECIMAL(10,2)`; no se usa `float` ni `double`.

### Cancelar Job

`PATCH /api/v1/jobs/{id}/cancel`

```json
{
  "cancellationNotes": "Cancelado por decisión del cliente."
}
```

Permite `PENDIENTE → CANCELADO` y `EN_PROCESO → CANCELADO`, asigna `cancelledAt`, conserva
`completionDate` en `null` y admite hasta 500 caracteres de notas.

## Líneas reales de servicios y piezas

Estas líneas representan lo realmente ejecutado o utilizado en el Job. Son independientes de las
líneas snapshot de la Work Order Revision aprobada y nunca modifican la cotización autorizada.

Los endpoints de lectura permiten `ADMINISTRADOR` y `TECNICO`:

- `GET /api/v1/jobs/{jobId}/services`;
- `GET /api/v1/jobs/{jobId}/parts`.

Para `TECNICO`, el backend resuelve el perfil desde el JWT y primero verifica
`jobs.id_jobs + jobs.technician_id`. Un Job ajeno o inexistente responde `404`; no se acepta
`technicianId` desde el cliente. Un técnico sin perfil recibe `403`.

Las mutaciones continúan reservadas a `ADMINISTRADOR`:

- `POST /api/v1/jobs/{jobId}/services`;
- `PUT /api/v1/jobs/{jobId}/services/{lineId}`;
- `DELETE /api/v1/jobs/{jobId}/services/{lineId}`;
- `POST /api/v1/jobs/{jobId}/parts`;
- `PUT /api/v1/jobs/{jobId}/parts/{lineId}`;
- `DELETE /api/v1/jobs/{jobId}/parts/{lineId}`.

Las consultas se permiten en cualquier estado. Crear, actualizar o eliminar solo se permite en
`PENDIENTE` o `EN_PROCESO`; un Job `COMPLETADO` o `CANCELADO` responde `409 Conflict`. Las búsquedas
de línea siempre combinan `lineId` y `jobId`; una línea de otro Job se trata como no encontrada para
evitar IDOR.

### Servicio real

Request de alta y actualización:

```json
{
  "serviceId": 1,
  "quantity": 1.00,
  "unitPrice": 1200.00
}
```

Response:

```json
{
  "id": 1,
  "jobId": 1,
  "serviceId": 1,
  "serviceName": "Cambio de aceite de transmisión",
  "quantity": 1.00,
  "unitPrice": 1200.00,
  "lineSubtotal": 1200.00,
  "createdAt": "2026-07-18T12:00:00",
  "updatedAt": null
}
```

### Pieza real

Request de alta y actualización:

```json
{
  "partId": 1,
  "quantity": 1.00,
  "unitPrice": 800.00
}
```

La respuesta sustituye `serviceId/serviceName` por `partId/partName`. El esquema v1 no contiene
una columna `notes` en `job_services` ni `job_parts`; por compatibilidad no se acepta ni se inventa
ese campo en esta fase.

Cantidad debe ser mayor que cero y precio unitario no negativo. Ambos admiten hasta dos decimales.
El backend calcula `lineSubtotal = quantity * unitPrice`, redondeado a dos decimales con
`BigDecimal`; el cliente no controla el subtotal. La restricción única permite una sola línea por
servicio o pieza dentro del mismo Job.

Después de cada alta, actualización o eliminación, el backend sincroniza `jobs.actual_subtotal` con
la suma de `job_services.actual_subtotal + job_parts.actual_subtotal`, conserva `actual_iva` y
recalcula `actual_total = actual_subtotal + actual_iva`. El endpoint de completar continúa siendo el
cierre financiero, exige que el subtotal enviado coincida con la suma real de líneas y valida IVA y
total. No se descuenta inventario ni stock.

## Respuesta

```json
{
  "success": true,
  "data": {
    "id": 1,
    "workOrderId": 1,
    "initialApprovedRevisionId": 7,
    "technicianId": 3,
    "status": "PENDIENTE",
    "scheduledStartDate": "2026-07-20T09:00:00",
    "startDate": null,
    "completionDate": null,
    "cancelledAt": null,
    "actualHours": null,
    "realSubtotalAmount": 0.00,
    "realIvaAmount": 0.00,
    "realTotalAmount": 0.00,
    "notes": "Trabajo creado a partir de cotización aprobada.",
    "cancellationNotes": null,
    "createdAt": "2026-07-18T12:00:00",
    "updatedAt": null
  },
  "error": null
}
```

No se exponen entidades JPA, tokens, hashes ni credenciales.

## Estados y conflictos

Estados: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`, `CANCELADO`.

Transiciones permitidas:

- `PENDIENTE → EN_PROCESO`;
- `PENDIENTE → CANCELADO`;
- `EN_PROCESO → COMPLETADO`;
- `EN_PROCESO → CANCELADO`.

Los estados terminales no permiten nuevas transiciones. Las operaciones de workflow toman bloqueo
pesimista y el esquema vuelve a validar integridad mediante triggers. Una transición inválida,
duplicidad o conflicto concurrente responde `409`; datos inválidos responden `400`.

## Fuera de alcance

- Service Reports;
- PDF, dashboards, frontend, inventario y portal cliente;
- notas por línea, porque el esquema actual no ofrece esa columna.

## Prueba MySQL QA de ownership de líneas

`JobLineOwnershipMySqlIT` ejecuta las consultas reales contra la copia aislada
`localhost:3307/mechsync_security_qa`. Es read-only y falla antes de iniciar si el destino o usuario
no corresponde al ambiente QA. Valida dos técnicos, ownership cruzado de Jobs y que servicios y
piezas devueltos pertenecen únicamente al `job_id` consultado.
