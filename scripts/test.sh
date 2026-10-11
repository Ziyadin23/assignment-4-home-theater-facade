#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./scripts/build.sh
compiler=javac
runtime=java
if [[ -n "${JAVA_HOME:-}" ]]; then
    compiler="$JAVA_HOME/bin/javac"
    runtime="$JAVA_HOME/bin/java"
fi
mkdir -p build/test-classes
find src/test/java -name '*.java' -print > build/test-sources.txt
"$compiler" --release 17 -Xlint:all -Werror -cp build/classes \
    -d build/test-classes @build/test-sources.txt
"$runtime" -cp build/classes:build/test-classes theater.HomeTheaterTests
