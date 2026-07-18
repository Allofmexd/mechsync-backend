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
| GET | `/api/v1/jobs/{jobId}/service-report` | Consulta el reporte unico de un Job |
| POST | `/api/v1/service-reports` | Crea el cierre desde un Job completado |

No se exponen `PUT`, `PATCH`, `DELETE`, PDF ni generacion de archivos.

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

## Limites de la fase

- frontend de Service Reports;
- PDF y descarga de archivos;
- correo, firmas digitales e imagenes;
- recomendaciones estructuradas, porque la columna no existe;
- edicion posterior al cierre;
- lectura aislada para tecnicos o clientes.
