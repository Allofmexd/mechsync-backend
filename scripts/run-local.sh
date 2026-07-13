#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  printf 'No se encontró .env. Copia .env.example a .env y configura tus credenciales.\n' >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

required_variables=(
  MECHSYNC_DB_URL
  MECHSYNC_DB_USERNAME
  MECHSYNC_DB_PASSWORD
  MECHSYNC_JWT_SECRET
  MECHSYNC_JWT_EXPIRATION_MINUTES
  MECHSYNC_JWT_ISSUER
)

for variable_name in "${required_variables[@]}"; do
  if [[ -z "${!variable_name:-}" ]]; then
    printf 'Falta la variable requerida %s en .env.\n' "$variable_name" >&2
    exit 1
  fi
done

if [[ "$MECHSYNC_DB_URL" == *NOMBRE_DB* ]]; then
  printf 'Reemplaza NOMBRE_DB en MECHSYNC_DB_URL antes de levantar la API.\n' >&2
  exit 1
fi

if [[ "$MECHSYNC_DB_PASSWORD" == "CAMBIA_ESTA_PASSWORD" ]]; then
  printf 'Reemplaza CAMBIA_ESTA_PASSWORD con la contraseña MySQL de la aplicación.\n' >&2
  exit 1
fi

if [[ "$MECHSYNC_JWT_SECRET" == *CAMBIA* ]]; then
  printf 'Reemplaza el placeholder de MECHSYNC_JWT_SECRET antes de levantar la API.\n' >&2
  exit 1
fi

cd "$PROJECT_ROOT"
exec mvn spring-boot:run
