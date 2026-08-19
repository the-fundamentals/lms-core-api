#!/usr/bin/env bash
# Per-boot runtime reconciliation for Cloud Agents.
# Brings up the local PostgreSQL instance the application depends on and applies
# the schema. Idempotent and safe to run on every boot; returns once the
# database is ready. The application itself runs in the "api" terminal.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

bash .cursor/postgres.sh

echo "[start] PostgreSQL ready. The API is launched by the 'api' terminal (./gradlew bootRun)."
