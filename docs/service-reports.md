# Service Reports / cierre oficial

## Alcance

Service Report representa el cierre oficial de un Job ya ejecutado. No sustituye la cotizacion
autorizada ni el Job: la Work Order Revision conserva la autorizacion, el Job conserva la ejecucion
real y el Service Report registra el cierre final y sus importes.

Todos los endpoints permanecen bajo `/api/v1`. No existe `/api/v2`.

## Autorizacion

`ADMINISTRADOR` conserva lectura, creación y PDF globales. `TECNICO` puede listar reportes mediante
`GET /api/v1/service-reports/assigned-to-me`, consultar un reporte y descargar su PDF solo cuando el
Job relacionado está asignado a su perfil. El filtro usa `service_reports.job_id` y
`jobs.technician_id` en repositorio. Un reporte o Job ajeno responde `404`.

- Sin JWT: `401 Unauthorized`.
- `CLIENTE`: `403 Forbidden`.
- `TECNICO` sin perfil: `403 Forbidden`.
- Crear reportes continúa reservado a `ADMINISTRADOR`.

## Endpoints

| Metodo | Ruta | Descripcion |
|---|---|---|
| GET | `/api/v1/service-reports?page=0&size=20` | Lista paginada, ordenada por ID descendente |
| GET | `/api/v1/service-reports/assigned-to-me?page=0&size=20` | Lista paginada de los Jobs del técnico |
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
`ADMINISTRADOR` descarga cualquier reporte; `TECNICO`, únicamente uno relacionado con su Job. Sin
JWT responde `401`, `CLIENTE` recibe `403` y un ID inexistente o ajeno al técnico responde `404`.

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
- lectura para clientes.

## Prueba de integración MySQL del listado técnico

`ServiceReportJpaRepositoryMySqlIT` ejecuta la consulta nativa paginada contra la base QA real.
La prueba es read-only, exige que `MECHSYNC_DB_URL` apunte exactamente a
`localhost:3307/mechsync_security_qa` y falla antes de iniciar si el destino es diferente. Valida
filtrado por técnico, orden descendente por `id_service_reports`, paginación, `totalElements` y
exclusión cruzada entre dos técnicos.

El nombre `*IT` la mantiene separada de la suite unitaria normal. Debe ejecutarse explícitamente,
con el túnel QA activo y las variables locales cargadas:

```powershell
mvn "-Dtest=ServiceReportJpaRepositoryMySqlIT" test
```

No debe ejecutarse contra `mechsync_db`, H2 ni una base distinta de la copia QA autorizada.
