#!/usr/bin/env bash
port="${MANAGEMENT_PORT:-$1}"
request=$'GET /actuator/health/readiness HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n'

(exec 3<>"/dev/tcp/127.0.0.1/$port" && printf '%s' "$request" >&3 && grep -q '"status":"UP"' <&3) && exit 0

printf '%s' "$request" \
  | timeout 4 openssl s_client -quiet -connect "127.0.0.1:$port" -servername localhost 2>/dev/null \
  | grep -q '"status":"UP"'
