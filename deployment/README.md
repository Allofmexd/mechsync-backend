# Despliegue del backend

Esta carpeta contiene utilidades auditables para desplegar el JAR de MechSync. No contiene
llaves, tokens, contraseñas ni archivos de entorno.

`scripts/deploy-backend-prod.sh` se ejecuta en la instancia EC2 mediante GitHub Actions. Recibe
como primer argumento el JAR temporal, que por defecto es `/tmp/mechsync-backend.jar`, y realiza:

1. Validación del archivo y del directorio destino.
2. Reemplazo de `/opt/mechsync/backend/app.jar`.
3. Asignación de propietario `mechsync:mechsync` y permisos `640`.
4. Reinicio y comprobación de `mechsync-backend.service`.
5. Health check local con reintentos.

El servidor debe tener previamente el servicio systemd, el usuario `mechsync`, el directorio de
aplicación y `/etc/mechsync/mechsync-backend.env`. El usuario SSH utilizado por el workflow debe
tener permisos `sudo` no interactivos únicamente para las operaciones de despliegue necesarias.

La configuración completa está en
[`docs/deployment/github-actions-prod.md`](../docs/deployment/github-actions-prod.md).
