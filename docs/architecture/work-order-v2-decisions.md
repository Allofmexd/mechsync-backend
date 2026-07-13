# Decisiones técnicas para Work Order v2 y cotizaciones versionadas

## 1. Contexto

MechSync v1 ya dispone de Work Orders relacionadas con Vehicle Intake y separa importes estimados
de los importes reales de Job y finales de Service Report. Sin embargo, cada Work Order contiene una
sola cotización mutable: una actualización reemplaza importes, condiciones y observaciones sin
conservar la versión anterior.

La auditoría `work-order-quotation-versioning-audit.md` concluyó que v1 no puede ofrecer historial
financiero auditable, aprobación atribuible ni snapshots documentales completos. Por ello se adopta
`database/v2` como siguiente diseño de datos. Este documento cierra las reglas necesarias para
diseñar posteriormente el DDL y adaptar el backend; no cambia el sistema vigente.

La separación de responsabilidades queda fijada:

- **Work Order:** identidad del diagnóstico, planificación y proceso de cotización.
- **Work Order Revision:** snapshot inmutable de una propuesta económica y sus condiciones.
- **Job:** ejecución real autorizada por una revisión aprobada.
- **Service Report:** cierre oficial y costos finales.

## 2. Decisiones confirmadas

1. Una Work Order pertenece a un Vehicle Intake y puede tener muchas revisiones.
2. Work Order no almacena `customer_id` ni `vehicle_id`; ambos se obtienen desde Vehicle Intake.
3. Solo una revisión por Work Order es la vigente.
4. Puede haber varias revisiones aprobadas en el historial, pero solo una es la aprobación final
   activa que puede iniciar un Job.
5. Job debe referenciar directamente la revisión aprobada que autorizó su inicio.
6. Toda modificación financiera, de planificación o de detalles crea una nueva revisión.
7. El contenido de revisiones anteriores nunca se sobrescribe.
8. Work Order puede existir sin servicios ni piezas planificadas.
9. Los detalles son soporte documental opcional y no determinan obligatoriamente el subtotal.
10. El subtotal de la revisión se captura manualmente y es la fuente autoritativa.
11. IVA es opcional por revisión; la tasa predeterminada cuando aplica es 16 %.
12. La moneda predeterminada es MXN y se conserva en cada revisión.
13. La aceptación del cliente se conserva como metadata, aun cuando el cliente no tenga usuario.
14. La aprobación interna del taller y la aceptación del cliente son hechos distintos.
15. Los costos reales no se escriben en Work Order ni en sus revisiones.
16. No se permite borrar físicamente una Work Order o revisión con historia financiera; se usan
    estados de cancelación.

## 3. Modelo de revisiones

### 3.1 Work Order principal

`work_orders` representa el agregado estable. Conserva:

- identidad;
- relación con Vehicle Intake;
- estado operativo general;
- referencia a la revisión vigente;
- referencia a la revisión aprobada final activa;
- auditoría de creación y actualización;
- versión de concurrencia.

No conserva subtotal, IVA, total, técnico responsable de la propuesta, fechas estimadas ni detalles
que puedan variar entre revisiones.

### 3.2 Revisión vigente

La revisión vigente es la propuesta más reciente que el taller considera activa para consulta,
negociación o seguimiento. Se identifica exclusivamente mediante `current_revision_id` en
`work_orders`; no se infiere con `MAX(revision_number)` ni con un booleano distribuido.

Reglas:

- Solo existe un puntero vigente por Work Order.
- Puede apuntar a una revisión `DRAFT`, `SENT`, `APPROVED`, `REJECTED` o `CANCELLED`.
- No puede apuntar a una revisión `SUPERSEDED`.
- Una revisión rechazada puede seguir siendo la vigente hasta crear una sucesora.
- Crear una revisión nueva actualiza atómicamente el puntero y marca la anterior como
  `SUPERSEDED`, salvo que la anterior permanezca `APPROVED` como evidencia histórica.
- Cambiar el puntero no elimina ni modifica el contenido de revisiones anteriores.

### 3.3 Revisión aprobada final activa

La aprobación final activa se identifica mediante `final_approved_revision_id` en `work_orders`.
Es la única revisión autorizada para iniciar un Job nuevo.

Reglas:

- Debe pertenecer a la misma Work Order.
- Debe tener estado `APPROVED`.
- Debe contener aprobación interna y aceptación del cliente completas.
- Puede ser igual o distinta de la revisión vigente. Por ejemplo, una revisión nueva puede estar en
  negociación mientras la aprobación anterior sigue siendo la autorización activa.
- Las revisiones aprobadas anteriormente permanecen en el historial con sus datos de aprobación.
- Aprobar una revisión posterior cambia el puntero final dentro de una transacción; no borra la
  aprobación histórica anterior.
- Cuando un Job ya fue creado, su revisión inicial autorizante queda fija aunque posteriormente se
  aprueben ajustes.

### 3.4 Estados de revisión

Los estados se almacenarán en un catálogo separado `work_order_revision_status_catalog`. No se
reutiliza el contexto operativo de `status_catalog`, porque el estado de la Work Order y el estado
documental de una propuesta son conceptos distintos.

Estados definitivos:

- `DRAFT`: propuesta preparada internamente y todavía no enviada.
- `SENT`: propuesta presentada al cliente; queda disponible para aceptación o rechazo.
- `APPROVED`: propuesta aceptada por el cliente y aprobada internamente.
- `REJECTED`: propuesta rechazada por el cliente.
- `SUPERSEDED`: propuesta reemplazada por una revisión posterior sin ser la aprobación final activa.
- `CANCELLED`: propuesta retirada explícitamente sin cancelar necesariamente toda la Work Order.

Transiciones permitidas:

- `DRAFT → SENT | CANCELLED | SUPERSEDED`;
- `SENT → APPROVED | REJECTED | CANCELLED | SUPERSEDED`;
- `REJECTED → SUPERSEDED` al crear una sucesora;
- `APPROVED` conserva ese estado como evidencia histórica, aunque deje de ser la aprobación activa;
- estados terminales no regresan a `DRAFT`.

El contenido snapshot no cambia durante estas transiciones.

### 3.5 Inmutabilidad

Desde la inserción de una revisión son inmutables:

- Work Order asociada y número de revisión;
- técnico y fechas de planificación;
- subtotal, configuración de IVA, moneda y totales;
- notas técnicas y notas para el cliente;
- motivo de cambio y autor;
- servicios y piezas snapshot.

Solo pueden cambiar mediante transiciones controladas:

- estado documental;
- metadata de aprobación interna;
- metadata de aceptación del cliente;
- punteros vigente y aprobada final en la Work Order.

Incluso una corrección en `DRAFT` crea una revisión sucesora. Esto evita que exista una ventana de
edición silenciosa y simplifica la auditoría. Los detalles de una revisión se insertan en la misma
transacción que su cabecera; no se reemplazan después.

### 3.6 Creación de una nueva revisión

Se crea una nueva revisión cuando cambia cualquiera de estos elementos:

- subtotal, tratamiento de IVA, tasa, moneda o total;
- técnico, horas o fechas estimadas;
- notas técnicas o condiciones comunicadas al cliente;
- servicios, piezas, cantidades, precios snapshot o notas de línea;
- cualquier costo adicional que requiera nueva autorización.

La operación:

1. bloquea la fila de Work Order;
2. obtiene el siguiente `revision_number`;
3. copia la revisión vigente y sus detalles como punto de partida, si existe;
4. aplica los cambios solicitados en el nuevo snapshot;
5. exige `change_reason` desde la revisión 2;
6. inserta cabecera y detalles atómicamente;
7. actualiza `current_revision_id`;
8. conserva sin cambios `final_approved_revision_id` hasta una nueva aprobación.

La numeración inicia en 1 y aumenta de uno en uno por Work Order. Nunca se reutilizan números.

### 3.7 Rechazo, cancelación y costos adicionales

- **Rechazo:** la revisión pasa a `REJECTED`. No se borra. Puede seguir vigente hasta que se cree
  otra propuesta.
- **Cancelación de revisión:** pasa a `CANCELLED`; mantiene su contenido y evidencia.
- **Cancelación de Work Order:** cambia el estado general a cancelado, bloquea nuevas revisiones y
  Jobs, pero conserva todos los punteros y el historial para consulta.
- **Costo adicional antes de iniciar Job:** genera revisión nueva; solo después de su aceptación y
  aprobación puede sustituir la aprobación final activa.
- **Costo adicional después de iniciar Job:** genera una revisión de autorización adicional. El Job
  conserva su revisión inicial y, en la fase Jobs, deberá incorporar una relación
  `job_authorized_revisions` para registrar ampliaciones aprobadas sin reescribir el origen.

Las vistas administrativas pueden consultar todas las revisiones. Una vista para cliente solo debe
mostrar revisiones presentadas (`SENT`) y sus estados posteriores, nunca borradores internos.

## 4. Modelo monetario

### 4.1 Tipos y precisión

Decisión definitiva:

- Montos monetarios en MySQL: `DECIMAL(19,4)`.
- Tasas en MySQL: `DECIMAL(10,6)`, expresadas como fracción; 16 % se almacena como `0.160000`.
- Cantidades de líneas: `DECIMAL(19,6)`.
- Horas: `DECIMAL(10,4)`.
- Java: `BigDecimal` en dominio, comandos, DTOs y persistencia.
- Queda prohibido usar `float` o `double` para importes, tasas, cantidades calculadas o totales.

`DECIMAL(19,4)` ofrece rango amplio y cuatro decimales para precios intermedios sin sobredimensionar
todos los montos a seis decimales. La escala 6 se reserva para tasas y cantidades, donde la precisión
fraccionaria es más probable.

### 4.2 Campos financieros por revisión

- `subtotal_amount`: presupuesto neto capturado manualmente.
- `apply_iva`: indica de forma inequívoca si se agrega IVA.
- `iva_rate`: `0.160000` cuando aplica; `0.000000` cuando no aplica.
- `iva_amount`: monto calculado del impuesto.
- `total_amount`: subtotal más IVA.
- `currency`: código ISO 4217 de tres caracteres; predeterminado `MXN`.

El backend es la autoridad de `iva_amount` y `total_amount`. El cliente envía subtotal,
`applyIva` y moneda; no puede imponer totales contradictorios.

### 4.3 Cálculo y redondeo

Reglas definitivas:

1. Se usa aritmética decimal exacta con `BigDecimal`.
2. La tasa no se construye desde tipos binarios; se usa representación textual o constante decimal.
3. Si `applyIva=false`, tasa y monto son cero, y total equivale al subtotal.
4. Si `applyIva=true`, `ivaAmount = subtotalAmount × ivaRate` y
   `totalAmount = subtotalAmount + ivaAmount`.
5. Los cálculos internos conservan la precisión disponible y se normalizan a escala 4 al persistir
   el snapshot, usando `RoundingMode.HALF_UP`.
6. Al presentar, aprobar, facturar, cerrar o generar un documento, los importes contractuales se
   expresan a escala 2 con `HALF_UP`.
7. La revisión debe guardar los importes contractuales ya presentados con equivalencia reproducible;
   no se recalculan usando precios de catálogo posteriores.
8. Los totales de línea se calculan con cantidad escala 6 y precio escala 4, y se normalizan a escala
   4. La visualización usa escala 2.

La creación de una revisión `SENT` representa el punto de presentación y debe fijar los importes a
dos decimales dentro de columnas escala 4 (`8500.0000`, por ejemplo). Una revisión `DRAFT` también
guarda valores escala 4, pero antes de pasar a `SENT` se valida que su representación a dos decimales
sea la que se comunicará. Como el contenido no se edita, cualquier corrección crea una sucesora.

### 4.4 IVA opcional y precios con IVA incluido

- `apply_iva=false` representa una cotización sin IVA agregado. Debe poder acompañarse de
  `tax_treatment_notes` cuando el negocio necesite justificar el tratamiento.
- Solicitar factura y aplicar IVA son decisiones distintas. Un futuro `requires_invoice` no debe
  controlar `apply_iva` automáticamente.
- v2 no aceptará inicialmente precios declarados “IVA incluido”. Todos los importes de cabecera se
  interpretan como netos antes de IVA cuando `apply_iva=true`.
- Si una fuente entrega un precio con IVA incluido, debe normalizarse antes de crear el snapshot.
- Soportar formalmente precios IVA incluido requerirá una fase posterior con un modo fiscal explícito;
  no se agregará una segunda bandera ambigua en v2 inicial.

La validez fiscal del tratamiento sin IVA debe definirse con asesoría contable y normativa aplicable;
el sistema registra la decisión, pero no la deduce de la forma de pago.

### 4.5 Snapshot monetario

Se conservan por revisión:

- subtotal manual;
- bandera y tasa de IVA;
- monto de IVA y total;
- moneda;
- cantidades, precios unitarios y totales de línea;
- nombres y descripciones mostrados al cliente;
- condiciones y notas asociadas.

La suma de detalles puede diferir del subtotal manual. La API debe exponer, de forma informativa,
`details_total_amount` calculado sin tratar esa diferencia como error.

## 5. Modelo de aceptación

### 5.1 Aprobación interna del taller

La aprobación interna confirma que un usuario autorizado revisó la evidencia de aceptación y habilitó
la revisión para iniciar trabajo:

- `approved_by_user_id`: usuario interno que registra/aprueba la autorización;
- `approved_at`: momento de aprobación interna.

No identifica necesariamente a la persona cliente que aceptó.

### 5.2 Aceptación del cliente

Metadata obligatoria para una revisión `APPROVED`:

- `accepted_by_name`: nombre de la persona que aceptó;
- `accepted_by_user_id`: nullable; se usa cuando esa persona tiene usuario relacionado;
- `accepted_at`: momento reportado de aceptación;
- `acceptance_method_id`: método normalizado;
- `acceptance_notes`: contexto o referencia no sensible de la evidencia.

Los métodos viven en `work_order_acceptance_method_catalog`:

- `IN_PERSON`;
- `WHATSAPP`;
- `PHONE`;
- `EMAIL`;
- `SIGNED_DOCUMENT`;
- `OTHER`.

El catálogo separado evita un ENUM rígido y permite ampliar métodos mediante una migración controlada
de datos de catálogo.

Reglas de integridad:

- Para estado `APPROVED`, aprobación interna y aceptación del cliente deben estar completas.
- `accepted_by_user_id` es opcional; `accepted_by_name` nunca lo es al aprobar.
- Los campos de aceptación deben ser todos nulos antes de registrar aceptación, excepto notas de
  preparación que deben vivir fuera de estos campos.
- La aceptación no autentica por sí misma al cliente; documenta el canal y la persona reportada.
- Cambiar aceptación después de aprobar no está permitido. Una corrección administrativa requiere un
  evento de auditoría o revisión sucesora, nunca sobrescritura silenciosa.

Ejemplo conceptual: un usuario interno registra que una persona aceptó por WhatsApp en una fecha
determinada y agrega una nota descriptiva. No es necesaria una cuenta del cliente ni una firma
digital formal para conservar esa metadata.

## 6. Modelo relacional propuesto

### 6.1 `work_orders`

Propósito: identidad y estado operativo del proceso de cotización.

Campos principales:

- `id_work_orders`;
- `vehicle_intake_id`;
- `status_id` del contexto operativo Work Orders;
- `current_revision_id` nullable durante la creación inicial;
- `final_approved_revision_id` nullable;
- `lock_version` para concurrencia optimista;
- `created_by_user_id`, `created_at`, `updated_at`.

Relaciones y constraints:

- FK obligatoria a Vehicle Intake.
- FKs de ambos punteros a revisiones.
- Integridad compuesta para garantizar que cada puntero pertenece a la misma Work Order.
- No contiene campos financieros ni detalles snapshot.
- Borrado restringido cuando existen revisiones; cancelación lógica para historia financiera.

Índices:

- Vehicle Intake;
- estado operativo;
- revisión vigente;
- aprobación final activa.

### 6.2 `work_order_revision_status_catalog`

Propósito: catálogo documental de estados de revisión.

Campos:

- `id_work_order_revision_status_catalog`;
- `code` único;
- `name`, `description`;
- auditoría temporal.

Contiene exactamente los seis estados definidos en este documento.

### 6.3 `work_order_acceptance_method_catalog`

Propósito: normalizar canales de aceptación.

Campos:

- `id_work_order_acceptance_method_catalog`;
- `code` único;
- `name`, `description`;
- auditoría temporal.

### 6.4 `work_order_revisions`

Propósito: snapshot inmutable de cada propuesta.

Campos principales:

- `id_work_order_revisions`;
- `work_order_id`;
- `revision_number`;
- `revision_status_id`;
- `technician_id`;
- fechas y horas estimadas;
- `subtotal_amount DECIMAL(19,4)`;
- `apply_iva`;
- `iva_rate DECIMAL(10,6)`;
- `iva_amount DECIMAL(19,4)`;
- `total_amount DECIMAL(19,4)`;
- `currency CHAR(3)`;
- `tax_treatment_notes` nullable;
- `technical_notes`, `customer_notes`;
- `change_reason`;
- `created_by_user_id`;
- `approved_by_user_id`, `approved_at`;
- `accepted_by_name`, `accepted_by_user_id`, `accepted_at`;
- `acceptance_method_id`, `acceptance_notes`;
- `created_at`, `workflow_updated_at`.

Constraints:

- UNIQUE (`work_order_id`, `revision_number`).
- UNIQUE auxiliar (`work_order_id`, `id_work_order_revisions`) para FKs compuestas de punteros.
- número de revisión positivo.
- importes y horas no negativos.
- entrega estimada no anterior al inicio.
- moneda de tres caracteres en mayúsculas.
- coherencia entre `apply_iva`, tasa, IVA y total.
- coherencia conjunta de aprobación y aceptación.
- FKs a Work Order, técnico, usuarios, estado y método de aceptación.
- eliminación restringida; las revisiones se conservan.

Índices:

- (`work_order_id`, `revision_number`) único;
- estado de revisión;
- técnico;
- autor y aprobador;
- fecha de creación y aprobación.

### 6.5 `work_order_revision_services`

Propósito: snapshot opcional de servicios planificados.

Campos:

- `id_work_order_revision_services`;
- `work_order_revision_id`;
- `line_number`;
- `service_id` nullable;
- `service_name_snapshot` obligatorio;
- `service_description_snapshot` nullable;
- `quantity DECIMAL(19,6)`;
- `unit_price_snapshot DECIMAL(19,4)`;
- `line_total_snapshot DECIMAL(19,4)`;
- `notes` nullable;
- `created_at`.

Constraints e índices:

- UNIQUE (`work_order_revision_id`, `line_number`).
- cantidad positiva; precio y total no negativos.
- FK de revisión con borrado restringido.
- FK nullable al catálogo de servicios; retirar un servicio del catálogo no elimina el snapshot.
- índice por `service_id` para trazabilidad de origen.

No se impone unicidad por `service_id`: una propuesta puede mostrar líneas separadas del mismo
servicio con condiciones o notas distintas.

### 6.6 `work_order_revision_parts`

Propósito: snapshot opcional de piezas planificadas.

Campos:

- `id_work_order_revision_parts`;
- `work_order_revision_id`;
- `line_number`;
- `part_id` nullable;
- `part_name_snapshot` obligatorio;
- `part_number_snapshot` nullable;
- `part_description_snapshot` nullable;
- `quantity DECIMAL(19,6)`;
- `unit_price_snapshot DECIMAL(19,4)`;
- `line_total_snapshot DECIMAL(19,4)`;
- `notes` nullable;
- `created_at`.

Aplican las mismas reglas de línea, conservación e índices que para servicios. El número snapshot es
nullable porque el catálogo v1 aún no posee un identificador comercial de pieza.

### 6.7 Relación futura con Jobs

`jobs` deberá incorporar `initial_approved_revision_id` como FK obligatoria e inmutable. Al crear un
Job, esa revisión debe:

- pertenecer a la Work Order correspondiente;
- estar `APPROVED`;
- coincidir con `final_approved_revision_id` en ese instante;
- tener aceptación y aprobación completas.

Para autorizaciones adicionales posteriores al inicio se propone `job_authorized_revisions`, con
unicidad por Job y revisión. Esta tabla conserva la revisión inicial y las ampliaciones sin convertir
costos estimados en costos reales.

### 6.8 Garantías de unicidad y concurrencia

Los punteros en `work_orders` garantizan una sola vigente y una sola aprobación final activa. Además:

- creación de revisión bloquea la Work Order y usa UNIQUE por número;
- aprobación bloquea la Work Order y la revisión candidata;
- se valida la versión `lock_version` para detectar escrituras concurrentes;
- el cambio de estado, metadata y puntero final ocurre en una transacción;
- un índice/constraint generado adicional puede reforzar la aprobación activa, pero no sustituye los
  punteros ni la transacción;
- los reintentos deben ser idempotentes y nunca incrementar dos veces el número de revisión.

## 7. Reglas de negocio finales

1. Crear una Work Order crea su revisión 1 en la misma transacción.
2. Una Work Order puede no tener líneas de servicio o pieza.
3. El subtotal manual es autoritativo; el total de detalles es informativo.
4. Todo cambio de contenido crea una revisión nueva.
5. Revisiones y detalles no se editan ni eliminan físicamente.
6. Solo estado y metadata de workflow cambian mediante operaciones explícitas.
7. Solo una revisión es vigente mediante el puntero de Work Order.
8. Solo la aprobación final activa puede iniciar un Job.
9. Aprobación exige aceptación del cliente y aprobación interna.
10. Una revisión rechazada o cancelada permanece consultable.
11. Cancelar Work Order bloquea revisiones y Jobs nuevos, pero conserva historia.
12. Los snapshots no se recalculan cuando cambia el catálogo.
13. IVA y moneda pertenecen a cada revisión.
14. Los costos reales y finales quedan fuera de Work Order.
15. Una revisión presentada al cliente se muestra con dos decimales y conserva los valores que
    sustentaron esa presentación.

## 8. Impacto en backend

### 8.1 Módulo Work Orders

- Separar `WorkOrder` de `WorkOrderRevision` y sus líneas snapshot.
- Retirar la edición financiera directa del PUT vigente.
- Limitar actualizaciones de Work Order a metadata operativa no versionada.
- Crear un servicio monetario de dominio basado exclusivamente en `BigDecimal`.
- Crear puertos para bloqueo, numeración, snapshots, estados y aceptación.

Casos de uso futuros:

- `CreateWorkOrderRevisionUseCase`;
- `ListWorkOrderRevisionsUseCase`;
- `GetCurrentWorkOrderRevisionUseCase`;
- `GetFinalApprovedWorkOrderRevisionUseCase`;
- `ApproveWorkOrderRevisionUseCase`;
- `RejectWorkOrderRevisionUseCase`;
- `CancelWorkOrderRevisionUseCase`;
- creación de revisión sucesora con servicios y piezas snapshot.

Los antiguos nombres `ReplaceRevisionServicesUseCase` y `ReplaceRevisionPartsUseCase` no deben
mutar una revisión existente. Si se conservan como intención de aplicación, su resultado debe ser
una revisión sucesora completa.

### 8.2 API futura

La versión incompatible debe publicarse bajo `/api/v2`. Contrato conceptual:

- crear Work Order con revisión inicial;
- listar revisiones;
- crear revisión sucesora;
- consultar vigente;
- consultar aprobación final activa;
- presentar, aprobar, rechazar o cancelar mediante comandos explícitos;
- consultar servicios y piezas de una revisión.

Los DTOs reciben subtotal, tratamiento IVA, moneda, planificación, notas y detalles opcionales. El
servidor calcula IVA, total y totales de línea. Ningún endpoint modifica snapshots existentes.

### 8.3 Validaciones y errores

Validaciones:

- escalas y rangos decimales;
- moneda y tasa;
- secuencia de fechas;
- estados y transiciones;
- referencias de catálogo opcionales con snapshot obligatorio;
- aceptación completa;
- pertenencia de punteros y revisión;
- concurrencia e idempotencia.

Errores nuevos:

- revisión inexistente o no perteneciente a la Work Order;
- revisión inmutable;
- transición inválida;
- aceptación incompleta;
- revisión no autorizada para Job;
- conflicto de concurrencia o numeración;
- duplicidad de línea;
- inconsistencias monetarias.

### 8.4 Tests

- Unitarios de cálculo decimal con IVA, sin IVA, límites y `HALF_UP`.
- Unitarios de transiciones, inmutabilidad y creación sucesora.
- Controller de roles, contratos y errores.
- Persistencia MySQL para constraints, FKs compuestas y snapshots.
- Concurrencia: numeración simultánea y aprobaciones competidoras.
- Idempotencia de creación/aprobación.
- Migración y backfill desde v1.
- Catálogos modificados después de cotizar sin alterar snapshots.
- Job rechazado si no usa la aprobación final activa.

### 8.5 Documentación

Después de aprobar el diseño SQL:

- actualizar `docs/work-orders.md` para el API v2;
- crear `docs/work-order-revisions.md`;
- conservar la documentación v1 claramente versionada;
- documentar fórmulas, escalas, transiciones y evidencia de aceptación.

## 9. Impacto en database/v2

### 9.1 Migraciones necesarias

El diseño SQL posterior deberá:

1. crear catálogos de estado de revisión y método de aceptación;
2. adaptar `work_orders` como agregado sin importes mutables;
3. crear revisiones y detalles snapshot;
4. agregar punteros e integridad compuesta;
5. agregar checks, índices y control de concurrencia;
6. preparar la futura FK de Job a revisión aprobada;
7. incluir scripts separados de validación y rollback no destructivo cuando sea posible.

### 9.2 Backfill conceptual desde v1

- Cada Work Order v1 genera revisión 1.
- Importes v1 se convierten a escala 4 sin inventar precisión.
- `estimated_iva > 0` permite inferir `apply_iva=true`; IVA cero es ambiguo y requiere marca de
  revisión manual.
- Las líneas se copian con nombres y descripciones actuales como snapshots migrados.
- El estado v1 se mapea conservadoramente; `APROBADO` no crea automáticamente aprobación final si no
  existe evidencia de aceptación.
- Autor, aprobador y aceptación desconocidos permanecen nulos y se identifican como datos migrados
  pendientes de conciliación.
- No se fabrican fechas, personas ni canales de aceptación.

### 9.3 Riesgos

- Ambigüedad histórica de IVA cero y aprobaciones v1.
- Nombres históricos no recuperables si el catálogo cambió antes del backfill.
- Dependencias circulares controladas entre Work Order y revisiones por sus punteros.
- Diferencias de redondeo entre datos antiguos y reglas v2.
- Periodo de convivencia entre API v1 y v2.
- Implementar Jobs antes de completar la migración rompería trazabilidad.

## 10. Plan de implementación sugerido

1. Traducir este documento a diseño SQL detallado para `database/v2`.
2. Revisar DDL, constraints, orden de creación, datos de catálogo y rollback.
3. Diseñar y probar backfill v1 → v2 sin ejecutarlo sobre la base vigente.
4. Adaptar el dominio Work Orders y el cálculo monetario.
5. Publicar endpoints v2 de Work Order y revisiones.
6. Implementar comandos de presentación, aceptación, aprobación, rechazo y cancelación.
7. Incorporar snapshots opcionales de servicios y piezas desde la creación de revisión.
8. Ejecutar pruebas MySQL, concurrencia, seguridad y migración.
9. Solo entonces diseñar Jobs con revisión aprobada inicial y autorizaciones adicionales.

## 11. Decisiones pendientes reales

Las decisiones funcionales y monetarias centrales quedan cerradas. Permanecen pendientes únicamente:

- política legal y periodo de conservación de evidencia de aceptación;
- si se almacenarán archivos de evidencia y dónde, sin guardar binarios pesados en tablas operativas;
- formato, firma y conservación de PDF por revisión;
- roles exactos autorizados para aprobación interna;
- definición contable/fiscal validada para casos donde `apply_iva=false`;
- estrategia de coexistencia y fecha de retiro del API v1;
- detalle definitivo de autorizaciones adicionales cuando Jobs sea diseñado.
