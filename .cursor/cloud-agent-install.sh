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

# Build pods often clone siblings at default branch; mise.toml lives on random/hotfix today.
SIBLING_REF="${LMS_SIBLING_REF:-random/hotfix}"

locate_sibling_dir() {
  local name="$1"
  local candidate=""
  for candidate in \
    "$(dirname "$API_ROOT")/${name}" \
    "/agent/repos/${name}" \
    "/workspace/${name}"; do
    if [[ -d "${candidate}/.git" ]]; then
      echo "${candidate}"
      return 0
    fi
  done
  return 1
}

ensure_sibling_has_mise() {
  local root="$1"
  if [[ -f "${root}/mise.toml" ]]; then
    return 0
  fi
  echo "==> ${root}: fetching ${SIBLING_REF} (mise.toml not on current branch)" >&2
  cd "${root}"
  git fetch origin "${SIBLING_REF}" >&2
  git checkout -B "${SIBLING_REF}" "origin/${SIBLING_REF}" >&2
  [[ -f "${root}/mise.toml" ]]
}

resolve_sibling_repo() {
  local name="$1"
  local dir=""
  dir="$(locate_sibling_dir "${name}")" || return 1
  ensure_sibling_has_mise "${dir}" || return 1
  echo "${dir}"
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

  if web_root="$(resolve_sibling_repo lms-web-management)"; then
    mise_bootstrap_repo "${web_root}"
    run_web_checks "${web_root}"
  else
    echo "WARN: lms-web-management missing or no mise.toml (wrong branch?); skipping web." >&2
  fi

  if tf_root="$(resolve_sibling_repo lms-terraform-iac)"; then
    mise_bootstrap_repo "${tf_root}"
    run_terraform_checks "${tf_root}"
  else
    echo "WARN: lms-terraform-iac missing or no mise.toml (wrong branch?); skipping terraform." >&2
  fi

  echo "==> Cloud agent install finished."
}

main "$@"
