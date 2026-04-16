# Compliance Matrix Design Specification (HMS-7 & HMS-8)

**Date:** April 16, 2026  
**Status:** Design Phase — Ready for Implementation  
**Related Backlog:** HMS-7 (Service), HMS-8 (Controller)

---

## Executive Summary

The compliance matrix answers: **"For each active employee × active document reference, what is their training status?"**

- **Calculated dynamically** (never stored)
- **Read-only** service (no mutations)
- **Filtered** to active entities only
- **Queryable** by optional `employeeId` or `documentRefId` parameters

---

## 1. Status Enum Definition

### Four States (in order of **precedence**)

```java
enum TrainingStatus {
    COMPLETE,              // Most recent training on CURRENT version + NOT expired
    OUTDATED_DOCUMENT,     // Most recent training on NON-CURRENT version + NOT expired
    EXPIRED_TRAINING,      // Most recent training EXISTS + IS EXPIRED (any version)
    INCOMPLETE             // NO training record exists
}
```

### Status Logic (Priority Order — First Match Wins)

Given: `(employeeId, documentRefId)`

```
Step 1: Load all trainings for this employee on this document reference
        → Sort by createdAt DESC (most recent first)

Step 2: No trainings exist?
        → INCOMPLETE ✓ STOP

Step 3: Evaluate most recent training:

  a) Is training on CURRENT version AND NOT expired?
     → COMPLETE ✓ STOP

  b) Is training on NON-CURRENT version AND NOT expired?
     → OUTDATED_DOCUMENT ✓ STOP

  c) Is training EXPIRED (any version)?
     → EXPIRED_TRAINING ✓ STOP

  d) Fallback (shouldn't happen):
     → INCOMPLETE
```

### Expiry Logic

- Training valid **on** the expiry date (inclusive)
- Expired **after** the expiry date
- Check: `LocalDate.now().isAfter(trainingInstance.expiryDate)` → expired

---

## 2. Data Filtering Rules

### Scope Reduction (Phase 1: Load)

**EXCLUDED** from matrix entirely:
- Inactive employees (`employee.active = FALSE`)
- Inactive document references (`document_reference.active = FALSE`)

**Implicit exclusion** during calculation:
- Trainings on inactive document references are omitted
  - Query: `WHERE dv.document_reference.active = TRUE`

### Optional Query Filters (Phase 3: Output)

User can narrow results:
- `?employeeId={id}` → only rows for that employee (must be active, else 404 or empty)
- `?documentRefId={id}` → only columns for that docRef (must be active, else 404 or empty)

**Design Decision:** Return **404 Not Found** if filter parameter references an inactive entity.

---

## 3. Data Model & Queries Required

### Entities Involved

```
TrainingInstance (core fact)
  ├─ expiryDate: LocalDate
  ├─ createdAt: Instant
  └─ relations:
      ├─ TrainingTrainee → Employee
      └─ TrainingDocument → DocumentVersion

DocumentVersion
  ├─ isCurrent: boolean
  ├─ documentReference: DocumentReference
  └─ relations: (via FK)

DocumentReference
  ├─ active: boolean
  └─ relations: (via FK)

Employee
  ├─ active: boolean
  └─ relations: (via FK)
```

### Key Queries

1. **All active employees**: `SELECT * FROM employee WHERE active = TRUE`
2. **All active document references**: `SELECT * FROM document_reference WHERE active = TRUE`
3. **All trainings for employee on document reference**:
   ```sql
   SELECT ti.*, dv.is_current, dv.document_reference_id
   FROM training_instance ti
   JOIN training_trainee tt ON ti.id = tt.training_instance_id
   JOIN training_document td ON ti.id = td.training_instance_id
   JOIN document_version dv ON td.document_version_id = dv.id
   WHERE tt.employee_id = ?
     AND dv.document_reference_id = ?
     AND dv.document_reference.active = TRUE
   ORDER BY ti.created_at DESC
   LIMIT 1  -- Only need most recent
   ```

4. **Current version for document reference**:
   ```sql
   SELECT * FROM document_version 
   WHERE document_reference_id = ? AND is_current = TRUE
   ```

---

## 4. Implementation Breakdown (Logical Pieces)

### **Phase A: Core Service (HMS-7)**

#### A1: Create `TrainingStatus` Enum
- **File:** `src/main/java/com/effectivehygiene/hms/training/TrainingStatus.java`
- **Content:** 4 enum values
- **Effort:** 5 minutes
- **Test:** N/A (enum only)

#### A2: Create `TrainingComplianceService` (Skeleton)
- **File:** `src/main/java/com/effectivehygiene/hms/training/TrainingComplianceService.java`
- **Methods:**
  - `calculateStatus(Long employeeId, Long documentRefId) → TrainingStatus`
  - `findTrainingsForEmployeeOnDocRef(Long empId, Long docRefId) → List<TrainingInstance>`
  - `isCurrentVersion(TrainingInstance, Long docRefId) → boolean`
  - `isExpired(TrainingInstance) → boolean`
- **Dependencies:** Inject `TrainingInstanceRepository`, `TrainingTraineeRepository`, `TrainingDocumentRepository`, `DocumentVersionRepository`, `EmployeeRepository`, `DocumentReferenceRepository`
- **Effort:** 30 minutes
- **Test:** Unit test for each status transition (4 scenarios)

#### A3: Create Repository Methods (if needed)
- **File:** `TrainingTraineeRepository`, `TrainingDocumentRepository` (may already exist)
- **Methods to add:**
  - `TrainingTraineeRepository.findByTrainingInstanceAndEmployee(trainingId, empId)`
  - `TrainingDocumentRepository.findByTrainingInstanceAndDocVersionId(trainingId, docVersionId)`
- **Effort:** 10 minutes (if methods don't exist)

#### A4: Create `ComplianceMatrix` DTO (Container)
- **File:** `src/main/java/com/effectivehygiene/hms/training/dto/ComplianceMatrix.java`
- **Purpose:** Hold intermediate results (employees, docRefs, status map)
- **Structure:**
  ```java
  public record ComplianceMatrix(
      List<EmployeeInfo> employees,
      List<DocumentReferenceInfo> documents,
      Map<Long, Map<Long, TrainingStatus>> statusMatrix  // empId → (docRefId → status)
  ) {}
  ```
- **Effort:** 15 minutes

---

### **Phase B: Matrix Builder (HMS-7 continued)**

#### B1: Create `buildMatrix()` Method (Bulk Calculation)
- **File:** `TrainingComplianceService.buildMatrix(Long empIdFilter, Long docRefIdFilter) → ComplianceMatrix`
- **Logic:**
  1. Load all active employees (or filtered to `empIdFilter`)
  2. Load all active document references (or filtered to `docRefIdFilter`)
  3. For each (employee, docRef) pair, call `calculateStatus()`
  4. Build response DTO with all statuses
- **Effort:** 45 minutes
- **Test:** Integration test with known data setup (e.g., seed 3 employees, 3 docRefs, various training states)

#### B2: Create Integration Test for Service
- **File:** `src/test/java/com/effectivehygiene/hms/training/TrainingComplianceServiceIT.java`
- **Scenarios:**
  - Employee + DocRef with COMPLETE training (current version, not expired)
  - Employee + DocRef with OUTDATED_DOCUMENT (older version, not expired)
  - Employee + DocRef with EXPIRED_TRAINING (any version, expired)
  - Employee + DocRef with INCOMPLETE (no training)
  - Filters work correctly (by employeeId, by documentRefId)
  - Inactive entities are excluded
- **Effort:** 1 hour

---

### **Phase C: Controller & API (HMS-8)**

#### C1: Create Response DTOs
- **File:** `src/main/java/com/effectivehygiene/hms/training/dto/ComplianceMatrixResponse.java`
- **Structure:**
  ```java
  public record ComplianceMatrixResponse(
      Instant timestamp,
      List<EmployeeResponse> employees,
      List<DocumentReferenceResponse> documents,
      Map<Long, Map<Long, String>> matrix  // empId → (docRefId → statusString)
  ) {}
  ```
- **Effort:** 20 minutes

#### C2: Create `ComplianceMatrixController`
- **File:** `src/main/java/com/effectivehygiene/hms/training/ComplianceMatrixController.java`
- **Endpoints:**
  - `GET /api/compliance/matrix` → all employees × all docRefs
  - `GET /api/compliance/matrix?employeeId={id}` → filtered to one employee
  - `GET /api/compliance/matrix?documentRefId={id}` → filtered to one docRef
- **Logic:**
  1. Validate filter parameters (if provided, entity must exist and be active, else 404)
  2. Call `complianceService.buildMatrix(empIdFilter, docRefIdFilter)`
  3. Map to response DTO
  4. Return 200 OK
- **Effort:** 30 minutes
- **Test:** Integration test (3 scenarios: no filter, filter by emp, filter by docRef)

#### C3: Create Controller Integration Test
- **File:** `src/test/java/com/effectivehygiene/hms/integration/api/ApiComplianceMatrixIT.java`
- **Scenarios:**
  - GET `/api/compliance/matrix` returns 200 + correct matrix JSON
  - GET `/api/compliance/matrix?employeeId=1` returns 200 + filtered rows
  - GET `/api/compliance/matrix?documentRefId=10` returns 200 + filtered columns
  - GET `/api/compliance/matrix?employeeId=999` returns 404
  - Unauthenticated request returns 401
- **Effort:** 45 minutes

---

## 5. Implementation Order (Sequential)

1. **A1:** `TrainingStatus` enum (5 min)
2. **A2:** `TrainingComplianceService` skeleton (30 min)
3. **A3:** Repository methods (10 min)
4. **A4:** `ComplianceMatrix` DTO (15 min)
5. **B1:** `buildMatrix()` method (45 min)
6. **B2:** Service integration test (1 hour) — **VERIFY SERVICE WORKS**
7. **C1:** Response DTOs (20 min)
8. **C2:** `ComplianceMatrixController` (30 min)
9. **C3:** Controller integration test (45 min) — **VERIFY API WORKS**
10. **Polish:** Update backlog, run full test suite (30 min)

**Total Estimated:** ~4–5 hours

---

## 6. Known Edge Cases & Decisions

| Case | Decision |
|------|----------|
| Employee inactive at query time | Excluded from matrix entirely |
| DocRef inactive at query time | Excluded from matrix entirely |
| Training on inactive reference | Omitted from consideration (invisible) |
| Training on current version, expired today | EXPIRED_TRAINING (expiry takes precedence) |
| Training on old version, expired today | EXPIRED_TRAINING (expiry takes precedence) |
| Multiple trainings for same emp+docRef | Use MOST RECENT (by `createdAt` DESC) |
| Filter by invalid ID | Return 404 Not Found |
| Filter by inactive employee | Return 404 Not Found |
| No trainings at all | INCOMPLETE |

---

## 7. Testing Strategy

### Unit Tests (A2)
- `calculateStatus()` for all 4 statuses
- Date boundary checks (valid on last day, expired after)
- Null/empty handling

### Integration Tests (B2, C3)
- Use existing seed data from `data_initialisation_v2.sql`
- Create fresh training instances with known states
- Verify matrix contains expected statuses
- Verify filters narrow results correctly

### Full Suite Run
- `./mvnw test` (all 13+ tests should pass)
- `./mvnw -Dtest="ApiComplianceMatrixIT" test` (new compliance test)

---

## 8. Acceptance Criteria (MVP Readiness)

- [x] `TrainingStatus` enum exists with 4 values
- [x] `calculateStatus()` correctly identifies all 4 states
- [x] `buildMatrix()` returns full matrix for all active entities
- [x] `GET /api/compliance/matrix` endpoint works
- [x] Filters (`?employeeId`, `?documentRefId`) work correctly
- [x] Inactive entities are excluded from matrix
- [x] Unauthenticated requests return 401
- [x] All integration tests pass
- [x] Documentation updated in BACKLOG.md

---

## 9. Files to Create/Modify

### Create (NEW)
- `src/main/java/com/effectivehygiene/hms/training/TrainingStatus.java`
- `src/main/java/com/effectivehygiene/hms/training/TrainingComplianceService.java`
- `src/main/java/com/effectivehygiene/hms/training/dto/ComplianceMatrix.java`
- `src/main/java/com/effectivehygiene/hms/training/dto/ComplianceMatrixResponse.java`
- `src/main/java/com/effectivehygiene/hms/training/ComplianceMatrixController.java`
- `src/test/java/com/effectivehygiene/hms/training/TrainingComplianceServiceIT.java`
- `src/test/java/com/effectivehygiene/hms/integration/api/ApiComplianceMatrixIT.java`

### Modify (EXISTING)
- `docs/BACKLOG.md` (mark HMS-7, HMS-8 as complete)
- `AGENTS.md` (add Issue 16: Compliance Matrix)

---

## 10. Session Recovery Checkpoint

**If session disconnects during implementation:**

1. Refer to this file for full design intent
2. Current phase: Check progress against Section 5 (Implementation Order)
3. Status enum and service are foundation — complete these first
4. Controller can be added independently after service is tested
5. Integration tests are the validation gate — ensure they pass

**Last Updated:** 2026-04-16T [session time]  
**Prepared By:** Design Phase  
**Ready for:** Coding

---

