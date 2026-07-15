# Técnicos de solo lectura

## Alcance

El módulo permite consultar los técnicos existentes y la información básica de su usuario y
especialidad. En esta fase no expone endpoints para crear, modificar o eliminar técnicos.

## Endpoint

| Método | Ruta | Roles |
|---|---|---|
| GET | `/api/v1/technicians` | ADMINISTRADOR, TECNICO |

`CLIENTE` no tiene acceso al directorio administrativo de técnicos.

```bash
curl "http://localhost:8080/api/v1/technicians" \
  -H "Authorization: Bearer $TOKEN"
```

Respuesta de ejemplo; los IDs son ilustrativos y deben obtenerse del ambiente consultado:

```json
{
  "status": "OK",
  "data": [
    {
      "id": 1,
      "userId": 3,
      "firstName": "Nombre",
      "lastName": "Apellido",
      "fullName": "Nombre Apellido",
      "email": "tecnico@example.com",
      "phone": "9610000000",
      "specialtyId": 1,
      "specialtyCode": "TRANSMISIONES_AUTOMATICAS",
      "specialtyName": "Transmisiones automaticas",
      "hireDate": "2025-01-15"
    }
  ]
}
```

La consulta selecciona únicamente los campos publicados; no carga ni expone `password_hash`.
`fullName` y `specialtyName` son etiquetas de presentación derivadas de datos persistidos.

Para `POST /api/v1/vehicle-intakes`, `technicianId` continúa siendo opcional. Si el usuario elige
un técnico, debe enviarse el `id` devuelto por este endpoint; si no selecciona uno, el campo puede
omitirse o enviarse como `null`.

## Errores

- `401`: falta un JWT válido.
- `403`: el rol autenticado no tiene permiso.

