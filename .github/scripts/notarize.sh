#!/bin/bash
# Notarizes and staples every dmg and pkg in dist/.
#
# Required env: APPLE_API_KEY, APPLE_API_KEY_ID, APPLE_API_ISSUER_ID, RUNNER_TEMP
set -euo pipefail
umask 077

API_KEY_FILE="$RUNNER_TEMP/api-key.p8"
trap 'rm -f "$API_KEY_FILE"' EXIT

notarize() {
  xcrun notarytool submit "$1" \
    --key "$API_KEY_FILE" \
    --key-id "$APPLE_API_KEY_ID" \
    --issuer "$APPLE_API_ISSUER_ID" \
    --wait
  xcrun stapler staple "$1"
}

printf '%s' "$APPLE_API_KEY" > "$API_KEY_FILE"
for file in dist/*.dmg dist/*.pkg; do
  notarize "$file"
done
