# Auditoría de versionado de cotizaciones en Work Orders

## 1. Contexto

MechSync separa tres conceptos que no deben mezclarse:

- **Work Order:** diagnóstico, planificación y cotización del servicio propuesto.
- **Job:** trabajo realmente ejecutado a partir de una Work Order.
- **Service Report:** cierre oficial, entrega y costos finales del servicio.

La base v1 y el backend ya implementan el CRUD principal de Work Orders. Esta auditoría evalúa si
ese diseño puede conservar revisiones, snapshots financieros, IVA opcional e historial auditable.
No se modificó el esquema ni el código durante el análisis.

Las decisiones de negocio confirmadas son:

- El subtotal cotizado se captura manualmente.
- Los servicios y piezas son detalles opcionales; no determinan obligatoriamente el subtotal.
- Cambiar costos o condiciones debe producir una nueva revisión y nunca sobrescribir el histórico.
- El IVA es opcional; cuando aplica usa 16 %, con redondeo monetario a dos decimales.
- Los precios de catálogo son valores vivos; una cotización debe conservar sus propios snapshots.
- Los costos reales pertenecen a Job o Service Report.
- El cliente y el vehículo se obtienen desde Vehicle Intake; Work Order no los duplica.

## 2. Estado actual de v1

### 2.1 Tablas y responsabilidades

`work_orders` contiene una sola representación mutable de la cotización:

- identidad y relación: `id_work_orders`, `vehicle_intake_id`;
- responsable: `technician_id`;
- planificación: `work_order_date`, `estimated_start_date`, `estimated_delivery_date`,
  `estimated_hours`;
- importes: `estimated_subtotal`, `estimated_iva`, `estimated_total`;
- contexto: `technical_observations`, `status_id`;
- auditoría técnica mínima: `created_at`, `updated_at`.

`work_order_services` y `work_order_parts` guardan cantidades, precio unitario estimado y subtotal de
línea. Sus claves únicas impiden repetir el mismo elemento dentro de una Work Order. Ambas tablas
referencian el catálogo vivo (`services` o `parts`) y la Work Order directamente.

`jobs`, `job_services` y `job_parts` tienen campos `actual_*`, por lo que v1 sí diferencia valores
estimados de valores ejecutados. `service_reports` tiene importes `final_*` y confirmación del
cliente, preservando conceptualmente el cierre oficial.

Los estados existentes son:

- Work Orders: `PENDIENTE`, `APROBADO`, `RECHAZADO`, `EN_PROCESO`, `CANCELADO`.
- Jobs: `EN_PROCESO`, `COMPLETADO`, `CANCELADO`.
- Service Reports: `PENDIENTE`, `COMPLETADO`, `ENTREGADO`, `CANCELADO`.

### 2.2 Capacidades que v1 sí ofrece

- Una Work Order parte de un Vehicle Intake y no duplica `customer_id` ni `vehicle_id`.
- Permite cotizaciones sin servicios o piezas.
- Admite subtotal, IVA y total capturados manualmente.
- Conserva precio y subtotal de cada línea, aunque el precio del catálogo cambie después.
- Mantiene separación nominal entre importes estimados, reales y finales.
- Aplica FKs, importes no negativos, cantidades positivas y borrado restringido.

### 2.3 Comportamiento actual del backend

El dominio, DTOs y entidad JPA reflejan una sola cotización. `PUT /api/v1/work-orders/{id}` actualiza
directamente fechas, técnico, importes, observaciones y estado sobre la misma fila. `updated_at`
indica que hubo una modificación, pero no conserva el valor anterior, autor, motivo ni transición.

El backend valida referencias, importes no negativos y orden de fechas. No valida una fórmula de
IVA, no distingue IVA no aplicable de IVA aplicado con monto cero y no bloquea la edición de una
Work Order aprobada.

## 3. Brecha identificada

| Necesidad | Soporte v1 | Brecha |
|---|---|---|
| Múltiples revisiones | No | Solo existe una fila mutable por Work Order. |
| Revisión actual | No | No hay puntero, número ni indicador único. |
| Revisión aprobada | Parcial | `status_id=APROBADO` aplica a la Work Order, no identifica qué contenido fue aprobado. |
| Subtotal por revisión | No | El subtotal anterior se pierde al actualizar. |
| IVA opcional | No | `estimated_iva` es obligatorio; cero resulta ambiguo. |
| Tasa histórica de IVA | No | No se almacena tasa. |
| Total histórico | No | Solo queda el último total. |
| Motivo del cambio | No | No existe campo ni historial. |
| Autor/aprobador | No | No hay FKs a usuario creador o aprobador. |
| Bloqueo de edición aprobada | No | El PUT actual sobrescribe sin política de inmutabilidad. |
| Snapshot monetario de líneas | Parcial | Precio/cantidad quedan, pero nombre y descripción dependen del catálogo vivo. |
| Snapshot documental de pieza | Parcial | No se preservan nombre, descripción ni referencia propia en la línea. |
| Costos reales separados | Sí, conceptual | Jobs y reportes tienen campos reales/finales, pero Job no referencia la revisión aprobada. |

### 3.1 Riesgos de continuar únicamente con v1

- Una cotización aprobada puede cambiar sin evidencia del valor aceptado.
- `updated_at` no es un historial: no permite reconstruir el antes y después.
- Un cambio de nombre o descripción del catálogo altera la representación documental histórica.
- `estimated_iva = 0` no diferencia exención/no aplicación, inclusión previa o redondeo a cero.
- Un Job referencia solo `work_order_id`; si la cotización cambia, no se sabe qué revisión originó
  la ejecución.
- La aplicación podría implementar copias o auditoría en memoria, pero sin persistencia transaccional
  no garantiza trazabilidad ante fallos, concurrencia o accesos alternos a la base.

Las validaciones de fórmulas, permisos y transiciones pueden vivir en backend, pero revisiones,
snapshots, autores y aprobaciones requieren persistencia nueva.

## 4. Alternativas evaluadas

### Alternativa A: mantener v1 sin migraciones

Consiste en conservar `work_orders`, `work_order_services` y `work_order_parts`, usando `updated_at`
y disciplina de aplicación.

| Criterio | Evaluación |
|---|---|
| Trazabilidad | Muy baja; no reconstruye versiones. |
| Complejidad | Baja. |
| Riesgo sobre lo existente | Bajo técnicamente, alto para el negocio. |
| Compatibilidad con backend | Total. |
| Limpieza arquitectónica | Insuficiente para cotizaciones auditables. |
| Jobs/Service Reports futuros | Ambigua la cotización de origen. |
| Adecuación al negocio | No cumple las decisiones confirmadas. |

Los precios de línea funcionan como snapshot monetario parcial, pero no documental. Esta alternativa
solo sería aceptable para un prototipo sin aprobaciones ni obligación de auditoría.

### Alternativa B: migración incremental v1.x

Agrega `work_order_revisions` y tablas de detalle por revisión, manteniendo temporalmente las columnas
financieras actuales. Un backfill crea la revisión 1 de cada Work Order existente. Luego el backend
cambia a escritura dual o pasa directamente a las nuevas tablas.

| Criterio | Evaluación |
|---|---|
| Trazabilidad | Alta después de completar el backfill y cortar escrituras antiguas. |
| Complejidad | Media/alta por coexistencia de dos modelos. |
| Riesgo sobre lo existente | Medio; exige migración y estrategia de transición. |
| Compatibilidad con backend | Parcial; requiere refactor coordinado. |
| Limpieza arquitectónica | Buena al finalizar, confusa durante la transición. |
| Jobs/Service Reports futuros | Buena si Jobs referencia la revisión aprobada. |
| Adecuación al negocio | Cumple si se elimina la edición directa del modelo antiguo. |

Es viable si v1 ya contiene datos productivos que deban migrarse con mínima interrupción.

### Alternativa C: crear database/v2

Mantiene v1 estable y define desde el inicio Work Order como agregado estable con revisiones
inmutables y detalles snapshot. Incluye una estrategia explícita para importar datos v1 cuando sea
necesario.

| Criterio | Evaluación |
|---|---|
| Trazabilidad | Alta y nativa. |
| Complejidad | Alta inicialmente, menor complejidad accidental posterior. |
| Riesgo sobre lo existente | Bajo: v1 permanece intacta. |
| Compatibilidad con backend | Requiere refactor versionado del módulo. |
| Limpieza arquitectónica | La mejor de las alternativas. |
| Jobs/Service Reports futuros | Define una referencia inequívoca a la revisión aprobada. |
| Adecuación al negocio | Completa. |

## 5. Recomendación

Se recomienda **Alternativa C: database/v2**, acompañada de un proceso explícito de migración desde
v1. Las reglas confirmadas convierten el versionado en parte central del dominio, no en una mejora
cosmética. Diseñarlo en v2 evita mantener simultáneamente dos fuentes financieras dentro del mismo
esquema operativo.

Si existiera una obligación inmediata de conservar datos productivos en la misma base, la segunda
opción es B, pero debe tratarse como migración por etapas con fecha de retiro de las columnas v1.
No se recomienda A para uso real.

## 6. Modelo propuesto para v2

### 6.1 `work_orders`

Representa la identidad y ciclo operativo, no el snapshot financiero:

- `id_work_orders` PK.
- `vehicle_intake_id` FK obligatoria.
- `status_id` para el estado general operativo.
- `current_revision_id` FK nullable a la revisión vigente.
- `approved_revision_id` FK nullable a la revisión aprobada.
- `created_at`, `updated_at`.

El técnico, fechas estimadas, condiciones, importes y notas que puedan cambiar entre cotizaciones
deben vivir en la revisión. Los punteros permiten que la revisión vigente y la aprobada sean distintas
mientras una nueva propuesta está en evaluación.

Para impedir que un puntero apunte a una revisión de otra Work Order se recomienda una FK compuesta
o una validación equivalente robusta. La creación requiere una transacción: crear Work Order, crear
revisión inicial y actualizar el puntero vigente.

### 6.2 `work_order_revision_status_catalog`

Catálogo dedicado para no mezclar el estado operativo de Work Order con el estado documental de la
revisión. Valores iniciales sugeridos: `BORRADOR`, `PENDIENTE_APROBACION`, `APROBADA`, `RECHAZADA` y
`SUPERSEDIDA`.

### 6.3 `work_order_revisions`

Convención de nombres alineada con v1:

- `id_work_order_revisions` PK.
- `work_order_id` FK obligatoria.
- `revision_number` entero positivo.
- `revision_status_id` FK al catálogo de revisiones.
- `technician_id` FK obligatoria.
- `estimated_start_date`, `estimated_delivery_date`, `estimated_hours`.
- `subtotal_amount DECIMAL(12,2)` capturado manualmente.
- `apply_iva BOOLEAN` obligatorio.
- `iva_rate DECIMAL(5,4)`; `0.1600` cuando aplica y `0.0000` cuando no aplica.
- `iva_amount DECIMAL(12,2)`.
- `total_amount DECIMAL(12,2)`.
- `currency CHAR(3)` con valor inicial `MXN`.
- `technical_observations`.
- `change_reason` obligatorio a partir de la revisión 2.
- `created_by_user_id` FK obligatoria.
- `approved_by_user_id` FK nullable.
- `approved_at` nullable.
- `created_at` y, solo mientras sea borrador, `updated_at`.

Restricciones recomendadas:

- UNIQUE (`work_order_id`, `revision_number`).
- importes no negativos y `revision_number > 0`.
- `estimated_delivery_date >= estimated_start_date` cuando ambas existan.
- si `apply_iva=false`: tasa y monto son cero, total igual a subtotal;
- si `apply_iva=true`: tasa `0.1600`, IVA `ROUND(subtotal_amount * iva_rate, 2)` y total
  `ROUND(subtotal_amount + iva_amount, 2)`;
- aprobación exige aprobador y fecha; los tres campos deben ser nulos en estados no aprobados;
- una revisión deja de ser editable al salir de `BORRADOR`.

La regla “una sola aprobada/vigente” se expresa mediante `approved_revision_id` y
`current_revision_id` en `work_orders`, actualizados dentro de una transacción con bloqueo de la fila
padre. Si se exige además unicidad física por estado, debe evaluarse una columna generada e índice
único compatible con MySQL 8.

### 6.4 `work_order_revision_services`

- `id_work_order_revision_services` PK.
- `work_order_revision_id` FK obligatoria.
- `service_id` FK nullable, con `ON DELETE SET NULL` si se permite retirar catálogos.
- `service_name_snapshot VARCHAR(150)` obligatorio.
- `service_description_snapshot TEXT` nullable.
- `quantity DECIMAL(10,2)` positiva.
- `unit_price_snapshot DECIMAL(12,2)` no negativo.
- `line_total_snapshot DECIMAL(12,2)` no negativo.
- `notes` nullable.
- `created_at`.

### 6.5 `work_order_revision_parts`

- `id_work_order_revision_parts` PK.
- `work_order_revision_id` FK obligatoria.
- `part_id` FK nullable.
- `part_name_snapshot VARCHAR(150)` obligatorio.
- `part_description_snapshot TEXT` nullable.
- `part_reference_snapshot VARCHAR(100)` nullable, si se incorpora una referencia/SKU al catálogo.
- `quantity DECIMAL(10,2)` positiva.
- `unit_price_snapshot DECIMAL(12,2)` no negativo.
- `line_total_snapshot DECIMAL(12,2)` no negativo.
- `notes` nullable.
- `created_at`.

Los detalles son soporte documental opcional. La suma de líneas no tiene que igualar
`subtotal_amount`; si difiere, no es un error porque el subtotal manual es autoritativo. Conviene
mostrar ambos valores en la API para hacer visible la diferencia.

### 6.6 Relación futura con Jobs

Para trazabilidad, Job debe referenciar `work_order_revision_id` de la revisión aprobada que autorizó
el trabajo. A través de esa revisión se obtiene la Work Order, por lo que no es necesario duplicar
`work_order_id` salvo que exista una razón de rendimiento documentada. Crear un Job debe fallar si la
revisión no está aprobada.

Job conserva horas, piezas, servicios e importes realmente ejecutados. Service Report conserva los
importes finales y cierre; ninguno modifica la revisión histórica.

## 7. Reglas de negocio propuestas

1. Una Work Order puede existir sin detalles y debe tener al menos una revisión para cotizar.
2. El subtotal de la revisión se captura manualmente y es la fuente financiera autoritativa.
3. Servicios y piezas son snapshots opcionales de soporte.
4. Crear o cambiar costos, condiciones, técnico, fechas o detalles genera una nueva revisión.
5. Las revisiones enviadas, rechazadas, aprobadas o reemplazadas son inmutables.
6. Una nueva revisión se crea copiando la anterior y aplicando cambios en una transacción.
7. `change_reason` es obligatorio desde la revisión 2.
8. Si no aplica IVA: tasa y monto son cero; total igual a subtotal.
9. Si aplica IVA: tasa 16 %, monto y total redondeados a dos decimales con una política única
   (`HALF_UP` propuesta, pendiente de confirmación contable).
10. La aprobación registra usuario y fecha y actualiza el puntero aprobado atómicamente.
11. Una revisión nueva no cambia silenciosamente la revisión previamente aprobada.
12. Jobs nacen exclusivamente de una revisión aprobada y no alteran la cotización.
13. Los costos reales y finales pertenecen a Job y Service Report.

## 8. Impacto en backend

### Dominio y aplicación

- Reducir `WorkOrder` a identidad/ciclo y agregar `WorkOrderRevision`, `WorkOrderRevisionServiceItem`
  y `WorkOrderRevisionPartItem`.
- Evitar que `UpdateWorkOrderCommand` modifique importes históricos.
- Agregar casos de uso:
  - `CreateWorkOrderRevisionUseCase`;
  - `ListWorkOrderRevisionsUseCase`;
  - `GetCurrentWorkOrderRevisionUseCase`;
  - `ApproveWorkOrderRevisionUseCase`;
  - `ReplaceRevisionServicesUseCase`;
  - `ReplaceRevisionPartsUseCase`.
- Agregar puertos de persistencia para revisiones, snapshots, bloqueo del agregado y lectura de
  catálogos.
- Centralizar cálculo monetario y redondeo en un servicio de dominio probado.

### Persistencia

- Nuevas entidades y repositorios para las tres tablas de revisión.
- Transacciones para crear revisión, reemplazar detalles y aprobar.
- Bloqueo pesimista u optimistic locking para evitar dos números de revisión o aprobaciones
  concurrentes.
- Backfill de v1: cada Work Order se convierte en revisión 1; las líneas se copian con nombre y
  descripción actuales, registrando que son valores migrados.

### Web/API

El POST de Work Order podría crear agregado y revisión inicial. El PUT financiero actual debe
retirarse o limitarse a metadatos no versionados. Endpoints conceptuales:

- `POST /api/v2/work-orders`;
- `GET /api/v2/work-orders/{id}/revisions`;
- `POST /api/v2/work-orders/{id}/revisions`;
- `GET /api/v2/work-orders/{id}/revisions/current`;
- `POST /api/v2/work-orders/{id}/revisions/{revisionId}/approve`;
- `PUT /api/v2/work-orders/{id}/revisions/{revisionId}/services`;
- `PUT /api/v2/work-orders/{id}/revisions/{revisionId}/parts`.

Los DTOs deben aceptar `applyIva`, no aceptar montos calculados contradictorios o definir claramente
si el servidor los calcula, y nunca permitir editar una revisión no borrador.

### Errores y pruebas

Agregar errores de revisión inexistente, estado inválido, revisión inmutable, conflicto concurrente,
aprobación inválida, catálogo inexistente y duplicados en detalles.

Las pruebas deben cubrir fórmulas con IVA y sin IVA, redondeo, revisión incremental, snapshots ante
cambios de catálogo, aprobaciones concurrentes, autorización, inmutabilidad, backfill y creación de
Job solo desde la revisión aprobada.

Actualizar `docs/work-orders.md` después de aprobar el contrato v2; este documento no cambia el API
vigente.

## 9. Riesgos

- **Migración:** v1 no registra si IVA cero significaba “no aplica”; el backfill necesita una regla
  explícita y posiblemente revisión manual.
- **Snapshots históricos:** nombre y descripción anteriores al backfill no pueden reconstruirse si el
  catálogo ya cambió.
- **Doble fuente:** mantener columnas financieras en Work Order y revisiones durante mucho tiempo
  produciría inconsistencias.
- **Concurrencia:** sin bloqueo, dos procesos pueden crear el mismo número de revisión o aprobar dos
  revisiones.
- **Redondeo:** backend, MySQL, documentos y frontend deben usar la misma escala y modo.
- **Aprobación:** falta definir evidencia de aceptación del cliente y su valor legal.
- **Jobs:** implementar Jobs antes de fijar la referencia a revisión aprobada perpetuaría una brecha
  de trazabilidad.
- **Borrado:** cotizaciones y revisiones aprobadas deberían conservarse; conviene cancelación lógica,
  no borrado físico, cuando exista historia financiera.

## 10. Plan de implementación sugerido

1. Cerrar decisiones pendientes de aprobación, redondeo, moneda y evidencia del cliente.
2. Diseñar `database/v2`, DDL, constraints, índices, scripts de validación y rollback seguro.
3. Definir y probar la migración/backfill desde v1 con un conjunto de datos representativo.
4. Refactorizar Work Orders para separar agregado y revisiones; retirar edición financiera directa.
5. Implementar creación/listado de revisiones y snapshots opcionales de servicios/piezas.
6. Implementar aprobación transaccional, punteros vigente/aprobada y control de concurrencia.
7. Actualizar API/documentación y ejecutar pruebas unitarias, integración MySQL y migración.
8. Solo después diseñar Jobs referenciando la revisión aprobada.

## 11. Decisiones pendientes

- Confirmar nombres finales y si el API nuevo será `/api/v2` o mantendrá `/api/v1` con cambio
  incompatible coordinado.
- Confirmar `current_revision_id` y `approved_revision_id` en Work Order frente a una estrategia de
  indicadores únicos en revisiones.
- Confirmar si solo puede existir una revisión aprobada histórica o si varias pueden conservar estado
  “aprobada” mientras una sola es la aprobación vigente.
- Confirmar modo de redondeo contable (`HALF_UP` propuesto).
- Confirmar moneda única MXN o soporte multimoneda.
- Definir firma, aceptación digital o evidencia de aprobación del cliente.
- Definir si se requiere generar y conservar PDF de cada revisión.
- Definir tratamiento del IVA para importes que ya lo incluyen (`ivaIncluded`) frente a subtotal más
  IVA (`applyIva`); no deben coexistir banderas ambiguas.
- Definir política de retención, cancelación y prohibición de borrado de cotizaciones aprobadas.
