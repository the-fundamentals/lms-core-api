#!/usr/bin/env bash
# Idempotent repository bootstrap for Cloud Agents.
# System toolchains (Temurin JDK 25, PostgreSQL 16) live in the base snapshot;
# this only refreshes source-derived state after checkout: it resolves Gradle
# dependencies, generates the OpenAPI server code, and compiles main + test
# sources. No services are started and no tests are run here.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/temurin-25-jdk-amd64}"

chmod +x ./gradlew
# `classes` triggers openApiGenerate + compileJava; `testClasses` compiles tests.
./gradlew --no-daemon classes testClasses

echo "[install] Gradle dependencies resolved, OpenAPI code generated, sources compiled."
