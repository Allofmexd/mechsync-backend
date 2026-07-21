# Work Order Revisions

## Alcance

Las revisiones representan cotizaciones versionadas e inmutables de una Work Order. Esta
implementación completa la Fase 1 del MVP sobre `/api/v1`; “Work Orders v2” describe el modelo
interno de versionado y no una ruta `/api/v2`.

Cada revisión conserva snapshots de servicios y piezas. Cambiar posteriormente un catálogo no
altera una cotización histórica. Las líneas no tienen endpoints de actualización o eliminación:
cualquier cambio financiero requiere crear una revisión sucesora.

El módulo no crea Jobs automáticamente: una revisión final aprobada habilita el endpoint administrativo
de creación de Job. Service Reports pertenecen al cierre posterior. No existe PDF de cotización.

## Endpoints y autorización

| Método | Ruta | ADMINISTRADOR | TECNICO |
|---|---|---:|---:|
| GET | `/api/v1/work-orders/{workOrderId}/revisions?page=0&size=20` | Sí | Solo si está asignado |
| GET | `/api/v1/work-orders/{workOrderId}/revisions/current` | Sí | Solo si está asignado |
| GET | `/api/v1/work-orders/{workOrderId}/revisions/final-approved` | Sí | Solo si está asignado |
| GET | `/api/v1/work-orders/{workOrderId}/revisions/{revisionId}` | Sí | Solo si está asignado |
| POST | `/api/v1/work-orders/{workOrderId}/revisions` | Sí | No |
| PATCH | `/api/v1/work-orders/{workOrderId}/revisions/{revisionId}/send` | Sí | No |
| PATCH | `/api/v1/work-orders/{workOrderId}/revisions/{revisionId}/approve` | Sí | No |
| PATCH | `/api/v1/work-orders/{workOrderId}/revisions/{revisionId}/reject` | Sí | No |
| PATCH | `/api/v1/work-orders/{workOrderId}/revisions/{revisionId}/cancel` | Sí | No |

`CLIENTE` no tiene acceso durante el MVP. Sin un JWT válido se responde `401`; un rol no
autorizado recibe `403`. Para evitar IDOR, un técnico no asignado recibe `404` en consultas y no
puede inferir la existencia de la Work Order.

## Creación

El request de creación incluye las líneas snapshot. `services` y `parts` pueden omitirse o estar
vacíos; en ese caso el subtotal válido es cero. Si se informa `serviceId` o `partId`, el servidor
obtiene nombre y descripción del catálogo. Sin ID de catálogo, `nameSnapshot` es obligatorio.

```json
{
  "technicianId": 1,
  "estimatedStartDate": "2026-07-20T09:00:00",
  "estimatedDeliveryDate": "2026-07-22T18:00:00",
  "estimatedHours": 8.5000,
  "currency": "MXN",
  "applyIva": true,
  "ivaRate": 0.160000,
  "subtotalAmount": 2000.00,
  "ivaAmount": 320.00,
  "totalAmount": 2320.00,
  "technicalObservations": "Diagnóstico y cotización inicial.",
  "services": [
    {
      "lineNumber": 1,
      "serviceId": 1,
      "quantity": 1.000000,
      "unitPrice": 1200.0000,
      "lineSubtotal": 1200.0000
    }
  ],
  "parts": [
    {
      "lineNumber": 1,
      "partId": 1,
      "quantity": 1.000000,
      "unitPrice": 800.0000,
      "lineSubtotal": 800.0000
    }
  ]
}
```

El servidor asigna `revisionNumber` incremental dentro de la Work Order y devuelve `201 Created`
con `Location`. Desde la revisión 2, `changeReason` es obligatorio. La nueva revisión queda en
`DRAFT` y pasa a ser vigente. La anterior pasa a `SUPERSEDED` si estaba en `DRAFT`, `SENT` o
`REJECTED`. Una revisión aprobada permanece como evidencia histórica y como aprobación final.

Los números de línea deben ser positivos y únicos dentro de cada colección. Si se omiten, se
asignan según el orden del arreglo.

## Cálculos monetarios

- Todo importe usa `BigDecimal`; no se usa `float` ni `double`.
- El servidor recalcula cada subtotal de línea como `quantity * unitPrice`, con cuatro decimales.
- El subtotal de cabecera se deriva de la suma de las líneas y se redondea a dos decimales.
- `subtotalAmount`, `ivaAmount`, `totalAmount` y `lineSubtotal` enviados por el cliente solo se
  aceptan si coinciden con el cálculo del servidor.
- La moneda del MVP es `MXN`.
- Si `applyIva` es `true`, la tasa predeterminada es `0.160000`; IVA y total se redondean a dos
  decimales con `HALF_UP`.
- Si `applyIva` es `false`, la tasa e IVA son cero y el total equivale al subtotal.

## Workflow

Transiciones expuestas:

- `DRAFT -> SENT`
- `SENT -> APPROVED`
- `SENT -> REJECTED`
- `DRAFT -> CANCELLED`
- `SENT -> CANCELLED`

Al crear una sucesora, la transición interna a `SUPERSEDED` está permitida desde `DRAFT`, `SENT`
o `REJECTED`, conforme al SQL validado. Solo la revisión vigente puede ejecutar una transición.
Una revisión `APPROVED`, `SUPERSEDED` o `CANCELLED` no se modifica.

La concurrencia de creación y aprobación se serializa bloqueando la Work Order padre y usando
`lock_version`, evitando dos números de revisión o punteros vigentes/finales inconsistentes.

## Aprobación

`PATCH .../approve` recibe:

```json
{
  "acceptedByName": "Nombre del cliente",
  "acceptedByUserId": null,
  "acceptedAt": "2026-07-20T12:00:00",
  "acceptanceMethod": "IN_PERSON",
  "acceptanceNotes": "Aceptación registrada en el taller"
}
```

`acceptedByName` y un método activo del catálogo son obligatorios. `acceptedByUserId` es opcional,
pero debe referir a un usuario existente cuando se envía y nunca sustituye el nombre. Si
`acceptedAt` se omite, se usa la hora del servidor. Para el método `OTHER`, las notas son
obligatorias. La aprobación asigna el puntero `finalApprovedRevision` de la Work Order. El contrato
actual registra la aprobación interna y la evidencia de aceptación del cliente en la misma operación
atómica; no existe un endpoint separado de aceptación en el MVP.

Los endpoints `send`, `reject` y `cancel` no reciben body en esta fase porque el esquema validado no
incluye columnas persistentes de motivo para estas acciones.

## Respuesta y errores

La respuesta incluye identidad, número, estado, banderas `isCurrent` e `isFinalApproved`, datos de
planeación, importes, aceptación, `lockVersion`, timestamps y las líneas snapshot. El listado es
paginado y devuelve revisiones sin cargar líneas; los endpoints de detalle sí las incluyen. No se
exponen entidades JPA, hashes de contraseña ni otros datos sensibles.

- `400`: validación de request, catálogo, fecha o cálculo monetario inválido.
- `401`: autenticación ausente o inválida.
- `403`: rol sin permiso para la operación.
- `404`: Work Order/revisión inexistente o recurso no asignado al técnico.
- `409`: transición inválida, revisión no vigente o conflicto de versionado.

## Compatibilidad y límites

Los endpoints CRUD existentes de Work Orders se conservan bajo `/api/v1`. Los campos y rutas
existentes no se eliminan ni cambian en esta fase. Hasta retirar de forma planificada el modelo
legacy, sus importes estimados no deben interpretarse como sustituto de una revisión aprobada.

La creación de Job desde `finalApprovedRevision` y el cierre mediante Service Report están disponibles
en sus módulos administrativos. No existe aceptación directa por portal de cliente, generación de PDF
de cotización ni ruta pública `/api/v2`.
