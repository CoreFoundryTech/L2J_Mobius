#!/usr/bin/env bash
set -euo pipefail

ROLE="${1:-}"
shift || true

replace_ini_value() {
  local file="$1"
  local key="$2"
  local value="$3"

  if grep -qE "^[[:space:]]*${key}[[:space:]]*=" "${file}"; then
    sed -i -E "s#^[[:space:]]*${key}[[:space:]]*=.*#${key} = ${value}#" "${file}"
  else
    printf '\n%s = %s\n' "${key}" "${value}" >> "${file}"
  fi
}

require_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    echo "${name} is required" >&2
    exit 1
  fi
}

render_db_config() {
  local file="$1"
  require_env DB_HOST
  require_env DB_PORT
  require_env DB_NAME
  require_env DB_USER
  require_env DB_PASSWORD

  replace_ini_value "${file}" "URL" "jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf-8&useSSL=false"
  replace_ini_value "${file}" "Login" "${DB_USER}"
  replace_ini_value "${file}" "Password" "${DB_PASSWORD}"
}

case "${ROLE}" in
  login)
    cd /opt/l2/login
    render_db_config ./config/LoginServer.ini
    replace_ini_value ./config/LoginServer.ini "LoginserverHostname" "${LOGIN_BIND_HOST:-0.0.0.0}"
    replace_ini_value ./config/LoginServer.ini "LoginserverPort" "${LOGIN_CLIENT_PORT:-2106}"
    replace_ini_value ./config/LoginServer.ini "LoginHostname" "${LOGIN_GAME_HOST:-0.0.0.0}"
    replace_ini_value ./config/LoginServer.ini "LoginPort" "${LOGIN_GAME_PORT:-9014}"
    ;;
  game)
    cd /opt/l2/game
    render_db_config ./config/Server.ini
    replace_ini_value ./config/Server.ini "LoginHost" "${GAME_LOGIN_HOST:-login}"
    replace_ini_value ./config/Server.ini "LoginPort" "${LOGIN_INTERNAL_PORT:-9014}"
    replace_ini_value ./config/Server.ini "GameserverHostname" "${GAME_BIND_HOST:-0.0.0.0}"
    replace_ini_value ./config/Server.ini "GameserverPort" "${GAME_EXTERNAL_PORT:-7777}"
    replace_ini_value ./config/Server.ini "RequestServerID" "${REQUEST_SERVER_ID:-1}"

    cat > ./config/ipconfig.xml <<EOF_IPCONFIG
<?xml version="1.0" encoding="UTF-8"?>
<gameserver address="${PUBLIC_IP:-127.0.0.1}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../data/xsd/ipconfig.xsd">
	<define subnet="127.0.0.0/8" address="127.0.0.1" />
	<define subnet="10.0.0.0/8" address="${PUBLIC_IP:-127.0.0.1}" />
	<define subnet="172.16.0.0/12" address="${PUBLIC_IP:-127.0.0.1}" />
	<define subnet="192.168.0.0/16" address="${PUBLIC_IP:-127.0.0.1}" />
	<define subnet="0.0.0.0/0" address="${PUBLIC_IP:-127.0.0.1}" />
</gameserver>
EOF_IPCONFIG
    ;;
  *)
    echo "Usage: l2h5-entrypoint.sh <login|game> [command...]" >&2
    exit 1
    ;;
esac

exec "$@"
