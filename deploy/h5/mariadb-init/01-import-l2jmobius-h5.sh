#!/usr/bin/env bash
set -euo pipefail

DB_NAME="${MARIADB_DATABASE:-${MYSQL_DATABASE:-}}"
ROOT_PASSWORD="${MARIADB_ROOT_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"

if [ -z "${DB_NAME}" ]; then
  echo "MARIADB_DATABASE/MYSQL_DATABASE is required to import L2J schema" >&2
  exit 1
fi

if [ -z "${ROOT_PASSWORD}" ]; then
  echo "MARIADB_ROOT_PASSWORD/MYSQL_ROOT_PASSWORD is required to import L2J schema" >&2
  exit 1
fi

echo "Importing L2J Mobius H5 schema into ${DB_NAME}"

for sql_file in /l2j/sql/login/*.sql /l2j/sql/game/*.sql; do
  [ -f "${sql_file}" ] || continue
  echo "Importing ${sql_file}"
  mariadb --protocol=socket -uroot -p"${ROOT_PASSWORD}" "${DB_NAME}" < "${sql_file}"
done

echo "L2J Mobius H5 schema import finished"
