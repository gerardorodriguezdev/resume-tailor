#!/bin/bash
# Launches the app from the dmg in dist/ for 30 seconds and fails if it exits early or logs an exception, since a
# startup failure only shows an error dialog and keeps the process alive.
#
# Required env: RUNNER_TEMP
set -euo pipefail

mount_dir=$(mktemp -d "$RUNNER_TEMP/dmg.XXXXXX")
hdiutil attach -nobrowse -readonly -mountpoint "$mount_dir" dist/*.dmg
"$mount_dir/Resume Tailor.app/Contents/MacOS/Resume Tailor" > "$RUNNER_TEMP/app.log" 2>&1 &
app_pid=$!
sleep 30
running=true
kill "$app_pid" 2>/dev/null || running=false
wait "$app_pid" || true
hdiutil detach "$mount_dir"
cat "$RUNNER_TEMP/app.log"
if [[ "$running" == false ]] || grep -qE 'Exception|Error' "$RUNNER_TEMP/app.log"; then
  echo "::error::Resume Tailor failed on startup"
  exit 1
fi
