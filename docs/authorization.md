# Autorización por asignación

## Política

Los endpoints administrativos globales son exclusivos de `ADMINISTRADOR`. El rol `TECNICO` usa
endpoints explícitos `assigned-to-me`, cuyo filtro se ejecuta en repositorio con el Technician ID
resuelto desde el `userId` autenticado. El cliente nunca determina la pertenencia enviando un
Technician ID.

| Recurso | Endpoint global | Endpoint de TECNICO |
|---|---|---|
| Perfil Technician | `GET /api/v1/technicians` | `GET /api/v1/technicians/me` |
| Work Orders | `GET /api/v1/work-orders` | `GET /api/v1/work-orders/assigned-to-me` |
| Jobs | `GET /api/v1/jobs` | `GET /api/v1/jobs/assigned-to-me` |
| Service Reports | `GET /api/v1/service-reports` | `GET /api/v1/service-reports/assigned-to-me` |

`ADMINISTRADOR` conserva los endpoints globales y no necesita un perfil Technician. `CLIENTE`
recibe `403` en estas rutas. Sin JWT se responde `401`.

## Resolución User a Technician

El principal JWT contiene el ID del usuario autenticado. Un resolver central busca
`technicians.user_id` y entrega el Technician ID a los casos de uso asignados. Un usuario con rol
`TECNICO` sin perfil recibe `403`; no se usa un ID por defecto ni se amplía su acceso.

## Prevención de IDOR

Las consultas de listados y detalles incluyen el Technician ID en SQL/JPA. Un técnico que cambia el
ID de una Work Order, Job o Service Report no obtiene el recurso: la respuesta es `404`, aunque el
registro exista para otro técnico. La misma regla protege la descarga PDF. No se cargan listados
globales para filtrarlos en memoria o en el navegador.

Las escrituras administrativas existentes continúan limitadas a `ADMINISTRADOR`. El aislamiento de
lectura no habilita creación, actualización, cancelación ni cambios de workflow para `TECNICO`.
