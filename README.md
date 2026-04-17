# HMS — Hygiene Management System

MVP Status - Training Module Backend

HMS is a web application for management of hygiene department in food manufacturing compliant with BRC / FCCE*ISO 22000 standards.
App focuses on compliance, auditability and historical accuracy of all records.

Implemented in MVP:
- Authenticated access with DB-backed users (single admin-style role)
- Employee management with soft-delete (historical records preserved)
- Document management with soft-delete, version management, validity periods
- Document is split into Reference (Main document) and attached Version (Current and previous versions)
- Training instance - historical immutable event - Training of employees A, B, C on documents X, Y, Z was completed by trainer X on date X
- Employee x Documents training matrix from derived table that is created at the time of request. Returned statuses for each active pair:
  COMPLETE, OUTDATED_DOCUMENT, EXPIRED_TRAINING, INCOMPLETE
- UTC-first handling for timestamps and consistent API error responses for `/api/**`

Current scope notes:
- Employees are managed entities, not system users.
- Training and compliance status are derived from records; status is not stored as mutable state.
- Audit module exists in schema planning, but full audit-log feature is not yet active in runtime modules.

Future Development (Post-MVP)
Planned next phases focus on controlled expansion while preserving compliance invariants:
- Append-only audit logging module with searchable audit API
- Task scheduling and execution workflows
- KPI/dashboard reporting and compliance trend views
- Document file handling (PDF/image) and exports
- Storage of full documents instead of only document references
- Electronic signatures and notification/reminder workflows
- Extended roles/permissions and (later) multi-site support## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.x (local or Docker)

### Setup

1. **Create the database:**
   ```bash
   mysql -u root -p
   CREATE DATABASE hygiene_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Load the schema** (optional; Flyway will handle migrations):
   ```bash
   mysql -u root -p hygiene_app < docs/schema/schema_v1.sql
   ```

3. **(Optional) load sample data including login user seed:**
   ```bash
   mysql -u root -p hygiene_app < docs/schema/data_initialisation_v2.sql
   ```

4. **Run the app** (dev profile):
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   App runs on `http://localhost:8080`

5. **Login (if `data_initialisation_v2.sql` was loaded):**
   - Username: `admin`
   - Password: `admin123`

## Architecture

### Modules
- **Employee:** Manage employees (soft-delete, immutable audit trail)
- **Document:** Manage document references and versions (immutable append-only)
- **Training:** Create immutable training events (`POST /api/training/instances`)
- **User/Auth:** Database-backed login (`users` table + `UserDetailsServiceImpl`)
- **Audit:** *Planned* — append-only audit log

### Key Design Principles
- **Soft Delete:** Records are deactivated, never physically deleted
- **Single Current Version:** Document references have exactly one current version
- **Immutable History:** Training and audit records are create-only, never updated
- **UTC Everywhere:** All timestamps enforce UTC at JVM, Hibernate, and Jackson layers
- **Typed Exceptions:** Domain business errors use `DomainException` subclasses with stable `ErrorCode` values

## API Endpoints

### Employees
- `POST /api/employees` — Create
- `GET /api/employees` — List active
- `PUT /api/employees/{id}` — Update
- `PATCH /api/employees/{id}/deactivate` — Soft delete (idempotent)

### Documents
- `POST /api/documents/references` — Create reference
- `GET /api/documents/references` — List active references
- `POST /api/documents/references/{id}/versions` — Create version
- `GET /api/documents/references/{id}/versions` — List all versions
- `GET /api/documents/references/{id}/versions/current` — Get current version

### Training
- `POST /api/training/instances` — Create immutable training instance (persists instance + trainees + documents atomically)
- `GET /api/training/instances` — List all training instances (with trainee and document version IDs)
- `GET /api/training/instances/{id}` — Get single training instance by ID

### Compliance
- `GET /api/compliance/matrix` — Training compliance matrix (all active employees × active documents)
  - Optional filters: `?employeeId={id}` or `?documentRefId={id}`
  - Response includes current document version details (version, name, issue date)
  - Status values: `COMPLETE`, `OUTDATED_DOCUMENT`, `EXPIRED_TRAINING`, `INCOMPLETE`

## Error Responses

All `/api/**` errors return JSON with stable error codes:
```json
{
  "timestamp": "2026-04-15T15:30:00Z",
  "status": 409,
  "code": "INACTIVE_ENTITY",
  "message": "Cannot update inactive employee id=1",
  "path": "/api/employees/1"
}
```

## Testing

Run all tests:
```bash
./mvnw test
```

Run focused integration tests:
```bash
./mvnw -Dtest="ApiSecurityErrorResponseIT,ApiValidationErrorResponseIT,RequestLoggingMdcIT,ApiTrainingInstanceCreateIT,ApiComplianceMatrixIT" test
```

## Documentation

- **`AGENTS.md`** — Detailed architecture, domain rules, and coding patterns (for AI agents and developers)
- **`docs/POSTMAN_SETUP.md`** — Guide for testing APIs with Postman (session-based auth)
- **`docs/schema/schema_v1.sql`** — Database schema with all tables and constraints
- **`docs/schema/data_initialisation_v2.sql`** — Sample/seed data including dev login user (**must be run before first login**)
- **`src/test/java/com/effectivehygiene/hms/util/HashGeneratorTest.java`** — Utility test to generate BCrypt hashes for manual SQL seeding

## Dependencies

- **Spring Boot:** MVC, Security (session/form), JPA, Validation
- **Flyway:** Database migrations (baseline in progress)
- **MySQL Connector:** 8.2.0+
- **Jackson:** Spring Boot 4 includes both Jackson 2 (`com.fasterxml.jackson.*`) and Jackson 3 (`tools.jackson.*`)

### Boot 4 Jackson Note (Important)
- For ALL security handlers (`RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `RestLoginSuccessHandler`, `RestLoginFailureHandler`), use **Jackson 3** imports: `tools.jackson.databind.json.JsonMapper`.
- Security handlers are intentionally **self-contained** (internal `JsonMapper`) to avoid `@WebMvcTest` mapper-bean ordering issues.
- Do not auto-convert these handlers to `com.fasterxml.jackson.*` without running the focused integration tests.

## Recent Fixes (April 15-16, 2026)

- ✅ Fixed MySQL Connector CVE-2023-22102 (upgraded to 8.2.0)
- ✅ Standardized security error serialization on Boot 4/Jackson 3 conventions (`tools.jackson.*`) and removed mapper-bean coupling in security handlers
- ✅ Added proper exception handler for `InactiveEntityException` (409 CONFLICT)
- ✅ Switched authentication from in-memory credentials to DB-backed users (`UserRepository` + `UserDetailsServiceImpl`)
- ✅ Disabled Spring Boot default in-memory user (`spring.security.user.name: disabled`) to ensure DB-backed auth is used
- ✅ Fixed session persistence by explicitly configuring `HttpSessionSecurityContextRepository` in `SecurityConfig`
- ✅ Added `RestLoginSuccessHandler` (JSON 200) and `RestLoginFailureHandler` (JSON 401) to replace HTML redirect responses on login
- ✅ Added training read endpoints: `GET /api/training/instances` and `GET /api/training/instances/{id}`
- ✅ Implemented compliance matrix calculation service with 4-state status enum (COMPLETE, OUTDATED_DOCUMENT, EXPIRED_TRAINING, INCOMPLETE)
- ✅ Added `GET /api/compliance/matrix` endpoint with optional employee/document reference filters and current version details in response

See [AGENTS.md](AGENTS.md) for full architecture details and guardrails.
