# Customer Portal operational history

## Endpoints

All endpoints require `ROLE_CLIENTE`, resolve the Customer from the validated JWT, accept no
Customer/User identifier, and return 404 for both foreign and missing resources.

* `GET /api/v1/customer-portal/vehicle-intakes?page&size&vehicleId`
* `GET /api/v1/customer-portal/vehicle-intakes/{intakeId}`
* `GET /api/v1/customer-portal/work-orders?page&size&vehicleId&intakeId`
* `GET /api/v1/customer-portal/work-orders/{workOrderId}`
* `GET /api/v1/customer-portal/history?page&size&vehicleId`

Lists use zero-based server pagination (`size` 1..100). Optional resource filters are verified as
owned before the query. Data and count queries apply ownership in SQL.

## Timeline

The timeline is a read-only SQL projection; no history table or duplicated event is persisted. A
single `UNION ALL` produces globally ordered, paginated events: `VEHICLE_INTAKE`, `WORK_ORDER`,
`QUOTATION_AVAILABLE`, `QUOTATION_APPROVED`, `JOB_STARTED`, and `JOB_COMPLETED`. Ordering is
`event_date DESC, event_type, event_id DESC`.

Internal Intake observations, technician identifiers, technical notes, status IDs, actor IDs and
legacy Work Order financial fields are never selected. Service Report events are reserved for
Phase D.
