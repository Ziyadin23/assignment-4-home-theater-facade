#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh
runtime=java
if [[ -n "${JAVA_HOME:-}" ]]; then runtime="$JAVA_HOME/bin/java"; fi
"$runtime" -cp build/classes theater.Client "$@"
