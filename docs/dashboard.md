# Dashboard administrativo

## Alcance y seguridad

El Dashboard es de solo lectura y se publica bajo `/api/v1/dashboard`. Todos sus endpoints exigen
rol `ADMINISTRADOR`; un usuario autenticado con rol `TECNICO` o `CLIENTE` recibe `403` y una
solicitud anónima recibe `401`. No acepta identificadores de usuario, técnico o cliente.

## Periodo

Los endpoints aceptan `from` y `to` en formato `YYYY-MM-DD`. `to` es inclusivo: internamente las
consultas usan el inicio de `from` y el inicio del día posterior a `to` como límite exclusivo. Sin
fechas se usa desde el primer día del mes actual hasta hoy. El rango máximo es de 731 días
calendario inclusivos; formatos inválidos, `from > to` o rangos mayores responden `400`.

## Definición de métricas

- **Clientes registrados:** todos los registros de `customers`; no existe baja lógica.
- **Vehículos registrados:** todos los registros de `vehicles`; no existe baja lógica.
- **Ingresos abiertos:** ingresos cuyo estado `VEHICLE_INTAKES` no sea `COMPLETADO` ni
  `CANCELADO`.
- **Work Orders activas:** órdenes en `PENDIENTE`, `APROBADO` o `EN_PROCESO`; se excluyen
  `RECHAZADO` y `CANCELADO`.
- **Jobs en proceso:** Jobs cuyo estado `JOBS` sea `EN_PROCESO`.
- **Ingresos del periodo:** suma de `service_reports.final_total` únicamente para reportes
  `ENTREGADO`, usando `delivered_at`. Cotizaciones, Jobs y reportes no entregados no se consideran
  ingreso final. La moneda es `MXN` y Java mantiene `BigDecimal`.
- **Work Orders por estado:** conteo por estado de órdenes cuya `work_order_date` cae en el rango.
- **Jobs por estado:** conteo por estado de Jobs creados en el rango mediante `created_at`.
- **Ingresos por mes:** la misma suma final agrupada cronológicamente por el mes de `delivered_at`;
  los meses sin ingresos se devuelven con cero.
- **Servicios más realizados:** suma de `job_services.quantity` para Jobs `COMPLETADO`, filtrados
  por `completion_date`. El nombre mostrado es el nombre actual del catálogo `services`.
- **Carga por técnico:** estado actual de los Jobs creados en el rango, con total, en proceso y
  completados. Solo expone Technician ID y nombre completo.

Las cards operativas, salvo ingresos, son una fotografía actual y no se restringen por periodo.

## Endpoints

| Método | Endpoint |
|---|---|
| GET | `/api/v1/dashboard/summary?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| GET | `/api/v1/dashboard/work-orders-by-status?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| GET | `/api/v1/dashboard/jobs-by-status?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| GET | `/api/v1/dashboard/revenue-by-month?from=YYYY-MM-DD&to=YYYY-MM-DD` |
| GET | `/api/v1/dashboard/top-services?from=YYYY-MM-DD&to=YYYY-MM-DD&limit=5` |
| GET | `/api/v1/dashboard/technician-workload?from=YYYY-MM-DD&to=YYYY-MM-DD` |

Las respuestas usan el envelope estándar `{ "status": "OK", "data": ... }`. Las colecciones sin
datos responden `200` y lista vacía; la serie mensual completa los meses sin ingreso con cero.

## Implementación

Las consultas usan `COUNT`, `SUM`, `GROUP BY`, `ORDER BY` y `LIMIT` directamente en MySQL mediante
parámetros de `NamedParameterJdbcTemplate`. No cargan entidades completas, no agregan registros en
React y no ejecutan consultas por estado o por técnico. La agrupación mensual usa funciones MySQL;
la suite automatizada cubre contrato, periodo, use case y estructura/parametrización SQL, pero no
levanta una instancia MySQL efímera porque el proyecto no dispone de Testcontainers.
