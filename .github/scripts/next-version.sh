#!/bin/bash
# Exports VERSION as the latest vX.Y.Z release with its patch incremented, or 1.0.0 when there is none.
#
# Required env: GH_TOKEN, GITHUB_REPOSITORY, GITHUB_ENV
set -euo pipefail

latest=$(gh release list --repo "$GITHUB_REPOSITORY" --limit 100 --json tagName --jq '.[].tagName' \
  | grep -E '^v[0-9]+\.[0-9]+\.[0-9]+$' | sort -V | tail -n 1 || true)
if [[ -z "$latest" ]]; then
  version=1.0.0
else
  IFS=. read -r major minor patch <<< "${latest#v}"
  version="$major.$minor.$((patch + 1))"
fi
echo "VERSION=$version" >> "$GITHUB_ENV"
