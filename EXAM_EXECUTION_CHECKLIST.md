# 📋 Exam Scope Execution Checklist

**Scope:** HMS-1 to HMS-8, HMS-12 (Training Instance + Compliance Matrix)  
**Total Effort:** 16–18 hours  
**Status:** ✅ READY

---

## Pre-Execution Setup (DO FIRST)

- [ ] Database schema loaded
  ```bash
  mysql -u root -p hygiene_app < docs/schema/schema_v1.sql
  ```
- [ ] Verify tables exist: employee, document_reference, document_version, training_instance, training_trainee, training_document
- [ ] App runs on dev profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- [ ] Can login with admin/admin123
- [ ] No compilation errors: `./mvnw clean compile -DskipTests`

---

## HMS-1: Exception Hardening (1–2 hours)

**Goal:** Migrate IllegalStateException → DomainException subclasses

- [ ] Identify any IllegalStateException in EmployeeService/DocumentService
- [ ] Create/use appropriate DomainException subclasses
- [ ] Update GlobalExceptionHandler if needed
- [ ] Write integration tests for each exception path
- [ ] Verify API returns correct error codes (404, 409, etc.)
- [ ] No regression in employee/document endpoints

**Exit Criteria:** ✅ All IllegalStateException replaced, integration tests pass

---

## HMS-2: Training Models (1–2 hours)

**Goal:** Create JPA entities for training (immutable)

- [ ] TrainingInstance entity with id, trainer_name, trainer_type ENUM, dates, expiry, comments, created_at
- [ ] TrainingTrainee entity (foreign keys to training_instance and employee)
- [ ] TrainingDocument entity (foreign keys to training_instance and document_version)
- [ ] Repositories with find methods
- [ ] Integration test: persist + read training with trainees and documents

**Exit Criteria:** ✅ Entities compile, schema mapping correct, tests pass

---

## HMS-3: TrainingService (2–3 hours)

**Goal:** Business rules for immutable training creation

- [ ] createTraining() method with all required parameters
- [ ] Validate all employees exist AND active
- [ ] Validate all document_versions exist AND current
- [ ] Validate expiry date >= start_date + validity
- [ ] All trainees share same expiry_date
- [ ] No public update/delete methods
- [ ] Integration tests for each validation rule

**Exit Criteria:** ✅ Rules enforced, tests cover all error paths

---

## HMS-4: Training POST Endpoint (1–2 hours)

**Goal:** API to create training

- [ ] POST /api/trainings with CreateTrainingRequest DTO
- [ ] @Valid validation on request
- [ ] 201 Created response with training JSON
- [ ] Error handling (400, 409, 401 scenarios)
- [ ] Integration tests for happy path + error scenarios

**Exit Criteria:** ✅ Endpoint works end-to-end, error handling correct

---

## HMS-5: Training Read Methods (0.5–1 hour)

**Goal:** Service methods for training queries

- [ ] findById(id) with EntityNotFoundException
- [ ] findByEmployeeId(employeeId) 
- [ ] findByDocumentReference(refId)
- [ ] findAll() with pagination or full list
- [ ] Integration tests for each method

**Exit Criteria:** ✅ All query methods implemented and tested

---

## HMS-6: Training GET Endpoints (1–2 hours)

**Goal:** API to retrieve training records

- [ ] GET /api/trainings (list all)
- [ ] GET /api/trainings/{id} (detail)
- [ ] GET /api/trainings?employeeId={id} (filter)
- [ ] GET /api/trainings?documentRefId={id} (filter)
- [ ] Integration tests for all scenarios

**Exit Criteria:** ✅ All endpoints work, filters tested

---

## HMS-7: Compliance Calculation (2–3 hours) ⭐ EXAM CORE

**Goal:** Calculate training status dynamically

- [ ] TrainingStatus enum (COMPLETE, OUTDATED_DOCUMENT, OUTDATED_TRAINING, INCOMPLETE)
- [ ] calculateStatus(employeeId, documentRefId) method
- [ ] Logic for each status scenario
- [ ] Handle inactive employees/documents
- [ ] No stored status field (derived only)
- [ ] Integration tests for all cases

**Exit Criteria:** ✅ Status calculation correct, edge cases handled, tests pass

---

## HMS-8: Compliance Matrix Controller (2–3 hours) ⭐ EXAM CORE

**Goal:** Expose compliance matrix API

- [ ] GET /api/compliance/matrix endpoint
- [ ] Response format with employees, documents, matrix
- [ ] Filters: ?employeeId={id}, ?documentRefId={id}
- [ ] Integration tests for all scenarios
- [ ] Verify statuses calculated correctly

**Exit Criteria:** ✅ Endpoint works, statuses correct, filters work

---

## HMS-12: Smoke Test & Integration (2–3 hours)

**Goal:** End-to-end validation; no regressions

- [ ] Full workflow: employee → document → training → compliance matrix
- [ ] Error scenarios tested (401, 400, 409, 404, 500)
- [ ] No regressions in existing employee/document endpoints
- [ ] Integration/smoke test covering full workflow

**Exit Criteria:** ✅ Full smoke test passes, no regressions, EXAM READY

---

## Final Verification

- [ ] All 9 active tickets completed
- [ ] All integration tests pass: `./mvnw test`
- [ ] No compilation errors: `./mvnw clean compile`
- [ ] App runs cleanly: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`
- [ ] Compliance matrix works end-to-end
- [ ] Error handling consistent

---

## Sign-Off

**Completed by:** _______________  
**Date:** _______________  
**Status:** ✅ EXAM READY

