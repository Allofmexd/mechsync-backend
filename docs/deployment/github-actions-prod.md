# Deploy del backend con GitHub Actions

## Objetivo

El workflow `.github/workflows/deploy-backend-prod.yml` prueba, compila y despliega el backend de
MechSync automáticamente cuando se hace push a la rama `prod`. También permite una ejecución
manual mediante `workflow_dispatch`.

## Rama de despliegue

La rama que dispara el despliegue es:

```text
prod
```

## Secrets requeridos

Configura estos repository secrets:

| Secret | Descripción | Valor sugerido |
| --- | --- | --- |
| `EC2_HOST` | Host público o DNS de la instancia API | `3.212.179.142` |
| `EC2_USER` | Usuario SSH de despliegue | `ubuntu` |
| `EC2_PORT` | Puerto SSH | `22` |
| `EC2_SSH_PRIVATE_KEY` | Contenido completo de la llave privada autorizada en EC2 | Sin valor en el repositorio |

Para cargarlos en GitHub, abre **Repository Settings → Secrets and variables → Actions → New
repository secret**. Crea un secret por cada nombre de la tabla.

## Preparación obligatoria de EC2

Antes del primer despliegue deben existir:

- Usuario de sistema `mechsync`.
- Directorio `/opt/mechsync/backend`.
- Servicio `mechsync-backend.service`.
- Archivo `/etc/mechsync/mechsync-backend.env` con la configuración del servicio.
- `curl` y acceso SSH habilitado para el runner.

El usuario remoto debe tener permisos `sudo` no interactivos para mover y proteger el JAR,
reiniciar el servicio y consultar su estado. Limita esos permisos a los comandos necesarios.

La instancia debe aceptar SSH desde GitHub Actions o desde el origen de red configurado. Los
runners hospedados por GitHub no tienen una única IP fija; revisa la estrategia de red antes de
habilitar el workflow en producción.

## Flujo del workflow

1. Descarga el repositorio con `actions/checkout`.
2. Configura Java 17 Temurin y caché de Maven con `actions/setup-java`.
3. Ejecuta `mvn clean test`.
4. Ejecuta `mvn clean package`.
5. Selecciona un único JAR de `target/`, excluyendo `original-*.jar`.
6. Crea una llave SSH temporal desde `EC2_SSH_PRIVATE_KEY` con permisos `600`.
7. Sube el JAR y el script de despliegue a `/tmp`.
8. Reemplaza `/opt/mechsync/backend/app.jar`, configura propietario y permisos, reinicia systemd
   y valida `http://localhost:8080/api/v1/health` con reintentos.
9. Elimina la llave temporal incluso si un paso falla.

El workflow falla si fallan los tests, el build, la selección del JAR, SSH/SCP, systemd o el
health check.

## Crear y publicar la rama `prod`

Ejecuta estos comandos únicamente cuando los cambios estén revisados y listos para producción:

```bash
git checkout -b prod
git push -u origin prod
```

Después, cualquier merge o push posterior a `prod` dispara el workflow. También puede ejecutarse
manualmente desde la pestaña **Actions** de GitHub.

## Validación después del despliegue

```bash
curl -i http://3.212.179.142:8080/api/v1/health
curl -i http://3.212.179.142:8080/api/v1/health/database
```

Para validar el preflight CORS de login:

```bash
curl -i -X OPTIONS "http://3.212.179.142:8080/api/v1/auth/login" \
  -H "Origin: http://localhost:5173" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type"
```

La respuesta esperada es HTTP 200 o 204 e incluye:

```text
Access-Control-Allow-Origin: http://localhost:5173
```

La misma prueba local usa `http://localhost:8080/api/v1/auth/login`.

## Advertencias de seguridad

- No subas archivos `.pem` ni otras llaves privadas.
- No pegues la llave privada en archivos del repositorio.
- No imprimas secrets en logs o comandos de diagnóstico.
- Conserva `/etc/mechsync/mechsync-backend.env` únicamente en EC2 y fuera de Git.
- Limita los orígenes CORS de producción mediante `MECHSYNC_CORS_ALLOWED_ORIGINS`.
- El `ssh-keyscan` del workflow evita prompts interactivos, pero para mayor seguridad operativa
  considera fijar y verificar previamente la huella del host en vez de confiar en el primer scan.
