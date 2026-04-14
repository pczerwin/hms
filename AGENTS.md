# AGENTS.md

## Project Snapshot
- Spring Boot 4.0.5 + Java 17 + Maven wrapper (`pom.xml`, `mvnw`).
- Core runtime stack: Web MVC, Spring Security (session/form login), JPA, Flyway, MySQL.
- Current implemented business modules are `employee` and `document`; `training`, `audit`, and `user` packages are placeholders.

## Product Intent (Why This App Exists)
- This is a compliance-oriented hygiene management app for managerial users (employees are managed entities, not system users).
- Primary design goal is auditability and historical traceability (BRC/QMS style), not CRUD convenience.
- Prefer data-retention semantics (soft delete, immutable records, append-only history) when designing new features.

## MVP Scope Guardrails
- In current code, `employee` + `document` are active modules; treat `training`, `audit`, and `user` as planned/incomplete unless explicitly implemented.
- Do not add post-MVP features by default: file/PDF uploads, task scheduling, KPI automation, notifications, e-signatures, or multi-site logic.
- Keep auth/session behavior aligned with single-role admin model until role expansion is explicitly requested.
- When uncertain, prioritize preserving compliance invariants over adding richer UX behavior.

## Architecture and Boundaries
- API layer lives in `src/main/java/com/effectivehygiene/hms/*/*Controller.java` and is currently entity-first (controllers accept/return JPA entities directly).
- Business rules live in services (`employee/EmployeeService.java`, `document/DocumentService.java`) and are marked `@Transactional`.
- Persistence uses Spring Data repositories per aggregate (`EmployeeRepository`, `DocumentReferenceRepository`, `DocumentVersionRepository`).
- Startup entry point is `HmsApplication`; no separate hexagonal/adaptor split yet.

## Request + Error Flow (Important)
- All requests require authentication (`SecurityConfig.securityFilterChain`: `.anyRequest().authenticated()`).
- `/api/**` gets JSON auth failures via `RestAuthenticationEntryPoint` (401) and `RestAccessDeniedHandler` (403); non-API paths use browser login redirect.
- Controller/service exceptions are normalized by `domain/exception/GlobalExceptionHandler` into `ApiErrorResponse`.
- Standard error payload fields are fixed: `timestamp`, `status`, `code`, `message`, `path` (`api/error/ApiErrorResponse.java`).
- Existing services still throw `IllegalStateException`; these currently map to 500 via the catch-all handler.

## Domain Rules You Must Preserve
- Employees are soft-deleted (`employee.Employee.active`); deactivate via `PATCH /api/employees/{id}/deactivate` is intentionally idempotent.
- Employee number must remain unique when present (`EmployeeService.create/update`).
- Document references are logical IDs (`DocumentReference.referenceCode`) and are soft-active.
- Document versions are append-only in practice; creating a new current version must unset previous current (`DocumentService.createVersion`).
- Creating a version for inactive reference reactivates that reference (`DocumentService.createVersion`).
- If implementing training next, model training instances as immutable historical events (create-only; no edit/delete flows).
- Training compliance status should be derived from records + current document version, not stored as a mutable status field.

## Timezone, DB, and Logging Conventions
- UTC is enforced at JVM, Hibernate, and Jackson layers (`config/TimeZoneConfig.java`, `application.yml`).
- Dev DB expects local MySQL `hygiene_app` (`application-dev.yml`); prod uses env vars (`application-prod.yml`).
- Flyway is enabled globally but baseline migration is intentionally minimal (`db/migration/V1__baseline.sql`); schema docs live under `docs/schema/`.
- MDC keys `requestId`, `user`, `path` are injected per request by `logging/LoggingContextFilter` and used by `logback-spring.xml` pattern.

## Build/Test Workflows
- Run app (dev profile): `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- Run all tests: `./mvnw test`
- Run focused integration contract test (no DB auto-config): `./mvnw -Dtest=Phase2ErrorHandlingAndLoggingIT test`
- Manual timezone DB verification is in `docs/minor/tz_smoke.sql` (not an automated test — requires live MySQL).

## Coding Patterns for This Repo
- Keep REST paths under `/api/...` for API endpoints to retain JSON security error behavior.
- When adding business errors, prefer typed `DomainException` subclasses so responses carry stable `ErrorCode` values.
- If you introduce DTO endpoints, wire `@Valid` + handler contract consistency (see phase2 integration test for expected JSON shape).
- Preserve soft-delete and "single current version" semantics; these are core compliance behaviors.
- Keep log messages useful for forensic tracing since MDC is already configured for request correlation.

