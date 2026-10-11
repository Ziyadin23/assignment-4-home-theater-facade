#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
compiler=javac
if [[ -n "${JAVA_HOME:-}" ]]; then compiler="$JAVA_HOME/bin/javac"; fi
if ! command -v "$compiler" >/dev/null 2>&1; then
    echo 'A JDK is required. Install JDK 17 and set JAVA_HOME.' >&2
    exit 1
fi
mkdir -p build/classes
find src/main/java -name '*.java' -print > build/main-sources.txt
"$compiler" --release 17 -Xlint:all -Werror -d build/classes @build/main-sources.txt
