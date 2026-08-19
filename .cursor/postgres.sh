#!/usr/bin/env bash
# Idempotently initialize and start a local PostgreSQL 16 instance that matches
# the datasource configured in src/main/resources/application*.yml
# (jdbc:postgresql://localhost:5432/postgres, user/password: postgres/postgres).
set -euo pipefail

PG_BIN="/usr/lib/postgresql/16/bin"
export PATH="${PG_BIN}:${PATH}"
PGDATA="${LMS_PGDATA:-${HOME}/.lms-pgdata}"
PGLOG="${PGDATA}/server.log"

if [ ! -f "${PGDATA}/PG_VERSION" ]; then
  echo "[postgres] initializing cluster at ${PGDATA}"
  mkdir -p "${PGDATA}"
  initdb -D "${PGDATA}" -U postgres --auth-local=trust --auth-host=md5 >/dev/null
fi

if pg_ctl -D "${PGDATA}" status >/dev/null 2>&1; then
  echo "[postgres] already running"
else
  # A snapshot/unclean shutdown can leave a stale postmaster.pid that blocks
  # startup even though no server is running; remove it before starting.
  if [ -f "${PGDATA}/postmaster.pid" ]; then
    echo "[postgres] removing stale postmaster.pid"
    rm -f "${PGDATA}/postmaster.pid"
  fi
  echo "[postgres] starting"
  pg_ctl -D "${PGDATA}" -l "${PGLOG}" -o "-p 5432 -c listen_addresses=localhost -c unix_socket_directories=/tmp" -w start
fi

# Wait for readiness.
for _ in $(seq 1 30); do
  if pg_isready -h localhost -p 5432 -U postgres >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

# Ensure the postgres role has the password the application expects.
psql -h /tmp -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 \
  -c "ALTER ROLE postgres WITH PASSWORD 'postgres';" >/dev/null

# Provision the application schema. Spring's spring.sql.init only runs for embedded
# datasources by default, so apply schema.sql here. It is idempotent
# (CREATE TABLE IF NOT EXISTS), matching the app's expected tables.
SCHEMA_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/src/main/resources/schema.sql"
if [ -f "${SCHEMA_FILE}" ]; then
  psql -h /tmp -p 5432 -U postgres -d postgres -v ON_ERROR_STOP=1 -f "${SCHEMA_FILE}" >/dev/null
  echo "[postgres] schema applied from ${SCHEMA_FILE}"
fi

echo "[postgres] ready on localhost:5432 (db: postgres)"
