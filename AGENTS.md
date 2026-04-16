# AGENTS.md

## Project Snapshot
- Spring Boot 4.0.5 + Java 17 + Maven wrapper (`pom.xml`, `mvnw`).
- Core runtime stack: Web MVC, Spring Security (session/form login), JPA, Flyway, MySQL.
- Current implemented modules are `employee`, `document`, `training` (create flow), and `user` (DB-backed authentication). `audit` remains a placeholder.

## Product Intent (Why This App Exists)
- This is a compliance-oriented hygiene management app for managerial users (employees are managed entities, not system users).
- Primary design goal is auditability and historical traceability (BRC/QMS style), not CRUD convenience.
- Prefer data-retention semantics (soft delete, immutable records, append-only history) when designing new features.

## MVP Scope Guardrails
- In current code, `employee`, `document`, `training` create flow, and DB-backed user login are active; treat `audit` as planned/incomplete unless explicitly implemented.
- Do not add post-MVP features by default: file/PDF uploads, task scheduling, KPI automation, notifications, e-signatures, or multi-site logic.
- Keep auth/session behavior aligned with single-role admin model until role expansion is explicitly requested.
- When uncertain, prioritize preserving compliance invariants over adding richer UX behavior.

## Architecture and Boundaries
- API layer lives in `src/main/java/com/effectivehygiene/hms/*/*Controller.java`; controllers use DTOs (request/response) and delegate to services — entities are not returned directly from controllers.
- Business rules live in services (`employee/EmployeeService.java`, `document/DocumentService.java`, `training/TrainingService.java`) and are marked `@Transactional`.
- Persistence uses Spring Data repositories per aggregate (`EmployeeRepository`, `DocumentReferenceRepository`, `DocumentVersionRepository`, `UserRepository`).
- Authentication uses `security/UserDetailsServiceImpl.java` to load active users from DB (`users` table) instead of in-memory credentials.
- Startup entry point is `HmsApplication`; no separate hexagonal/adaptor split yet.

## Request + Error Flow (Important)
- All requests require authentication (`SecurityConfig.securityFilterChain`: `.anyRequest().authenticated()`).
- `/api/**` gets JSON auth failures via `RestAuthenticationEntryPoint` (401) and `RestAccessDeniedHandler` (403); non-API paths use browser login redirect.
- Controller/service exceptions are normalized by `api/error/GlobalExceptionHandler` into `ApiErrorResponse`. Services throw typed `DomainException` subclasses (e.g. `EntityNotFoundException`, `DuplicateEntityException`) — not `IllegalStateException`.

## Domain Rules You Must Preserve
- Employees are soft-deleted (`employee.Employee.active`); deactivate via `PATCH /api/employees/{id}/deactivate` is intentionally idempotent.
- Employee number must remain unique when present (`EmployeeService.create/update`).
- Document references are logical IDs (`DocumentReference.referenceCode`) and are soft-active.
- Document versions are append-only in practice; creating a new current version must unset previous current (`DocumentService.createVersion`).
- Creating a version for inactive reference reactivates that reference (`DocumentService.createVersion`).
- Training instances are immutable historical events (create-only; no edit/delete flows).
- Training creation persists atomically across `training_instance`, `training_trainee`, and `training_document` in one transaction.
- Training creation request currently includes `employeeIds` and `documentVersionIds`; all employees must be active, all versions must be current, and parent document references must be active.
- Training compliance status should be derived from records + current document version, not stored as a mutable status field.

## Timezone, DB, and Logging Conventions
- UTC is enforced at JVM, Hibernate, and Jackson layers (`config/TimeZoneConfig.java`, `application.yml`).
- Dev DB expects local MySQL `hygiene_app` (`application-dev.yml`); prod uses env vars (`application-prod.yml`).
- Flyway is enabled globally but baseline migration is intentionally minimal (`db/migration/V1__baseline.sql`); schema docs live under `docs/schema/`.
- MDC keys `requestId`, `user`, `path` are injected per request by `logging/LoggingContextFilter` and used by `logback-spring.xml` pattern.

## Build/Test Workflows
- Run app (dev profile): `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Run all tests: `./mvnw test`
- Run focused integration tests: `./mvnw -Dtest="ApiSecurityErrorResponseIT,ApiValidationErrorResponseIT,RequestLoggingMdcIT" test`
- Run training create integration tests: `./mvnw -Dtest="ApiTrainingInstanceCreateIT" test`
- Manual timezone DB verification is in `docs/minor/tz_smoke.sql` (not an automated test — requires live MySQL).

## Jackson Conventions (Boot 4 Guardrail)
- Spring Boot 4.0.5 brings **both** Jackson stacks on classpath (`com.fasterxml.jackson.*` 2.x and `tools.jackson.*` 3.x).
- For this repo's security JSON handlers (`RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`), use **Jackson 3** imports: `tools.jackson.*`.
- Keep security handlers **self-contained** (internal `JsonMapper`) instead of constructor-injecting a mapper bean; this avoids `@WebMvcTest` slice startup failures caused by mapper-bean resolution/order.
- Do **not** "auto-correct" `tools.jackson.*` to `com.fasterxml.jackson.*` in security code without a full test pass.
- If touching security JSON serialization, run:
  - `./mvnw test`
  - `./mvnw -Dtest="ApiSecurityErrorResponseIT,ApiValidationErrorResponseIT,RequestLoggingMdcIT" test`

## Coding Patterns for This Repo
- Keep REST paths under `/api/...` for API endpoints to retain JSON security error behavior.
- Training create endpoint path is `/api/training/instances`.
- When adding business errors, prefer typed `DomainException` subclasses so responses carry stable `ErrorCode` values.
- If you introduce DTO endpoints, wire `@Valid` + handler contract consistency (see phase2 integration test for expected JSON shape).
- Preserve soft-delete and "single current version" semantics; these are core compliance behaviors.
- Keep log messages useful for forensic tracing since MDC is already configured for request correlation.

## Recent Fixes (April 15-16, 2026)
- **Issue 1:** Upgraded `mysql-connector-j` from 8.0.33 → 8.2.0 to resolve CVE-2023-22102 vulnerability.
- **Issue 2:** Standardized security error serialization on Boot 4/Jackson 3 conventions (`tools.jackson.*`) and removed mapper-bean coupling in security handlers to avoid `@WebMvcTest` context failures.
- **Issue 8:** Added dedicated `@ExceptionHandler` for `InactiveEntityException` in `GlobalExceptionHandler` to return 409 CONFLICT with `ErrorCode.INACTIVE_ENTITY` instead of 500 INTERNAL_ERROR.
- **Issue 9:** Added UTF-8 collation guardrails in schema/data scripts (`docs/schema/schema_v1.sql`, `docs/schema/data_initialisation_v2.sql`) and temp-table cleanup for reruns.
- **Issue 10:** Expanded training seed data so each training instance can cover 1-4 document versions in `docs/schema/data_initialisation_v2.sql`.
- **Issue 11:** Implemented training create service orchestration with transactional save to `training_instance`, `training_trainee`, and `training_document`.
- **Issue 12:** Added training create API endpoint `POST /api/training/instances` and integration coverage in `ApiTrainingInstanceCreateIT`.
- **Issue 13:** Switched login credential source from in-memory user to DB-backed `users` table via `UserDetailsServiceImpl` + `UserRepository`, with seeded `admin` in `docs/schema/data_initialisation_v2.sql`.
