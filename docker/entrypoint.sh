#!/usr/bin/env bash
set -euo pipefail

cert="${TLS_CERT_FILE:-/etc/openjcockpit/tls/tls.crt}"
key="${TLS_KEY_FILE:-/etc/openjcockpit/tls/tls.key}"
ca="${TLS_CA_FILE:-/etc/openjcockpit/tls/ca.crt}"

require_readable() {
  if [[ ! -e "$1" ]]; then
    echo "TLS misconfigured: $1 is missing${2:+ ($2)}" >&2
    exit 1
  fi
  if [[ ! -r "$1" ]]; then
    echo "TLS misconfigured: $1 is not readable by UID $(id -u)" >&2
    exit 1
  fi
}

trusted=()
if [[ -e "$cert" || -e "$key" ]]; then
  require_readable "$cert" "certificate and private key must both be mounted"
  require_readable "$key" "certificate and private key must both be mounted"
  echo "TLS enabled: serving HTTPS with certificate $cert" >&2
  export SERVER_SSL_BUNDLE=server
  export SPRING_SSL_BUNDLE_PEM_SERVER_KEYSTORE_CERTIFICATE="file:$cert"
  export SPRING_SSL_BUNDLE_PEM_SERVER_KEYSTORE_PRIVATEKEY="file:$key"
  export SPRING_SSL_BUNDLE_PEM_SERVER_RELOADONUPDATE=true
  if [[ -n "${TLS_KEY_PASSWORD:-}" ]]; then
    export SPRING_SSL_BUNDLE_PEM_SERVER_KEYSTORE_PRIVATEKEYPASSWORD="$TLS_KEY_PASSWORD"
  fi
  export MANAGEMENT_SERVER_SSL_ENABLED="${MANAGEMENT_SERVER_SSL_ENABLED:-false}"
  trusted+=("$cert")
else
  echo "TLS disabled: no certificate at $cert, serving HTTP" >&2
fi
if [[ -e "$ca" ]]; then
  require_readable "$ca"
  trusted+=("$ca")
fi

java_opts=(-XX:MaxRAMPercentage=75)
if (( ${#trusted[@]} > 0 )); then
  truststore="$(mktemp -d)/truststore.p12"
  storepass=changeit
  cp "$JAVA_HOME/lib/security/cacerts" "$truststore"
  chmod u+w "$truststore"
  # keytool imports one certificate per call, so split each PEM bundle into its certificates first.
  split_dir="$(mktemp -d)"
  awk -v dir="$split_dir" '
    /-----BEGIN CERTIFICATE-----/ { n++; out = sprintf("%s/%03d.pem", dir, n) }
    out { print > out }
    /-----END CERTIFICATE-----/ { close(out); out = "" }
  ' "${trusted[@]}"
  count=0
  for pem in "$split_dir"/*.pem; do
    [[ -e "$pem" ]] || continue
    count=$((count + 1))
    keytool -importcert -noprompt -keystore "$truststore" -storepass "$storepass" \
      -alias "openjcockpit-$(basename "$pem" .pem)" -file "$pem" >/dev/null
  done
  rm -rf "$split_dir"
  if (( count == 0 )); then
    echo "TLS misconfigured: no PEM certificates found in ${trusted[*]}" >&2
    exit 1
  fi
  echo "TLS trust: added $count certificate(s) from ${trusted[*]} to the JVM truststore" >&2
  java_opts+=(
    "-Djavax.net.ssl.trustStore=$truststore"
    "-Djavax.net.ssl.trustStorePassword=$storepass"
    "-Djavax.net.ssl.trustStoreType=PKCS12"
  )
fi

exec java "${java_opts[@]}" -jar /app/app.jar "$@"
