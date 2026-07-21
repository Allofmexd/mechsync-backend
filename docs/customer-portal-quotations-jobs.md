# Customer Portal quotations and Jobs

## Visible quotation

`GET /api/v1/customer-portal/work-orders/{workOrderId}/quotation` first validates Work Order
ownership. It returns the final revision only when it is `APPROVED`; otherwise it returns the
current revision only when it is `SENT`. DRAFT, REJECTED, SUPERSEDED and CANCELLED revisions are
indistinguishable from an unavailable quotation (404).

The response uses `BigDecimal` amounts and immutable service/part snapshots. It omits actor IDs,
internal reasons, lock versions and mutable catalog references. The portal cannot accept, reject or
edit a quotation.

## Jobs

* `GET /api/v1/customer-portal/jobs?page&size&vehicleId&workOrderId`
* `GET /api/v1/customer-portal/jobs/{jobId}`

Queries join Job -> Work Order -> Intake -> Vehicle -> Customer. Responses contain customer-facing
status, dates, vehicle, technician display name/specialty, and service/part names and quantities.
They exclude staff contact details, execution notes, acquisition costs and provisional Job amounts.
The Service Report remains the authoritative final economic source and will be implemented in
Phase D.
