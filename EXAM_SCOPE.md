# Exam Scope — Training Instance + Compliance Matrix (MVP Core)

**Focus:** Training instance + derived compliance matrix for exam pass.  
**Date Updated:** April 15, 2026  
**Status:** ✅ READY TO EXECUTE

---

## What's In Scope (Critical for Exam)

### Active Tickets: HMS-1 to HMS-8, HMS-12

| Ticket | Title | Epic | Hours | Why It Matters |
|--------|-------|------|-------|----------------|
| **HMS-1** | Migrate IllegalStateException → DomainException | Foundation | 1–2 | Stabilize error handling; already partially done |
| **HMS-2** | Create Training domain models | Training | 1–2 | Core data layer (TrainingInstance, Trainee, Document) |
| **HMS-3** | Implement TrainingService (create-only) | Training | 2–3 | Business rules: validate employees, documents, expiry dates |
| **HMS-4** | Create TrainingController POST | Training | 1–2 | API to create training records (exam requirement) |
| **HMS-5** | Add TrainingService read methods | Training | 0.5–1 | Query by employee/document (support compliance matrix) |
| **HMS-6** | Create TrainingController GET | Training | 1–2 | List/detail endpoints (exam requirement) |
| **HMS-7** | Implement TrainingComplianceService | Compliance | 2–3 | **Core exam feature:** Calculate status dynamically (COMPLETE, OUTDATED_DOCUMENT, OUTDATED_TRAINING, INCOMPLETE) |
| **HMS-8** | Create ComplianceMatrixController | Compliance | 2–3 | **Core exam feature:** Expose compliance matrix (employee × document × status) |
| **HMS-12** | Wiring and smoke test | Integration | 2–3 | End-to-end validation; no regressions |

**Total Effort:** ~16–18 hours

**Recommended Order:** HMS-1 → HMS-2 → HMS-3 → HMS-4 → HMS-5 → HMS-6 → HMS-7 → HMS-8 → HMS-12

---

## What's Out of Scope (Deferred to Phase 2)

❌ **Audit Logging (HMS-9, HMS-10, HMS-11)**
- ❌ AuditLog entity + repository
- ❌ AuditService integration
- ❌ Audit API endpoints

**Reason:** Time constraints. Training + compliance matrix is critical for exam. Audit trail deferred post-exam.

---

## Critical Exam Deliverables

### ✅ 1. Training Instance API
- `POST /api/trainings` — Create training record
  - Validates employees are active
  - Validates document versions are current
  - Enforces expiry date rules
  - Returns 201 with created training

- `GET /api/trainings` — List trainings
- `GET /api/trainings/{id}` — Get training detail
- `GET /api/trainings?employeeId={id}` — Filter by employee
- `GET /api/trainings?documentRefId={id}` — Filter by document

### ✅ 2. Training Compliance Matrix API
- `GET /api/compliance/matrix` — **CORE EXAM FEATURE**
  - Returns: `{ employees: [...], documents: [...], matrix: { employeeId: { docRefId: status } } }`
  - Status values: `COMPLETE`, `OUTDATED_DOCUMENT`, `OUTDATED_TRAINING`, `INCOMPLETE`
  - Filters: `?employeeId={id}`, `?documentRefId={id}`
  - Calculated dynamically (not stored)

### ✅ 3. Training Immutability
- No update/delete endpoints for training records
- Training is create-only (append-only history)
- Compliance status is derived from current state

---

## Database Prerequisites

**CRITICAL:** Before starting HMS-2, ensure database schema is loaded:

```bash
# 1. Create database
mysql -u root -p
CREATE DATABASE hygiene_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET time_zone = '+00:00';
USE hygiene_app;

# 2. Load schema
SOURCE docs/schema/schema_v1.sql;

# 3. Verify tables exist
SHOW TABLES;
# Should include:
# - employee
# - document_reference
# - document_version
# - training_instance
# - training_trainee
# - training_document
```

**Dev App Profile:** `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

---

## Key Design Principles (Preserved in Exam Scope)

- ✅ **Soft Delete:** Employees and documents deactivated, never deleted
- ✅ **Immutability:** Training records are create-only; never updated
- ✅ **Derived Status:** Compliance calculated dynamically from current data
- ✅ **Typed Errors:** All domain errors use `DomainException` subclasses with `ErrorCode`
- ✅ **Traceability:** All operations logged via `CurrentUser.usernameOrSystem()`

---

## Testing Strategy

Each ticket requires **integration tests** matching `Phase2ErrorHandlingAndLoggingIT` patterns:

- ✅ Happy path (successful creation/retrieval)
- ✅ Validation errors (invalid input → 400 `VALIDATION_FAILED`)
- ✅ Business errors (inactive employee → 409 `INACTIVE_ENTITY`)
- ✅ Security errors (unauthenticated → 401 `UNAUTHENTICATED`)
- ✅ Not found (invalid ID → 404 `ENTITY_NOT_FOUND`)

Use `@ActiveProfiles("dev")` for tests requiring MySQL.

---

## Execution Checklist

- [ ] Load database schema (`docs/schema/schema_v1.sql`)
- [ ] HMS-1: Exception refactoring
- [ ] HMS-2: Training models (TrainingInstance, TrainingTrainee, TrainingDocument)
- [ ] HMS-3: TrainingService with business rules
- [ ] HMS-4: TrainingController POST endpoint
- [ ] HMS-5: TrainingService read methods
- [ ] HMS-6: TrainingController GET endpoints
- [ ] HMS-7: **TrainingComplianceService** (status calculation engine)
- [ ] HMS-8: **ComplianceMatrixController** (core exam endpoint)
- [ ] HMS-12: End-to-end smoke test + integration validation
- [ ] **Exam Ready:** Train + compliance matrix working end-to-end

---

## Timeline Estimate

| Scenario | Hours | Days |
|----------|-------|------|
| 1 developer, 8 hours/day | 16–18 | 2–2.5 |
| 2 developers, parallel | 8–10 | 1–1.5 |
| With unforeseen issues | 20–24 | 2.5–3 |

**Target:** Complete by exam date with buffer for testing.

---

## Phase 2 Roadmap (Post-Exam)

Once exam is passed, Phase 2 includes:

1. **HMS-9, HMS-10, HMS-11:** Audit logging (append-only trail)
2. **User Module:** Database-backed auth (replace in-memory)
3. **Flyway Integration:** Migrate schema to code-managed migrations
4. **Advanced Features:** Tasks, KPI dashboard, file uploads, etc.

---

## Success Criteria for Exam

✅ **Training Instance Management:**
- Create, retrieve, list training records
- Validate business rules (active employees, current documents)
- Immutability enforced (no edit/delete)

✅ **Training Compliance Matrix:**
- Calculate status for each employee × document pair
- Display in matrix format (derived, not stored)
- Filter by employee or document reference

✅ **API Contract:**
- All endpoints return JSON with `ApiErrorResponse` envelope
- Error codes are stable and meaningful (`VALIDATION_FAILED`, `INACTIVE_ENTITY`, etc.)
- Authentication required for all `/api/**` endpoints

✅ **Code Quality:**
- Integration tests demonstrate all acceptance criteria
- Error handling tested (401, 400, 409, 404, 500 scenarios)
- No regressions in existing employee/document endpoints

---

**Ready to start? Begin with database schema setup, then execute HMS-1 → HMS-12 in order.**

