#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home}"
export PATH="$JAVA_HOME/bin:$PATH"
exec ./gradlew run --quiet --console=plain
