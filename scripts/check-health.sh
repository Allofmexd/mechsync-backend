#!/usr/bin/env bash
set -uo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd -- "$SCRIPT_DIR/.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

SERVER_PORT="${SERVER_PORT:-8080}"

if [[ ! "$SERVER_PORT" =~ ^[0-9]+$ ]] || (( SERVER_PORT < 1 || SERVER_PORT > 65535 )); then
  printf 'SERVER_PORT debe ser un puerto válido entre 1 y 65535.\n' >&2
  exit 1
fi

BASE_URL="http://localhost:$SERVER_PORT"
TEMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TEMP_DIR"' EXIT

check_endpoint() {
  local path="$1"
  local response_file="$2"
  local http_code

  if ! http_code="$(curl --silent --show-error --output "$response_file" \
      --write-out '%{http_code}' "$BASE_URL$path")"; then
    printf 'GET %s -> no fue posible conectar con la API.\n' "$path" >&2
    return 1
  fi

  printf 'GET %s -> HTTP %s\n' "$path" "$http_code"
  if [[ -s "$response_file" ]]; then
    cat "$response_file"
    printf '\n'
  else
    printf '(respuesta vacía)\n'
  fi

  [[ "$http_code" =~ ^2[0-9][0-9]$ ]]
}

exit_status=0
check_endpoint "/api/v1/health" "$TEMP_DIR/api-health.json" || exit_status=1
check_endpoint "/api/v1/health/database" "$TEMP_DIR/database-health.json" || exit_status=1

exit "$exit_status"
