# Customer Portal: profile and vehicles

## Scope

This read-only phase exposes only the authenticated Customer profile and owned vehicles. It does not
expose service history, intakes, Work Orders, quotations, Jobs, Service Reports, PDFs, or acceptance
operations.

All endpoints require `CLIENTE` and resolve the Customer from the validated JWT. Requests never
accept a User or Customer identifier.

## Endpoints

### `GET /api/v1/customer-portal/profile`

Returns `customerId`, first and last name, email, optional phone, and optional address. The response
does not include `userId`, roles, password data, or JPA entities.

### `GET /api/v1/customer-portal/vehicles?page=0&size=20`

Returns a zero-based page with `content`, `page`, `size`, `totalElements`, and `totalPages`. Size must
be between 1 and 100. Data and count queries both use the resolved `customerId`. Results are ordered
by `created_at DESC, id_vehicles DESC`.

List entries contain vehicle ID, brand, model, year, color, plate, current mileage, a description,
and a masked VIN. A null or blank VIN remains null. For a nonempty VIN, only the final four
characters remain visible; VINs of four characters or fewer are fully masked.

### `GET /api/v1/customer-portal/vehicles/{vehicleId}`

The database query includes both vehicle ID and the resolved Customer ID. Owned vehicles return the
full VIN. Foreign and nonexistent vehicles return the same 404 response and cannot be distinguished.

## Security and errors

* Missing JWT: 401 from Spring Security.
* ADMINISTRADOR or TECNICO: 403.
* CLIENTE without Customer: 403 with the controlled profile message.
* Invalid pagination or nonpositive vehicle ID: 400.
* Foreign or nonexistent vehicle: 404.
* Unexpected failure: controlled 500 without SQL or stack traces in the response.

The portal controller uses method security, while every use case also invokes
`ResolveAuthenticatedCustomerUseCase`. Authorization is not delegated to the frontend.
