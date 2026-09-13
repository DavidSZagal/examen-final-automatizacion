#!/usr/bin/env bash

set -Eeuo pipefail

JAR_PATH="${1:-}"
DEPLOY_ROOT="${2:-target/blue-green}"
HEALTHCHECK_PORT="${HEALTHCHECK_PORT:-18080}"
FORCE_FAILURE="${FORCE_HEALTHCHECK_FAILURE:-false}"

if [[ -z "$JAR_PATH" ]]; then
    echo "ERROR: debe indicar la ruta del archivo JAR."
    exit 2
fi

if [[ ! -f "$JAR_PATH" ]]; then
    echo "ERROR: no existe el artefacto $JAR_PATH"
    exit 2
fi

BLUE_DIR="$DEPLOY_ROOT/blue"
GREEN_DIR="$DEPLOY_ROOT/green"
STATE_DIR="$DEPLOY_ROOT/state"
STATE_FILE="$STATE_DIR/active-slot.txt"
DEPLOY_LOG="$DEPLOY_ROOT/deployment.log"

mkdir -p "$BLUE_DIR" "$GREEN_DIR" "$STATE_DIR"

VERSION="${GITHUB_SHA:-local}"
TIMESTAMP="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

if [[ ! -f "$STATE_FILE" ]]; then
    cp "$JAR_PATH" "$BLUE_DIR/application.jar"

    printf 'version=%s\nstatus=baseline\n' \
        "$VERSION" > "$BLUE_DIR/metadata.txt"

    printf '%s\n' "blue" > "$STATE_FILE"
fi

ACTIVE_SLOT="$(tr -d '\r\n ' < "$STATE_FILE")"

case "$ACTIVE_SLOT" in
    blue)
        TARGET_SLOT="green"
        ;;
    green)
        TARGET_SLOT="blue"
        ;;
    *)
        echo "ERROR: estado Blue-Green no válido: $ACTIVE_SLOT"
        exit 2
        ;;
esac

PREVIOUS_SLOT="$ACTIVE_SLOT"
TARGET_DIR="$DEPLOY_ROOT/$TARGET_SLOT"

echo "Slot activo anterior: $PREVIOUS_SLOT"
echo "Desplegando nueva versión en: $TARGET_SLOT"

cp "$JAR_PATH" "$TARGET_DIR/application.jar"

printf 'version=%s\ndeployed_at=%s\nstatus=candidate\n' \
    "$VERSION" \
    "$TIMESTAMP" > "$TARGET_DIR/metadata.txt"

HEALTH_URL="http://127.0.0.1:${HEALTHCHECK_PORT}/actuator/health"
HEALTH_OK="false"
APP_PID=""

detener_aplicacion() {
    if [[ -n "$APP_PID" ]] \
            && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null || true
        wait "$APP_PID" 2>/dev/null || true
    fi
}

trap detener_aplicacion EXIT

if [[ "$FORCE_FAILURE" != "true" ]]; then
    java -jar "$TARGET_DIR/application.jar" \
        --server.port="$HEALTHCHECK_PORT" \
        > "$TARGET_DIR/application.log" 2>&1 &

    APP_PID="$!"

    for intento in $(seq 1 30); do
        if curl --silent --fail "$HEALTH_URL" \
                | grep --quiet '"status":"UP"'; then
            HEALTH_OK="true"
            break
        fi

        if ! kill -0 "$APP_PID" 2>/dev/null; then
            break
        fi

        sleep 1
    done
fi

detener_aplicacion
APP_PID=""
trap - EXIT

if [[ "$HEALTH_OK" != "true" ]]; then
    printf '%s\n' "$PREVIOUS_SLOT" > "$STATE_FILE"

    printf '%s status=ROLLBACK previous=%s failed=%s\n' \
        "$TIMESTAMP" \
        "$PREVIOUS_SLOT" \
        "$TARGET_SLOT" >> "$DEPLOY_LOG"

    echo "HEALTH CHECK: DOWN"
    echo "ROLLBACK EJECUTADO"
    echo "El tráfico permanece en: $PREVIOUS_SLOT"
    exit 1
fi

printf '%s\n' "$TARGET_SLOT" > "$STATE_FILE"

printf 'version=%s\ndeployed_at=%s\nstatus=active\n' \
    "$VERSION" \
    "$TIMESTAMP" > "$TARGET_DIR/metadata.txt"

printf '%s status=SUCCESS previous=%s active=%s\n' \
    "$TIMESTAMP" \
    "$PREVIOUS_SLOT" \
    "$TARGET_SLOT" >> "$DEPLOY_LOG"

echo "HEALTH CHECK: UP"
echo "CAMBIO DE TRÁFICO COMPLETADO"
echo "Slot activo actual: $TARGET_SLOT"
echo "Slot anterior disponible para rollback: $PREVIOUS_SLOT"

if [[ -n "${GITHUB_OUTPUT:-}" ]]; then
    {
        echo "previous_slot=$PREVIOUS_SLOT"
        echo "active_slot=$TARGET_SLOT"
        echo "deployment_status=success"
    } >> "$GITHUB_OUTPUT"
fi