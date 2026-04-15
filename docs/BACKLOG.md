# HMS MVP Ticket-Ready Backlog

**Goal:** Reach exam readiness with Training Instance + Derived Compliance Matrix (Audit logging deferred to Phase 2).

**Priority Scope:** HMS-1 to HMS-8 + HMS-12 (Training Core + Compliance Matrix). HMS-9-11 (Audit) deferred post-exam.

**Key Principle:** Each active ticket preserves compliance invariants (soft-delete, immutability, traceability), tests are integration-style matching `Phase2ErrorHandlingAndLoggingIT` patterns, and `/api/**` endpoints follow established error contract.

## Progress Update (2026-04-15)

- HMS-1 complete: domain exception hardening is in place (`InactiveEntityException` now mapped to `409/INACTIVE_ENTITY`).
- HMS-2 complete: `TrainingInstance`, `TrainingTrainee`, `TrainingDocument` entities and repositories implemented.
- HMS-3 in progress: training create service implemented with transactional save across `training_instance`, `training_trainee`, `training_document`; read methods pending.
- HMS-4 in progress: controller create endpoint implemented as `POST /api/training/instances` with request/response DTOs.
- Training create integration tests added: `ApiTrainingInstanceCreateIT` (201 success + 409 inactive employee contract).
- Schema/seed support updated: collation guardrails + rerun-safe temp table cleanup + multi-document training seed in `data_initialisation_v2.sql`.

---

## Epic 1: Stabilize Error Handling & Domain Exceptions

### HMS-1: Migrate IllegalStateException to DomainException subclasses
**Goal:** Replace ad-hoc `IllegalStateException` with typed domain exceptions so all business errors carry stable error codes.

**In Scope:**
- Create exception types: `EntityNotFoundException`, `ImmutableRecordException`, `InactiveEntityException` (already exist; add missing ones as needed).
- Replace `IllegalStateException` in `EmployeeService` and `DocumentService` with appropriate typed exceptions.
- Update `GlobalExceptionHandler` to map new exception types to correct HTTP status/codes.

**Out of Scope:**
- Refactor non-domain exceptions (e.g., validation, security).
- Change existing error payload format.

**Acceptance Criteria:**
- All `IllegalStateException` in services are replaced with `DomainException` subclasses.
- Each exception type has corresponding `ErrorCode` enum entry.
- HTTP status codes are consistent (e.g., not found = 404, conflict = 409).
- No raw 500 responses for expected business errors.

**Tests:**
- Integration tests for each exception path (e.g., update inactive employee returns 409 with correct code).
- Verify `ApiErrorResponse` contract (`timestamp`, `status`, `code`, `message`, `path`).
- Confirm JSON error response is returned for `/api/**` paths.

**Dependencies:** None (isolated hardening).

**Estimate:** S (1–2 hours).

**Files Affected:** `domain/exception/*.java`, `employee/EmployeeService.java`, `document/DocumentService.java`, `domain/exception/GlobalExceptionHandler.java`.

---

## Epic 2: Training Core Module (Create-Only, Immutable)

### HMS-2: Create Training domain models and persistence layer
**Status:** Complete

**Goal:** Add JPA entities for immutable training instances, attendee linkage, and document version tracking.

**In Scope:**
- Entity `TrainingInstance` (id, trainer_name, trainer_type ENUM, training_start_date, training_end_date, training_duration, training_expiry_date, comments, created_at).
- Entity `TrainingTrainee` (training_instance_id, employee_id, link only).
- Entity `TrainingDocument` (training_instance_id, document_version_id, link only).
- Repositories: `TrainingInstanceRepository`, `TrainingTraineeRepository`, `TrainingDocumentRepository`.
- Ensure schema matches `docs/schema/schema_v1.sql` (tables already defined).

**Out of Scope:**
- Business logic/rules (next ticket).
- API endpoints (next ticket).
- Audit logging (separate epic).

**Acceptance Criteria:**
- Entities compile and map correctly to existing schema.
- Foreign key constraints are respected in DDL.
- Repositories provide basic CRUD + find methods (e.g., `findByTrainingInstance`, `findByEmployeeId`).
- No setters/mutations for core fields after entity creation (use constructor/builder if needed).

**Tests:**
- Unit tests for entity creation and field access.
- Integration test: persist a training instance with trainees + documents; read it back unchanged.

**Dependencies:** Database schema must exist (check `docs/schema/schema_v1.sql` is applied; ensure dev DB has tables).

**Estimate:** S (1–2 hours).

**Files Affected:** `training/TrainingInstance.java`, `training/TrainingTrainee.java`, `training/TrainingDocument.java`, `training/*Repository.java`.

---

### HMS-3: Implement TrainingService with business rules (create-only)
**Status:** In Progress

**Goal:** Enforce training creation rules; no edit/delete flows.

**In Scope:**
- `TrainingService.createTraining(trainer_name, trainer_type, start_date, end_date, duration, trainees[], doc_versions[], expiry_date, comments)` method.
- Rules:
  - All document_versions must exist and be current (check `DocumentVersion.isCurrent() == true`).
  - All employees must exist and be active.
  - Training expiry_date must be >= start_date + default validity (or override allowed).
  - All trainees share one expiry date.
  - Immutable after creation (no update/delete methods exposed).
- Log successful creation via `CurrentUser` (audit logging happens in next epic).
- Service is `@Transactional`.

**Out of Scope:**
- API endpoint (next ticket).
- Audit log writes (separate epic).

**Acceptance Criteria:**
- Creating valid training returns instance with ID assigned.
- Creating training with non-current document version throws `ImmutableRecordException` (or domain error with code).
- Creating training with non-existent employee throws `EntityNotFoundException`.
- Creating training with inactive employee throws `InactiveEntityException`.
- No public `update()` or `delete()` methods on service.
- All trainees in instance share expiry date (cannot mix).

**Tests:**
- Integration test: valid training creation succeeds, retrieves as expected.
- Integration test: non-current document rejected with correct error code.
- Integration test: inactive employee rejected with correct error code.
- Verify immutability: attempt to call non-existent update method.

**Dependencies:** HMS-2 (models/repos), HMS-1 (exception types).

**Estimate:** M (2–3 hours).

**Files Affected:** `training/TrainingService.java`.

---

### HMS-4: Create TrainingController with POST endpoint
**Status:** In Progress

**Goal:** Expose immutable training creation API.

**In Scope:**
- `POST /api/trainings` with request DTO: `CreateTrainingRequest` (trainer_name, trainer_type, dates, trainees[], doc_versions[], expiry_date, comments).
- `POST /api/training/instances` with request DTO: `CreateTrainingInstanceRequest` (trainer fields, dates, `employeeIds[]`, `documentVersionIds[]`).
- Use `@Valid` for basic validation (non-null, date ordering).
- Call `TrainingService.createTraining()`.
- Return `201 Created` with created instance + IDs.
- Secured (requires authentication).

**Out of Scope:**
- GET list/detail endpoints (add in separate ticket).
- Update/delete endpoints (intentionally omitted per immutability).
- File uploads (out of MVP).

**Acceptance Criteria:**
- Endpoint returns `201` on success with training instance JSON.
- Invalid request (missing trainer_name, invalid dates) returns `400` with `VALIDATION_FAILED` code.
- Non-current document version returns domain error code with `4xx` status.
- Unauthenticated request returns `401` with `UNAUTHENTICATED` code.
- Response envelope matches `ApiErrorResponse` on errors.

**Tests:**
- Integration test: valid POST returns `201` with correct response shape.
- Integration test: invalid request returns `400` with validation error.
- Integration test: non-current doc returns domain error code (e.g., `IMMUTABLE_RECORD`).
- Integration test: unauthenticated request returns `401`.

**Dependencies:** HMS-3 (service), HMS-1 (exception types).

**Estimate:** S (1–2 hours).

**Files Affected:** `training/TrainingController.java`, `training/dto/CreateTrainingInstanceRequest.java`, `training/dto/TrainingInstanceResponse.java`.

---

### HMS-5: Add TrainingService read methods (list/detail, no edit)
**Goal:** Support retrieval of training records for compliance review.

**In Scope:**
- `findById(id)` → throws `EntityNotFoundException` if not found.
- `findByEmployeeId(employeeId)` → list of all training for employee.
- `findByDocumentReference(refId)` → list of training for document reference.
- `findAll()` → paginated list.
- No update/delete methods.

**Out of Scope:**
- API endpoints (next ticket).
- Filtering/advanced queries (add later if needed).

**Acceptance Criteria:**
- Read methods return correct training records.
- Non-existent ID throws `EntityNotFoundException`.
- Immutability preserved (no mutable operations exposed).

**Tests:**
- Unit/integration tests for each query method.
- Verify correct records returned for employee/document filters.

**Dependencies:** HMS-2 (models).

**Estimate:** XS (30 mins–1 hour).

**Files Affected:** `training/TrainingService.java`.

---

### HMS-6: Create TrainingController GET endpoints
**Goal:** Expose read-only training APIs.

**In Scope:**
- `GET /api/trainings` → list all (paginated or all for MVP).
- `GET /api/trainings/{id}` → detail.
- `GET /api/trainings?employeeId={id}` → filter by employee.
- `GET /api/trainings?documentRefId={id}` → filter by document reference.
- Secured (requires authentication).

**Out of Scope:**
- Complex filtering/search (add later).
- Update/delete (omitted).

**Acceptance Criteria:**
- All endpoints return correct JSON array/object.
- Unauthenticated returns `401`.
- Non-existent ID returns `404`.
- Response envelope is consistent (same shape as POST response).

**Tests:**
- Integration test: list returns all trainings.
- Integration test: detail for valid ID returns correct instance.
- Integration test: detail for invalid ID returns `404`.
- Integration test: filters work correctly (employee, doc ref).

**Dependencies:** HMS-5 (service read methods).

**Estimate:** S (1–2 hours).

**Files Affected:** `training/TrainingController.java`.

---

## Epic 3: Derived Training Compliance Matrix

### HMS-7: Implement TrainingComplianceService (derived status calculation)
**Goal:** Calculate training status per employee × document reference dynamically (not stored).

**In Scope:**
- Enum `TrainingStatus` (COMPLETE, OUTDATED_DOCUMENT, OUTDATED_TRAINING, INCOMPLETE).
- Method `calculateStatus(employeeId, documentRefId)` → returns `TrainingStatus`.
- Logic:
  - COMPLETE: employee has valid (non-expired) training on current document version.
  - OUTDATED_DOCUMENT: employee trained, but newer document version exists.
  - OUTDATED_TRAINING: employee trained on current version, but training expired.
  - INCOMPLETE: no training record exists.
- Uses `TrainingInstanceRepository`, `DocumentVersionRepository`, `EmployeeRepository` for queries.

**Out of Scope:**
- Persistence of status (derived only).
- Aggregated views (next ticket).
- API endpoint (next ticket).

**Acceptance Criteria:**
- Status calculation is correct for all four cases.
- No stored status fields; always derives from current data.
- Handles edge cases (employee inactive, document inactive, no training).
- Deterministic and repeatable (same inputs → same output).

**Tests:**
- Unit/integration tests for each status transition.
- Test scenario: training expires (clock passes expiry) → status changes to OUTDATED_TRAINING.
- Test scenario: new document version becomes current → status changes to OUTDATED_DOCUMENT.
- Test scenario: no training exists → status is INCOMPLETE.

**Dependencies:** HMS-3 (training creation), HMS-2 (models).

**Estimate:** M (2–3 hours).

**Files Affected:** `training/TrainingComplianceService.java`, `training/TrainingStatus.java`.

---

### HMS-8: Create ComplianceMatrixController with GET endpoint
**Goal:** Expose employee training compliance matrix.

**In Scope:**
- `GET /api/compliance/matrix` → returns matrix of (employee, document_ref, status).
- Optional filters: `?employeeId={id}` or `?documentRefId={id}`.
- Response format: `{ employees: [...], documents: [...], matrix: { employeeId: { docRefId: status } } }`.
- Secured (requires authentication).

**Out of Scope:**
- Real-time KPI dashboard (post-MVP).
- Drill-down details (add later).

**Acceptance Criteria:**
- Endpoint returns `200` with correct matrix JSON.
- All four status values appear correctly based on training + document state.
- Filters narrow results correctly.
- Unauthenticated returns `401`.

**Tests:**
- Integration test: matrix returns correct statuses for known setup.
- Integration test: after training expires (or new doc version), matrix updates.
- Integration test: filters work correctly.

**Dependencies:** HMS-7 (service).

**Estimate:** M (2–3 hours).

**Files Affected:** `training/ComplianceMatrixController.java`, `training/dto/ComplianceMatrixResponse.java`.

---

## Epic 4: Audit Logging (Append-Only, Compliance)

### HMS-9: Create AuditLog domain model and persistence
**Goal:** Add immutable append-only audit log for compliance traceability.

**In Scope:**
- Entity `AuditLog` (id, entity_type, entity_id, operation, old_value, new_value, performed_by, performed_at, created_at).
- Repository `AuditLogRepository` with methods: `save()`, `findByEntityType()`, `findByEntityAndId()`, `findByDateRange()`.
- Ensure schema matches `docs/schema/schema_v1.sql`.
- No update/delete on audit records (immutable append-only).

**Out of Scope:**
- Diff generation logic (next ticket).
- API endpoints (next ticket).

**Acceptance Criteria:**
- Entity maps to schema correctly.
- Repository persists and retrieves audit records.
- No public mutate/delete methods on entity/repository.

**Tests:**
- Integration test: persist audit record; read back unchanged.

**Dependencies:** Database schema must have `audit_log` table.

**Estimate:** S (1–2 hours).

**Files Affected:** `audit/AuditLog.java`, `audit/AuditLogRepository.java`.

---

### HMS-10: Implement AuditService for recording operations
**Goal:** Centralized audit log writes triggered by key operations.

**In Scope:**
- `logCreate(entityType, entityId, newValue, performedBy)`.
- `logUpdate(entityType, entityId, oldValue, newValue, performedBy)`.
- `logDeactivate(entityType, entityId, performedBy)`.
- Integration into `EmployeeService`, `DocumentService`, `TrainingService` (call from service methods after mutation).
- Use `CurrentUser.usernameOrSystem()` for actor.
- Store old/new values as JSON strings (simple serialization).

**Out of Scope:**
- Automatic aspect-based interception (keep explicit for now).
- Selective audit (audit all operations for MVP).

**Acceptance Criteria:**
- Audit log entries are created for create/update/deactivate operations.
- Entries include actor, timestamp, old/new values.
- No audit entry on read-only operations.

**Tests:**
- Integration test: create employee → audit log entry exists with correct details.
- Integration test: deactivate employee → audit log entry captured.
- Integration test: create training → audit log entry captured.

**Dependencies:** HMS-9 (model), integration into services.

**Estimate:** M (2–3 hours).

**Files Affected:** `audit/AuditService.java`, `employee/EmployeeService.java`, `document/DocumentService.java`, `training/TrainingService.java`.

---

### HMS-11: Create AuditController with GET endpoints
**Goal:** Expose audit log for compliance review.

**In Scope:**
- `GET /api/audit-logs` → list all (paginated).
- `GET /api/audit-logs?entityType={type}&entityId={id}` → filter by entity.
- `GET /api/audit-logs?startDate={date}&endDate={date}` → filter by date range.
- Secured (requires authentication).

**Out of Scope:**
- Real-time alerts (post-MVP).
- Export/report (post-MVP).

**Acceptance Criteria:**
- Endpoint returns list of audit log entries with correct fields.
- Filters work correctly.
- Unauthenticated returns `401`.
- Entries are ordered by timestamp (most recent first).

**Tests:**
- Integration test: list returns all audit entries.
- Integration test: filters narrow results.
- Integration test: date range filter works.

**Dependencies:** HMS-10 (service).

**Estimate:** S (1–2 hours).

**Files Affected:** `audit/AuditController.java`.

---

## Epic 5: Minimal UI Integration

### HMS-12: Wiring and smoke test
**Goal:** Ensure all active endpoints are accessible via UI or simple HTTP client; no major regressions.

**In Scope:**
- Wire training, compliance endpoints into simple HTML templates (or REST client smoke test).
- Verify authentication/session flow works end-to-end.
- Test error scenarios return expected JSON.
- Basic smoke test: create employee → create document version → create training → check compliance matrix.

**Out of Scope:**
- Audit endpoint wiring (deferred to Phase 2).
- Rich UI/UX (post-MVP; keep minimal).
- Task scheduling or KPI dashboard (post-MVP).
- File uploads (post-MVP).

**Acceptance Criteria:**
- All training/compliance endpoints are reachable and return correct data.
- No regressions in existing employee/document endpoints.
- Full smoke test scenario completes without errors.
- Error handling works consistently (401, 4xx, 5xx scenarios).

**Tests:**
- Integration/smoke test covering full workflow.
- Verify no breaking changes to existing APIs.

**Dependencies:** HMS-1-8 (all active tickets).

**Estimate:** M (2–3 hours).

**Files Affected:** templates, UI wiring, integration test suite.

---

## Backlog Summary

| Ticket | Title | Epic | Status | Estimate | Dependencies |
|--------|-------|------|--------|----------|--------------|
| HMS-1 | Migrate IllegalStateException to DomainException | 1 | Complete | S | None |
| HMS-2 | Create Training domain models | 2 | Complete | S | Schema |
| HMS-3 | Implement TrainingService (create-only) | 2 | In Progress | M | HMS-2, HMS-1 |
| HMS-4 | Create TrainingController POST | 2 | In Progress | S | HMS-3, HMS-1 |
| HMS-5 | Add TrainingService read methods | 2 | Planned | XS | HMS-2 |
| HMS-6 | Create TrainingController GET | 2 | Planned | S | HMS-5 |
| HMS-7 | Implement TrainingComplianceService | 3 | Planned | M | HMS-3, HMS-2 |
| HMS-8 | Create ComplianceMatrixController | 3 | Planned | M | HMS-7 |
| HMS-9 | Create AuditLog model | 4 | Deferred | S | Schema |
| HMS-10 | Implement AuditService | 4 | Deferred | M | HMS-9, integrate into services |
| HMS-11 | Create AuditController | 4 | Deferred | S | HMS-10 |
| HMS-12 | Wiring and smoke test | 5 | Planned | M | All above |

**Total Estimated Effort:** ~23–25 hours (XS + 9×S + 5×M = 0.5 + 9 + 10 = 19.5; add overhead/reviews).

**Recommended Sprint Order:** HMS-1 → HMS-2 → HMS-3 → HMS-4 → HMS-5 → HMS-6 → HMS-9 → HMS-7 → HMS-8 → HMS-10 → HMS-11 → HMS-12.

---

## Notes for Execution

- Each ticket should have a PR with integration tests before merge.
- Use `@ActiveProfiles("dev")` for tests requiring MySQL.
- Keep error handling consistent: prefer `DomainException` subclasses, return `ApiErrorResponse` JSON for `/api/**` paths.
- Audit logging should be explicit in service methods (not aspect-based for now).
- Training compliance status is **derived only** (not persisted); recalculate on each request.
- All training operations are **immutable** (create-only); no edit/delete endpoints.

---

## Phase 2 Roadmap (After Exam)

### Phase 2a: Audit Logging (Deferred from MVP)
- HMS-9: Create AuditLog domain model
- HMS-10: Implement AuditService (integrate into EmployeeService, DocumentService, TrainingService)
- HMS-11: Create AuditController with GET endpoints

### Phase 2b: Post-MVP Features
- Task scheduling and automation.
- KPI dashboard with periodic recalculation.
- File uploads and PDF export.
- Electronic signatures.
- Multi-site and role-based access control.
- Advanced reporting and data export.
- User module (replace in-memory auth with database-backed users).

