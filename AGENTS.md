# Project Overview

**lms-core-api** is the Learning Management System HTTP backend: Spring Boot 4 on Java 25, OpenAPI-first, Spring Data JDBC against PostgreSQL, Cognito JWT as an OAuth2 resource server, and S3 (or LocalStack) for file uploads. A sibling Bun package in `openapi/` publishes a TypeScript client (`@the-fundamentals/core-openapi`) to GitHub Packages.

## Repository Structure

- `.github/` — GitHub Actions: Java build, TruffleHog, OpenAPI npm publish.
- `.cursor/rules/` — Cursor agent rules for this API (OpenAPI, services, auth, tests).
- `docker/` — Compose for LocalStack (`4566`) and PostgreSQL 16 (`5432`).
- `gradle/` — Gradle 9.5.1 wrapper distribution metadata and JAR.
- `openapi/` — Canonical `openapi.yml` plus Bun/hey-api TS client package.
- `src/main/java/` — Application code (`tech.sangdang.lmscoreapi`).
- `src/main/resources/` — `application*.yml`, `schema.sql`.
- `src/test/java/` — JUnit 5 web-slice and Mockito tests.
- `build.gradle.kts` / `settings.gradle.kts` / `gradle.properties` — Build.

Build output (`build/`, including generated OpenAPI Java) is gitignored.

## Build & Development Commands

Requires **JDK 25**. CI uses Temurin 25. Gradle **9.5.1** via the wrapper.
`compileJava` depends on `openApiGenerate`.

```bash
# Resolve deps, generate OpenAPI Java, compile, test, assemble
./gradlew build

# Tests only (JUnit Platform)
./gradlew test

# Format check / apply (Spotless + google-java-format 1.35.0)
./gradlew spotlessCheck
./gradlew spotlessApply

# Compile only (Java type-check)
./gradlew compileJava compileTestJava

# Generate Spring interfaces/models from openapi/openapi.yml
./gradlew openApiGenerate

# Run the API (default Spring profile; needs Postgres on 5432)
./gradlew bootRun
```

Local stack (Compose requires `LOCALSTACK_AUTH_TOKEN`):

```bash
docker compose -f docker/docker-compose.yml up -d
```

Use Spring profile `local` for LocalStack Cognito/S3, dummy accounts, and
`application-local.yml` storage buckets. How that profile is selected is not
documented here.

To run the application in local mode, run `./gradlew bootRun --args='--spring.profiles.active=local'`

## Code Style & Conventions

- **Format:** Spotless `googleJavaFormat("1.35.0")` and unused-import removal.
- **Generated HTTP models/APIs:** `tech.sangdang.lmscoreapi.generated.*` —
  produced from `openapi/openapi.yml`; do not edit under `build/generated/`.
- **DTO roles in the spec:** Command (writes), Query/Filter (read input),
  Response (output). Request/response bodies `$ref` `components/schemas`.
- **Controllers:** `@RestController`, `implements` generated `*Api`, inject
  contracted service interfaces only.
- **Modules** (`src/main/java/.../modules/<name>/`):

  | Package | Role |
  |---|---|
  | `api/` | REST controllers |
  | `app/` | Contracted service interface |
  | `app/impl/` | Contracted implementation |
  | `app/internal/` | `@InternalService`, Small helper classes/functions that support the contracted services |
  | `app/mappers/` | Mappers between objects |
  | `dom/` | Domain Objects + repository interfaces + ports |
  | `infra/` | Adapters (S3, seed data, Cognito helpers) to ports + infrastructure work |

- **Persistence:** Spring Data JDBC. Repositories extend
  `BaseCommandRepository` / `BaseQueryRepository`;
  `repositoryBaseClass = BaseJdbcRepositoryImpl`. Writes use `insert`/`update`.
  Schema is `src/main/resources/schema.sql` (no Flyway/Liquibase).
- **Errors:** Throw `BusinessException` subclasses
  (`ObjectNotFoundException`, `ConflictException`,
  `GenericBadRequestException`). `GlobalExceptionHandler` maps them to
  `ApiErrorResponse`.
- **Tests:** Method names `action_scenario_expectedHttp`; `@DisplayName` on
  class and test. Jackson 3 `tools.jackson.databind.json.JsonMapper` for
  MockMvc bodies. Auth helpers in
  `src/test/java/.../helpers/SecurityTestSupport.java`.
- **OpenAPI `info.version`:** CI bumps this on publish; do not change it in
  feature work.
- **Commits:** No CONTRIBUTING template. History mixes freeform titles,
  `chore: bump openapi version to x.y.z [skip ci]`, and
  `feature: … (#N)`.

## Testing Strategy

- **Unit:** JUnit 5 + Mockito. Example:
  `modules/utility/app/impl/StorageServiceImplTest` (`@ExtendWith(MockitoExtension.class)`).
- **Integration (web slice):** `@WebMvcTest` + `@Import` of
  `GlobalExceptionHandler`, real `*ServiceImpl`, MapStruct impls,
  `SecurityConfig`, and internal services the impl needs. `@MockitoBean` for
  repositories and ports. Tests target **service/business outcomes**, not
  Bean Validation or malformed JSON.
- **E2E / Testcontainers / live DB tests:** none in `src/test`.

Local:

```bash
./gradlew test
```

CI (`.github/workflows/build.yml`, every push): Temurin 25, wrapper
validation, then:

```bash
chmod a+x ./gradlew
./gradlew build
```

`.github/workflows/code-validation.yml` runs TruffleHog (verified, unknown,
unverified). Super-linter is commented out.

## Security & Compliance

- **Secrets:** Do not commit tokens. Compose fails without
  `LOCALSTACK_AUTH_TOKEN`. `openapi/.npmrc` uses `GITHUB_TOKEN` for
  `npm.pkg.github.com`. Cognito pool/client IDs appear as YAML defaults;
  override with `COGNITO_USER_POOL_ID`, `COGNITO_APP_CLIENT_ID`,
  `COGNITO_BASE_DOMAIN`, `COGNITO_CLIENT_FULL_DOMAIN`.
- **Scanning:** TruffleHog on push. No Dependabot config in this repo.
- **Authz:** Path prefix is the access model (`/admin/**`, `/private/**`);
  keep `openapi.yml` and `SecurityConfig` aligned.
- **CSRF:** Disabled (stateless JWT resource server).

## Agent Guardrails

- Do not edit `build/` or `openapi/src/` / `openapi/dist/` generated TS
  (`openapi/.gitignore` ignores `src` under that package after generate).
- Do not change `openapi/openapi.yml` `info.version`.
- Do not inject `app/internal` types into controllers.
- Do not hold S3/HTTP I/O inside `@Transactional` methods.
- Do not replace `BaseJdbcRepositoryImpl` Criteria with ad-hoc SQL unless
  the task requires it.
- Do not add tests that only assert Spring/Jakarta validation failures.
- Do not commit `.env`, credentials, or expand TruffleHog-detectable secrets.
- `@TestController` types stay profile-limited (`local`, `test`).

## Extensibility Hooks

- **New HTTP API:** add path + schemas in `openapi/openapi.yml`, regenerate,
  implement `*Api`, add contracted service + tests.
- **New module:** same package split as `account`, `management`, `utility`.
- **Outbound I/O:** a `dom/ports` interface + `infra` implementation (see
  `S3Port` / `S3PortImpl`).
- **Profiles:** `local` (dummy accounts, LocalStack storage, test controllers),
  `test` (Swagger off, test controllers). Default `application.yml` has no
  storage block; `local` sets `app.utility.storage.*` and
  `app.config.load-dummy-accounts-into-database`.
- **Env / properties:** Cognito vars above; storage via
  `app.utility.storage` (`landing-zone-bucket-name`,
  `public-store-bucket-name`, `region`, `use-dummy-credentials`,
  `cloud-mode` `AWS`|`LOCALSTACK`, optional `endpoint-override`).
- **Feature flags:** none.

## Further Reading

- [openapi/openapi.yml](openapi/openapi.yml) — HTTP contract.
- [src/main/resources/schema.sql](src/main/resources/schema.sql) — DDL.
- [src/main/java/tech/sangdang/lmscoreapi/config/SecurityConfig.java](src/main/java/tech/sangdang/lmscoreapi/config/SecurityConfig.java)
- [docker/docker-compose.yml](docker/docker-compose.yml)
- [build.gradle.kts](build.gradle.kts)
- [openapi/README.md](openapi/README.md) — Bun package stub only.
- [README.md](README.md) — empty.
