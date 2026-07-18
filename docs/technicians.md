# Perfiles Technician

## Alcance

El módulo permite listar, consultar, crear y actualizar perfiles operativos Technician. El perfil
vincula un usuario existente con rol `TECNICO` a una especialidad del catálogo `specialties` y lo
habilita para asignaciones en Vehicle Intakes, Work Orders, revisiones y Jobs.

No se crean usuarios ni se modifican roles automáticamente. Tampoco existe `DELETE` de Technician
en esta fase.

## Endpoints

| Método | Ruta | ADMINISTRADOR | TECNICO | CLIENTE |
|---|---|---:|---:|---:|
| GET | `/api/v1/technicians` | Sí | Sí | No |
| GET | `/api/v1/technicians/{id}` | Sí | No | No |
| POST | `/api/v1/technicians` | Sí | No | No |
| PUT | `/api/v1/technicians/{id}` | Sí | No | No |

Todas las rutas requieren JWT Bearer. Sin token responden `401`; un rol sin permiso recibe `403`.

## Crear perfil

```http
POST /api/v1/technicians
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "userId": 7,
  "specialtyId": 1,
  "hireDate": "2026-07-18"
}
```

Reglas:

- `userId` y `specialtyId` son obligatorios y positivos.
- El usuario debe existir y tener el rol `TECNICO`.
- Un usuario solo puede tener un perfil Technician.
- La especialidad debe existir.
- `hireDate` es opcional.

La respuesta es `201 Created`, incluye `Location: /api/v1/technicians/{id}` y nunca expone
`password_hash`.

## Consultar y actualizar

`GET /api/v1/technicians/{id}` devuelve `404` si el perfil no existe.

```http
PUT /api/v1/technicians/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

```json
{
  "specialtyId": 2,
  "hireDate": "2026-07-18"
}
```

El `userId` es la identidad inmutable del perfil y no forma parte del request de actualización.
Solo se pueden actualizar la especialidad y la fecha de contratación.

## Respuesta

Los IDs del ejemplo son ilustrativos y deben obtenerse del ambiente consultado.

```json
{
  "status": "OK",
  "data": {
    "id": 1,
    "userId": 7,
    "firstName": "Tecnico",
    "lastName": "QA",
    "fullName": "Tecnico QA",
    "email": "tecnico.qa@mechsync.local",
    "phone": "9610000000",
    "specialtyId": 1,
    "specialtyCode": "TRANSMISIONES_AUTOMATICAS",
    "specialtyName": "Transmisiones automaticas",
    "hireDate": "2026-07-18",
    "createdAt": "2026-07-18T12:00:00",
    "updatedAt": null
  }
}
```

`GET /api/v1/technicians` conserva el contrato de lista simple bajo `data` para no romper el
frontend existente.

## Errores de dominio

- `404`: usuario, especialidad o perfil Technician inexistente.
- `409`: el usuario no tiene rol `TECNICO` o ya cuenta con perfil.
- `400`: request inválido.
- `401`: falta un JWT válido.
- `403`: rol sin autorización.

## Límites

- No hay frontend administrativo de perfiles Technician todavía.
- No hay eliminación de perfiles.
- No se implementa `assigned-to-me`.
- No se implementan Service Reports, PDF ni `/api/v2`.

Estos endpoints desbloquean el flujo E2E operativo que requiere `technicianId` real para Work Orders
y Jobs.
