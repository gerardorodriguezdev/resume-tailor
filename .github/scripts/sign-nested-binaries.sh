#!/bin/bash
# Signs every Mach-O binary inside the jars' nested BOOT-INF/lib dependencies that lacks a Developer ID signature,
# since notarization inspects nested archives and rejects ad-hoc or unsigned binaries.
#
# Required env: KEYCHAIN, SIGNING_NAME, RUNNER_TEMP
# Usage: sign-nested-binaries.sh <jar>...
set -euo pipefail

readonly IDENTITY="Developer ID Application: $SIGNING_NAME"

is_developer_id_signed() {
  local details
  details=$(codesign -dvv "$1" 2>&1 || true)
  grep -q '^Authority=Developer ID Application: ' <<< "$details"
}

sign_nested_jar() {
  local jar_dir="$1"
  local nested_jar="$2"
  local nested_dir
  nested_dir=$(mktemp -d "$RUNNER_TEMP/nested.XXXXXX")
  unzip -q "$nested_jar" -x '*.class' -d "$nested_dir"

  local signed=()
  while IFS=$'\t' read -r binary mime_type; do
    if [[ "$mime_type" == *application/x-mach-binary* ]] && ! is_developer_id_signed "$binary"; then
      codesign --force --timestamp --options runtime --keychain "$KEYCHAIN" --sign "$IDENTITY" "$binary"
      signed+=("${binary#"$nested_dir"/}")
    fi
  done < <(find "$nested_dir" -type f -print0 | xargs -0 file --mime-type -F $'\t')

  if ((${#signed[@]})); then
    (cd "$nested_dir" && zip -q "$nested_jar" "${signed[@]}")
    echo "Signed ${#signed[@]} binaries in $(basename "$nested_jar")"
    updated+=("${nested_jar#"$jar_dir"/}")
  fi
  rm -rf "$nested_dir"
}

sign_jar() {
  local jar
  jar=$(realpath "$1")
  local jar_dir
  jar_dir=$(mktemp -d "$RUNNER_TEMP/jar.XXXXXX")
  unzip -q "$jar" 'BOOT-INF/lib/*.jar' -d "$jar_dir"

  updated=()
  for nested_jar in "$jar_dir"/BOOT-INF/lib/*.jar; do
    sign_nested_jar "$jar_dir" "$nested_jar"
  done

  if ((${#updated[@]})); then
    (cd "$jar_dir" && zip -q -0 "$jar" "${updated[@]}")
  fi
  rm -rf "$jar_dir"
}

for jar in "$@"; do
  sign_jar "$jar"
done
