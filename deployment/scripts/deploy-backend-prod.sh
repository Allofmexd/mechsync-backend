#!/usr/bin/env bash
set -euo pipefail

SOURCE_JAR="${1:-/tmp/mechsync-backend.jar}"
DESTINATION_JAR="${MECHSYNC_DESTINATION_JAR:-/opt/mechsync/backend/app.jar}"
SERVICE_NAME="${MECHSYNC_SERVICE_NAME:-mechsync-backend.service}"
HEALTHCHECK_URL="${MECHSYNC_HEALTHCHECK_URL:-http://localhost:8080/api/v1/health}"
HEALTHCHECK_ATTEMPTS="${MECHSYNC_HEALTHCHECK_ATTEMPTS:-20}"
HEALTHCHECK_DELAY_SECONDS="${MECHSYNC_HEALTHCHECK_DELAY_SECONDS:-3}"

if [[ ! -f "$SOURCE_JAR" ]]; then
  printf 'Deployment JAR does not exist: %s\n' "$SOURCE_JAR" >&2
  exit 1
fi

if [[ ! -d "$(dirname "$DESTINATION_JAR")" ]]; then
  printf 'Backend destination directory does not exist: %s\n' "$(dirname "$DESTINATION_JAR")" >&2
  exit 1
fi

mv -- "$SOURCE_JAR" "$DESTINATION_JAR"
chown mechsync:mechsync "$DESTINATION_JAR"
chmod 640 "$DESTINATION_JAR"

systemctl restart "$SERVICE_NAME"
systemctl is-active --quiet "$SERVICE_NAME"
systemctl status "$SERVICE_NAME" --no-pager

for ((attempt = 1; attempt <= HEALTHCHECK_ATTEMPTS; attempt++)); do
  if curl \
      --fail \
      --silent \
      --show-error \
      --connect-timeout 3 \
      --max-time 10 \
      "$HEALTHCHECK_URL" >/dev/null; then
    printf 'Health check succeeded on attempt %s.\n' "$attempt"
    exit 0
  fi

  if [[ "$attempt" -lt "$HEALTHCHECK_ATTEMPTS" ]]; then
    sleep "$HEALTHCHECK_DELAY_SECONDS"
  fi
done

printf 'Health check failed after %s attempts: %s\n' \
  "$HEALTHCHECK_ATTEMPTS" "$HEALTHCHECK_URL" >&2
exit 1
