#!/usr/bin/env bash
# Best-effort per-boot startup for the local PostgreSQL used in development.
# The Vert.x API falls back to in-memory mode if Postgres is unavailable, so
# this script never fails the boot: any problem is logged and it exits 0.
set -uo pipefail

PGDATA="${HOME}/.yinilow-pgdata"
DB_NAME="yinilow"
PG_BIN="$(ls -d /usr/lib/postgresql/*/bin 2>/dev/null | sort -V | tail -1 || true)"

if [ -z "${PG_BIN}" ]; then
  echo "PostgreSQL is not installed; the API will run in in-memory mode."
  exit 0
fi

if [ ! -f "${PGDATA}/PG_VERSION" ]; then
  "${PG_BIN}/initdb" -D "${PGDATA}" -U postgres --auth-local=trust --auth-host=trust || {
    echo "initdb failed; the API will run in in-memory mode."
    exit 0
  }
fi

if ! "${PG_BIN}/pg_ctl" -D "${PGDATA}" status >/dev/null 2>&1; then
  "${PG_BIN}/pg_ctl" -D "${PGDATA}" \
    -o "-p 5432 -c listen_addresses=127.0.0.1 -k /tmp" \
    -l "${PGDATA}/server.log" -w start || {
    echo "Could not start PostgreSQL; the API will run in in-memory mode."
    exit 0
  }
fi

if ! "${PG_BIN}/psql" -h 127.0.0.1 -p 5432 -U postgres -tAc \
  "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" 2>/dev/null | grep -q 1; then
  "${PG_BIN}/createdb" -h 127.0.0.1 -p 5432 -U postgres "${DB_NAME}" || true
fi

echo "PostgreSQL ready on 127.0.0.1:5432 (database ${DB_NAME})."
