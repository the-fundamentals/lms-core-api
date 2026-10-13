#!/usr/bin/env bash
# Baked into environment builds: mise toolchains + deps + dependency-free test/lint gates.
# Full-stack local run is intentionally not started here; see cloud-agent-local-run.sh (on demand).
set -euo pipefail

API_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

ensure_mise() {
  if ! command -v mise >/dev/null 2>&1; then
    curl -fsSL https://mise.run | sh
  fi
  export PATH="${HOME}/.local/bin:${PATH}"
  eval "$(mise activate bash)"
}

find_sibling_repo() {
  local name="$1"
  local candidate=""
  for candidate in \
    "$(dirname "$API_ROOT")/${name}" \
    "/agent/repos/${name}" \
    "/workspace/${name}"; do
    if [[ -f "${candidate}/mise.toml" ]]; then
      echo "${candidate}"
      return 0
    fi
  done
  return 1
}

mise_bootstrap_repo() {
  local root="$1"
  echo "==> mise bootstrap: ${root}"
  cd "${root}"
  mise trust -y
  mise install
}

run_api_checks() {
  echo "==> API: ./gradlew test"
  cd "${API_ROOT}"
  mise exec -- ./gradlew test --no-daemon
}

run_web_checks() {
  local web_root="$1"
  if [[ -z "${GITHUB_TOKEN:-}" ]]; then
    echo "ERROR: GITHUB_TOKEN is required for bun install (GitHub Packages)." >&2
    exit 1
  fi
  export GITHUB_TOKEN
  echo "==> Web: bun install + lint"
  cd "${web_root}"
  mise exec -- bun install --frozen-lockfile
  mise exec -- bun --bun run lint
}

run_terraform_checks() {
  local tf_root="$1"
  echo "==> Terraform: init (no backend) + validate"
  cd "${tf_root}"
  mise exec -- terraform init -backend=false -input=false
  mise exec -- terraform validate
}

main() {
  ensure_mise

  mise_bootstrap_repo "${API_ROOT}"
  run_api_checks

  if web_root="$(find_sibling_repo lms-web-management)"; then
    mise_bootstrap_repo "${web_root}"
    run_web_checks "${web_root}"
  else
    echo "WARN: lms-web-management missing or no mise.toml (wrong branch?); skipping web." >&2
  fi

  if tf_root="$(find_sibling_repo lms-terraform-iac)"; then
    mise_bootstrap_repo "${tf_root}"
    run_terraform_checks "${tf_root}"
  else
    echo "WARN: lms-terraform-iac missing or no mise.toml (wrong branch?); skipping terraform." >&2
  fi

  echo "==> Cloud agent install finished."
}

main "$@"
