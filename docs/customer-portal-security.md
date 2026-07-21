# Customer Portal security foundation

## Scope

This document describes the security foundation for the read-only Customer Portal. Profile,
vehicles, operational history, visible quotations, and Jobs are exposed only through dedicated
endpoints under `/api/v1/customer-portal`.

## Authenticated Customer resolution

`ResolveAuthenticatedCustomerUseCase` resolves the current Customer without accepting a user ID,
customer ID, role, or ownership information from a request. The resolver obtains the validated JWT
principal through `CurrentAuthenticatedUserPort` and follows this chain:

```text
JWT AuthenticatedUser.id -> customers.user_id -> customers.id_customers
```

Only a principal whose validated JWT authorities contain `CLIENTE` can be resolved. The returned
contract contains only `customerId`; it does not expose the User, roles, contact data, or a JPA
entity.

## Error behavior

* Missing or invalid authentication remains the responsibility of Spring Security and returns 401.
* ADMINISTRADOR or TECNICO access to a Customer Portal use case is forbidden with 403.
* A CLIENTE account without a Customer profile returns 403 with a controlled support message.
* A duplicated or inconsistent User-Customer relation returns a controlled 500 response without
  SQL, table, column, or identifier details.
* Future lookups must return 404 for both foreign and nonexistent resources so resource existence is
  not disclosed.

## Customer and role integrity

Administrative Customer creation requires an existing User with role `CLIENTE` and without another
Customer profile. It does not create Users or assign roles automatically. A User with an associated
Customer cannot be changed to a non-CLIENTE role; the operation returns 409 and preserves both
records.

The database unique constraint on `customers.user_id` remains the authoritative duplicate guard.

## Ownership rules

Repository queries enforce ownership directly through parameterized joins:

```text
Customer:      customers.user_id = authenticated user ID
Vehicle:       vehicles.customer_id = resolved customer ID
Intake:        vehicle_intakes -> vehicles -> customer
Work Order:    work_orders -> vehicle_intakes -> vehicles -> customer
Revision:      work_order_revisions -> work_orders -> vehicle_intakes -> vehicles -> customer
Job:           jobs -> work_orders -> vehicle_intakes -> vehicles -> customer
Report/PDF:    service_reports -> jobs -> work_orders -> vehicle_intakes -> vehicles -> customer
```

Applications must not download global collections, filter ownership in Java or React, or accept a
Customer/User identifier supplied by the frontend. Lists must use server-side pagination with
ownership applied to both data and count queries.

## MVP decisions and pending work

The portal remains read-only. Profile, owned vehicles, Intakes, Work Orders, visible quotations,
Jobs and a projected timeline are implemented. Service Reports and PDF remain pending for Phase D.
Direct quotation acceptance, public tracking and write operations are outside this scope.

Dedicated DTOs exclude internal notes, staff contact data, actor IDs, security metadata,
acquisition costs, margins, draft/rejected/superseded revisions and other workshop information.
Unknown internal statuses are mapped to a safe customer-facing label.
