#!/usr/bin/env bash
# Per-boot startup for Cloud Agents.
# Brings up the local PostgreSQL instance (idempotent) and then launches the
# Spring Boot API in the foreground so the environment runs the application on
# boot. PostgreSQL is started as a background daemon by postgres.sh, which
# returns once the database is ready; bootRun then stays attached.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/temurin-25-jdk-amd64}"

bash .cursor/postgres.sh

echo "[start] PostgreSQL ready. Launching LMS Core API on http://localhost:8080 ..."
exec ./gradlew bootRun --args="--spring.profiles.active=local"
