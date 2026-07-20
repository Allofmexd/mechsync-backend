# Service Reports / cierre oficial

## Alcance

Service Report representa el cierre oficial de un Job ya ejecutado. No sustituye la cotizacion
autorizada ni el Job: la Work Order Revision conserva la autorizacion, el Job conserva la ejecucion
real y el Service Report registra el cierre final y sus importes.

Todos los endpoints permanecen bajo `/api/v1`. No existe `/api/v2`.

## Autorizacion

En esta fase todos los endpoints requieren `ADMINISTRADOR`.

- Sin JWT: `401 Unauthorized`.
- `TECNICO` y `CLIENTE`: `403 Forbidden`.

La lectura para tecnicos queda pendiente hasta aplicar aislamiento seguro por asignacion. El portal
cliente queda fuera del MVP actual.

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/v1/service-reports?page=0&size=20` | Lista paginada, ordenada por ID descendente |
| GET | `/api/v1/service-reports/{id}` | Consulta por ID |
| GET | `/api/v1/service-reports/{id}/pdf` | Genera y descarga el PDF del reporte |
| GET | `/api/v1/jobs/{jobId}/service-report` | Consulta el reporte unico de un Job |
| POST | `/api/v1/service-reports` | Crea el cierre desde un Job completado |

No se exponen `PUT`, `PATCH` ni `DELETE`.

## Creacion

```json
{
  "jobId": 1,
  "finalDescription": "Trabajo completado correctamente.",
  "customerConfirmation": true,
  "deliveredAt": "2026-07-18T18:00:00"
}
```

`jobId` y `finalDescription` son obligatorios. `customerConfirmation` es `false` si se omite y
`deliveredAt` es opcional. El esquema real utiliza `final_description`; no ofrece una columna de
recomendaciones, por lo que esta API no inventa ni mezcla ese dato dentro de otro campo.

El servidor valida que el Job exista y tenga estado `COMPLETADO`. Los estados `PENDIENTE`,
`EN_PROCESO` y `CANCELADO` responden `409 Conflict`. La restriccion unica
`uq_service_reports_job_id` y una validacion de aplicacion impiden mas de un reporte por Job.

Los importes `finalSubtotal`, `finalIva` y `finalTotal` se copian del Job; nunca se aceptan desde el
request. Java usa `BigDecimal` y MySQL `DECIMAL(10,2)`. Crear el reporte no modifica el Job, sus
servicios/piezas reales ni la Work Order Revision aprobada.

Un reporte sin `deliveredAt` nace `COMPLETADO`; con `deliveredAt` nace `ENTREGADO`. El estado se
resuelve por contexto/codigo en `status_catalog`, sin IDs hardcodeados.

La tabla no exige lineas para crear el cierre. Se acepta un Job completado sin lineas porque el flujo
de garantia o sin cargo puede cerrar con importes en cero; el Job sigue siendo la fuente de verdad.

## Respuesta

```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "jobId": 1,
    "status": "ENTREGADO",
    "reportDate": "2026-07-18T18:00:00",
    "finalDescription": "Trabajo completado correctamente.",
    "finalSubtotal": 3660.00,
    "finalIva": 585.60,
    "finalTotal": 4245.60,
    "customerConfirmation": true,
    "deliveredAt": "2026-07-18T18:00:00",
    "createdAt": "2026-07-18T18:00:00",
    "updatedAt": null
  }
}
```

No se exponen entidades JPA, tokens, hashes ni credenciales.

## Descarga PDF

`GET /api/v1/service-reports/{id}/pdf` genera el documento en memoria para un reporte existente.
Requiere rol `ADMINISTRADOR`; sin JWT responde `401` y `TECNICO`/`CLIENTE` reciben `403`.
Un ID inexistente responde `404`.

La respuesta exitosa es binaria:

- `Content-Type: application/pdf`;
- `Content-Disposition: attachment; filename="service-report-{id}.pdf"`;
- `Cache-Control: no-store`.

Ejemplo, sin registrar el JWT en el historial del comando:

```bash
curl --fail --show-error \
  -H "Authorization: Bearer $TOKEN" \
  -o service-report-1.pdf \
  http://localhost:8080/api/v1/service-reports/1/pdf
```

El PDF incluye identificadores del reporte, Job, Work Order e ingreso; estado y fechas; descripcion
final; cliente, tecnico y vehiculo; kilometraje disponible; servicios y piezas reales; unidad de las
piezas; subtotal, IVA y total. Cuando no hay lineas muestra un estado vacio explicito. Los importes
provienen del Service Report y se mantienen como `BigDecimal`/`DECIMAL`.

El documento se genera con Apache PDFBox y no se persiste en disco ni en base de datos. La descarga
no modifica `service_reports`, Jobs, lineas reales o Work Order Revisions. Tampoco genera correo,
almacenamiento S3, firmas digitales, imagenes o evidencias.

## Limites de la fase

- descarga del PDF desde frontend;
- correo, firmas digitales e imagenes;
- recomendaciones estructuradas, porque la columna no existe;
- edicion posterior al cierre;
- lectura aislada para tecnicos o clientes.
