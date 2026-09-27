#!/usr/bin/env bash
# Runs the benchmark once per spring-jdbc jar and writes JMH JSON results into results/.
# Usage: ./run-benchmark.sh <name>=<path to spring-jdbc jar> ...
# Example: ./run-benchmark.sh before=/tmp/spring-jdbc-main.jar after=/tmp/spring-jdbc-pr.jar
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p results

for spec in "$@"; do
    name="${spec%%=*}"
    jar="${spec#*=}"
    echo "== ${name}: ${jar}"
    ./gradlew -q run -PspringJdbcJar="${jar}" \
        --args="-rf json -rff $(pwd)/results/${name}.json"
done
