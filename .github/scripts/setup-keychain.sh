#!/bin/bash
# Imports every signing identity from a base64 PKCS#12 into a temporary keychain.
#
# Required env: KEYCHAIN, CERTIFICATE_P12, CERTIFICATE_PASSWORD, RUNNER_TEMP
set -euo pipefail
umask 077

readonly REQUIRED_IDENTITIES=("Developer ID Application" "Developer ID Installer")
readonly PRIVATE_KEY_BEGIN='-----BEGIN .*PRIVATE KEY-----'
readonly PRIVATE_KEY_END='-----END .*PRIVATE KEY-----'

SOURCE_P12="$RUNNER_TEMP/source.p12"
IMPORT_P12="$RUNNER_TEMP/import.p12"
KEYCHAIN_PASSWORD=$(uuidgen)
IMPORT_PASSWORD=$(uuidgen)
export IMPORT_PASSWORD
trap 'rm -f "$SOURCE_P12" "$IMPORT_P12"' EXIT

read_source_p12() {
  /usr/bin/openssl pkcs12 -in "$SOURCE_P12" -passin env:CERTIFICATE_PASSWORD "$@"
}

certificates() {
  read_source_p12 -nokeys
}

private_key_count() {
  read_source_p12 -nocerts -nodes | grep -c -- "$PRIVATE_KEY_BEGIN"
}

private_key() {
  read_source_p12 -nocerts -nodes | awk -v n="$1" -v begin="$PRIVATE_KEY_BEGIN" -v end="$PRIVATE_KEY_END" '
    $0 ~ begin { k++ }
    k == n { print }
    k == n && $0 ~ end { exit }
  '
}

create_keychain() {
  security create-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN"
  security set-keychain-settings -lut 3600 "$KEYCHAIN"
  security unlock-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN"
}

# `openssl pkcs12 -export` takes a single key, so each identity is re-exported on its own.
import_identity() {
  /usr/bin/openssl pkcs12 -export \
    -in <(certificates) \
    -inkey <(private_key "$1") \
    -passout env:IMPORT_PASSWORD \
    -out "$IMPORT_P12"
  security import "$IMPORT_P12" -k "$KEYCHAIN" -P "$IMPORT_PASSWORD" -x \
    -T /usr/bin/codesign \
    -T /usr/bin/productbuild
}

allow_signing_without_prompt() {
  security set-key-partition-list -S apple-tool:,apple: -s -k "$KEYCHAIN_PASSWORD" "$KEYCHAIN" > /dev/null
  security list-keychains -d user -s "$KEYCHAIN"
}

verify_identities() {
  local identities
  identities=$(security find-identity "$KEYCHAIN")
  for kind in "${REQUIRED_IDENTITIES[@]}"; do
    if ! grep -q "\"$kind: " <<< "$identities"; then
      echo "::error::No '$kind' identity (certificate + private key) in MACOS_CERTIFICATE_P12"
      exit 1
    fi
  done
}

printf '%s' "$CERTIFICATE_P12" | base64 --decode > "$SOURCE_P12"
create_keychain
for index in $(seq 1 "$(private_key_count)"); do
  import_identity "$index"
done
allow_signing_without_prompt
verify_identities
