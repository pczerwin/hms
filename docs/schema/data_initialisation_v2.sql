-- =========================================================
-- HMS - SAMPLE DATA INITIALISATION
-- Version: v2
-- Purpose: DEV / DEMO DATA ONLY
-- Initialisation: mysql -u root -p hygiene_app < docs/schema/data_initialisation_v2.sql
-- =========================================================

USE hygiene_app;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

-- Ensure reruns in the same session do not fail on temp table creation.
DROP TEMPORARY TABLE IF EXISTS tmp_training_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_training_trainee_seed;
DROP TEMPORARY TABLE IF EXISTS tmp_training_document_seed;

-- ---------------------------------------------------------
-- CLEAN EXISTING SAMPLE DATA
-- ---------------------------------------------------------
DELETE FROM training_document;
DELETE FROM training_trainee;
DELETE FROM training_instance;
DELETE FROM document_version;
DELETE FROM document_reference;
DELETE FROM employee;
DELETE FROM users;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- SYSTEM USERS (LOGIN CREDENTIALS)
-- password: admin123 (BCrypt cost 10)
-- To regenerate: run HashGeneratorTest in the test suite
-- ---------------------------------------------------------
INSERT INTO users (username, password_hash, active) VALUES
    ('admin', '$2a$10$DJAHukX108M13ngCZpQQwOsDMHwGbGvHRc3NYBAZvkEvclXBh8cwq', TRUE);

-- ---------------------------------------------------------
-- EMPLOYEES (OPERATIVES)
-- ---------------------------------------------------------
INSERT INTO employee (employee_number, first_name, last_name, department, job_role)
VALUES
    ('10',  'Lionel',    'Messi',       'Attack',      'Forward'),
    ('7',   'Cristiano', 'Ronaldo',     'Attack',      'Forward'),
    ('9',   'Robert',    'Lewandowski', 'Attack',      'Striker'),
    ('11',  'Neymar',    'Junior',      'Attack',      'Winger'),
    ('17',  'Kevin',     'De Bruyne',   'Midfield',    'Playmaker'),
    ('8',   'Toni',      'Kroos',       'Midfield',    'Central Midfielder'),
    ('10A', 'Luka',      'Modric',      'Midfield',    'Central Midfielder'),
    ('4',   'Virgil',    'van Dijk',    'Defence',     'Centre Back'),
    ('3',   'Sergio',    'Ramos',       'Defence',     'Centre Back'),
    ('1',   'Manuel',    'Neuer',       'Goalkeeping', 'Goalkeeper');

-- ---------------------------------------------------------
-- DOCUMENT REFERENCES
-- ---------------------------------------------------------
INSERT INTO document_reference (reference_code, origin_department, mandatory)
VALUES
    ('TACT-001', 'TACTICS', TRUE),
    ('TACT-002', 'TACTICS', TRUE),
    ('TACT-003', 'TACTICS', TRUE),
    ('TACT-004', 'TACTICS', FALSE),
    ('TACT-005', 'TACTICS', TRUE),
    ('TACT-006', 'TACTICS', FALSE),
    ('TACT-007', 'TACTICS', FALSE),
    ('TACT-008', 'TACTICS', TRUE),
    ('TACT-009', 'TACTICS', FALSE),
    ('TACT-010', 'TACTICS', FALSE);

-- ---------------------------------------------------------
-- DOCUMENT VERSIONS
-- All versions use default_training_validity_days = 365.
-- ---------------------------------------------------------
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
SELECT dr.id, x.version, x.document_name, x.version_issue_date, x.is_current, 365
FROM (
    SELECT 'TACT-001' AS reference_code, 'v1' AS version, 'High Press' AS document_name, '2023-01-15' AS version_issue_date, FALSE AS is_current
    UNION ALL SELECT 'TACT-001', 'v2', 'High Press Advanced Execution', '2024-03-01', TRUE
    UNION ALL SELECT 'TACT-002', 'v1', 'Low Block Defensive Structure', '2022-09-10', TRUE
    UNION ALL SELECT 'TACT-003', 'v1', 'Counter Attack', '2021-06-01', FALSE
    UNION ALL SELECT 'TACT-003', 'v2', 'Counter Attack Quick Transition', '2022-11-20', FALSE
    UNION ALL SELECT 'TACT-003', 'v3', 'Counter Attack Structured Transition', '2024-01-05', TRUE
    UNION ALL SELECT 'TACT-004', 'v1', 'False 9 Role', '2020-02-12', FALSE
    UNION ALL SELECT 'TACT-004', 'v2', 'False 9 Role Positional Play', '2023-08-18', TRUE
    UNION ALL SELECT 'TACT-005', 'v1', 'Tiki Taka Possession Play', '2019-05-01', TRUE
    UNION ALL SELECT 'TACT-006', 'v1', 'Gegenpressing High Intensity Press', '2021-10-01', TRUE
    UNION ALL SELECT 'TACT-007', 'v1', 'Park the Bus Deep Defensive Block', '2022-07-15', TRUE
    UNION ALL SELECT 'TACT-008', 'v1', 'Build from the Back Controlled Build Up', '2023-02-01', TRUE
    UNION ALL SELECT 'TACT-009', 'v1', 'Diamond Midfield Structure', '2020-04-10', TRUE
    UNION ALL SELECT 'TACT-010', 'v1', 'Wing Overload High Line', '2021-09-05', TRUE
) x
JOIN document_reference dr ON dr.reference_code = x.reference_code;

-- ---------------------------------------------------------
-- TRAINING INSTANCE SEED MATRIX (30 rows)
-- - First 10 rows: 12-24 months ago
-- - Remaining 20 rows: 0-12 months ago
-- - training_expiry_date = training_end_date + 365 days
-- ---------------------------------------------------------
CREATE TEMPORARY TABLE tmp_training_seed (
    trainer_signature VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci PRIMARY KEY,
    trainer_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    trainer_type VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    training_start_date DATE NOT NULL,
    training_end_date DATE NOT NULL,
    training_duration VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    comments VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    trainee_signature VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL
);

INSERT INTO tmp_training_seed
(trainer_signature, trainer_name, trainer_type, training_start_date, training_end_date, training_duration, comments, trainee_signature)
VALUES
    ('TRN-SIG-001', 'Alex Foster', 'EMPLOYEE',      '2024-05-06', '2024-05-06', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-001'),
    ('TRN-SIG-002', 'Priya Shah',  'CONTRACTOR',    '2024-06-10', '2024-06-10', '3h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-002'),
    ('TRN-SIG-003', 'Jordan Bell', 'OEM',           '2024-07-15', '2024-07-15', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-003'),
    ('TRN-SIG-004', 'Marta Klein', 'CERTIFICATION', '2024-08-20', '2024-08-20', '4h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-004'),
    ('TRN-SIG-005', 'Leo Grant',   'INDUCTION',     '2024-09-18', '2024-09-18', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-005'),
    ('TRN-SIG-006', 'Alex Foster', 'EMPLOYEE',      '2024-10-14', '2024-10-14', '3h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-006'),
    ('TRN-SIG-007', 'Priya Shah',  'CONTRACTOR',    '2024-11-11', '2024-11-11', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-007'),
    ('TRN-SIG-008', 'Jordan Bell', 'OEM',           '2024-12-09', '2024-12-09', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-008'),
    ('TRN-SIG-009', 'Marta Klein', 'CERTIFICATION', '2025-01-13', '2025-01-13', '3h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-009'),
    ('TRN-SIG-010', 'Leo Grant',   'INDUCTION',     '2025-03-10', '2025-03-10', '2h', 'Refresher cycle (older bucket)', 'TRAINEE-SIG-010'),

    ('TRN-SIG-011', 'Alex Foster', 'EMPLOYEE',      '2025-04-21', '2025-04-21', '2h', 'Recent cycle', 'TRAINEE-SIG-011'),
    ('TRN-SIG-012', 'Priya Shah',  'CONTRACTOR',    '2025-05-19', '2025-05-19', '3h', 'Recent cycle', 'TRAINEE-SIG-012'),
    ('TRN-SIG-013', 'Jordan Bell', 'OEM',           '2025-06-16', '2025-06-16', '2h', 'Recent cycle', 'TRAINEE-SIG-013'),
    ('TRN-SIG-014', 'Marta Klein', 'CERTIFICATION', '2025-07-14', '2025-07-14', '4h', 'Recent cycle', 'TRAINEE-SIG-014'),
    ('TRN-SIG-015', 'Leo Grant',   'INDUCTION',     '2025-08-11', '2025-08-11', '2h', 'Recent cycle', 'TRAINEE-SIG-015'),
    ('TRN-SIG-016', 'Alex Foster', 'EMPLOYEE',      '2025-09-08', '2025-09-08', '3h', 'Recent cycle', 'TRAINEE-SIG-016'),
    ('TRN-SIG-017', 'Priya Shah',  'CONTRACTOR',    '2025-10-13', '2025-10-13', '2h', 'Recent cycle', 'TRAINEE-SIG-017'),
    ('TRN-SIG-018', 'Jordan Bell', 'OEM',           '2025-11-17', '2025-11-17', '2h', 'Recent cycle', 'TRAINEE-SIG-018'),
    ('TRN-SIG-019', 'Marta Klein', 'CERTIFICATION', '2025-12-15', '2025-12-15', '3h', 'Recent cycle', 'TRAINEE-SIG-019'),
    ('TRN-SIG-020', 'Leo Grant',   'INDUCTION',     '2026-01-12', '2026-01-12', '2h', 'Recent cycle', 'TRAINEE-SIG-020'),
    ('TRN-SIG-021', 'Alex Foster', 'EMPLOYEE',      '2026-01-26', '2026-01-26', '2h', 'Recent cycle', 'TRAINEE-SIG-021'),
    ('TRN-SIG-022', 'Priya Shah',  'CONTRACTOR',    '2026-02-02', '2026-02-02', '3h', 'Recent cycle', 'TRAINEE-SIG-022'),
    ('TRN-SIG-023', 'Jordan Bell', 'OEM',           '2026-02-16', '2026-02-16', '2h', 'Recent cycle', 'TRAINEE-SIG-023'),
    ('TRN-SIG-024', 'Marta Klein', 'CERTIFICATION', '2026-02-23', '2026-02-23', '4h', 'Recent cycle', 'TRAINEE-SIG-024'),
    ('TRN-SIG-025', 'Leo Grant',   'INDUCTION',     '2026-03-02', '2026-03-02', '2h', 'Recent cycle', 'TRAINEE-SIG-025'),
    ('TRN-SIG-026', 'Alex Foster', 'EMPLOYEE',      '2026-03-09', '2026-03-09', '3h', 'Recent cycle', 'TRAINEE-SIG-026'),
    ('TRN-SIG-027', 'Priya Shah',  'CONTRACTOR',    '2026-03-16', '2026-03-16', '2h', 'Recent cycle', 'TRAINEE-SIG-027'),
    ('TRN-SIG-028', 'Jordan Bell', 'OEM',           '2026-03-23', '2026-03-23', '2h', 'Recent cycle', 'TRAINEE-SIG-028'),
    ('TRN-SIG-029', 'Marta Klein', 'CERTIFICATION', '2026-03-30', '2026-03-30', '3h', 'Recent cycle', 'TRAINEE-SIG-029'),
    ('TRN-SIG-030', 'Leo Grant',   'INDUCTION',     '2026-04-06', '2026-04-06', '2h', 'Recent cycle', 'TRAINEE-SIG-030');

-- Supports both schemas:
-- - with trainee_signature column
-- - without trainee_signature column
SET @has_trainee_signature := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'training_instance'
      AND column_name = 'trainee_signature'
);

SET @sql_training_insert := IF(
    @has_trainee_signature > 0,
    'INSERT INTO training_instance (trainer_name, trainer_type, training_start_date, training_end_date, training_duration, training_expiry_date, comments, trainer_signature, trainee_signature, created_at)
     SELECT trainer_name, trainer_type, training_start_date, training_end_date, training_duration, DATE_ADD(training_end_date, INTERVAL 365 DAY), comments, trainer_signature, trainee_signature, UTC_TIMESTAMP()
     FROM tmp_training_seed',
    'INSERT INTO training_instance (trainer_name, trainer_type, training_start_date, training_end_date, training_duration, training_expiry_date, comments, trainer_signature, created_at)
     SELECT trainer_name, trainer_type, training_start_date, training_end_date, training_duration, DATE_ADD(training_end_date, INTERVAL 365 DAY), comments, trainer_signature, UTC_TIMESTAMP()
     FROM tmp_training_seed'
);

PREPARE stmt_training_insert FROM @sql_training_insert;
EXECUTE stmt_training_insert;
DEALLOCATE PREPARE stmt_training_insert;

-- ---------------------------------------------------------
-- TRAINING <-> EMPLOYEE (attendance)
-- Variable participation: each training instance has 1 to 4 operatives.
-- ---------------------------------------------------------
CREATE TEMPORARY TABLE tmp_training_trainee_seed (
    trainer_signature VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    employee_number VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
);

INSERT INTO tmp_training_trainee_seed (trainer_signature, employee_number)
VALUES
    ('TRN-SIG-001', '10'),
    ('TRN-SIG-002', '9'),   ('TRN-SIG-002', '11'),
    ('TRN-SIG-003', '17'),  ('TRN-SIG-003', '8'),   ('TRN-SIG-003', '10A'),
    ('TRN-SIG-004', '4'),   ('TRN-SIG-004', '3'),   ('TRN-SIG-004', '1'),   ('TRN-SIG-004', '7'),
    ('TRN-SIG-005', '10'),  ('TRN-SIG-005', '9'),
    ('TRN-SIG-006', '7'),
    ('TRN-SIG-007', '11'),  ('TRN-SIG-007', '17'),  ('TRN-SIG-007', '4'),
    ('TRN-SIG-008', '8'),   ('TRN-SIG-008', '10A'), ('TRN-SIG-008', '1'),   ('TRN-SIG-008', '3'),
    ('TRN-SIG-009', '10'),  ('TRN-SIG-009', '7'),   ('TRN-SIG-009', '9'),   ('TRN-SIG-009', '11'),
    ('TRN-SIG-010', '17'),
    ('TRN-SIG-011', '4'),   ('TRN-SIG-011', '1'),
    ('TRN-SIG-012', '10A'), ('TRN-SIG-012', '8'),   ('TRN-SIG-012', '3'),
    ('TRN-SIG-013', '10'),  ('TRN-SIG-013', '17'),  ('TRN-SIG-013', '7'),   ('TRN-SIG-013', '1'),
    ('TRN-SIG-014', '9'),
    ('TRN-SIG-015', '11'),  ('TRN-SIG-015', '4'),   ('TRN-SIG-015', '8'),   ('TRN-SIG-015', '10A'),
    ('TRN-SIG-016', '3'),   ('TRN-SIG-016', '10'),
    ('TRN-SIG-017', '1'),   ('TRN-SIG-017', '7'),   ('TRN-SIG-017', '17'),
    ('TRN-SIG-018', '9'),   ('TRN-SIG-018', '11'),  ('TRN-SIG-018', '4'),   ('TRN-SIG-018', '10'),
    ('TRN-SIG-019', '8'),
    ('TRN-SIG-020', '10A'), ('TRN-SIG-020', '3'),
    ('TRN-SIG-021', '1'),   ('TRN-SIG-021', '9'),   ('TRN-SIG-021', '17'),
    ('TRN-SIG-022', '10'),  ('TRN-SIG-022', '7'),   ('TRN-SIG-022', '11'),  ('TRN-SIG-022', '8'),
    ('TRN-SIG-023', '4'),
    ('TRN-SIG-024', '3'),   ('TRN-SIG-024', '10A'), ('TRN-SIG-024', '1'),
    ('TRN-SIG-025', '9'),   ('TRN-SIG-025', '10'),  ('TRN-SIG-025', '17'),  ('TRN-SIG-025', '4'),
    ('TRN-SIG-026', '7'),   ('TRN-SIG-026', '8'),
    ('TRN-SIG-027', '11'),  ('TRN-SIG-027', '3'),   ('TRN-SIG-027', '1'),   ('TRN-SIG-027', '10A'),
    ('TRN-SIG-028', '17'),
    ('TRN-SIG-029', '4'),   ('TRN-SIG-029', '9'),   ('TRN-SIG-029', '7'),
    ('TRN-SIG-030', '10'),  ('TRN-SIG-030', '8'),   ('TRN-SIG-030', '11'),  ('TRN-SIG-030', '1');

INSERT INTO training_trainee (training_instance_id, employee_id)
SELECT ti.id, e.id
FROM tmp_training_trainee_seed s
JOIN training_instance ti ON ti.trainer_signature = s.trainer_signature COLLATE utf8mb4_unicode_ci
JOIN employee e ON e.employee_number = s.employee_number COLLATE utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- TRAINING <-> DOCUMENT VERSION (materials covered)
-- Mixes current and outdated versions across multiple references.
-- Each training instance covers between 1 and 4 document versions.
-- ---------------------------------------------------------
CREATE TEMPORARY TABLE tmp_training_document_seed (
    trainer_signature VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    reference_code VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    version VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
);

INSERT INTO tmp_training_document_seed (trainer_signature, reference_code, version)
VALUES
    ('TRN-SIG-001',  'TACT-001', 'v1'),
    ('TRN-SIG-001',  'TACT-003', 'v1'),
    ('TRN-SIG-001',  'TACT-005', 'v1'),
    ('TRN-SIG-002',  'TACT-001', 'v2'),
    ('TRN-SIG-003',  'TACT-003', 'v1'),
    ('TRN-SIG-003',  'TACT-004', 'v1'),
    ('TRN-SIG-004',  'TACT-003', 'v2'),
    ('TRN-SIG-004',  'TACT-004', 'v2'),
    ('TRN-SIG-004',  'TACT-002', 'v1'),
    ('TRN-SIG-004',  'TACT-007', 'v1'),
    ('TRN-SIG-005',  'TACT-003', 'v3'),
    ('TRN-SIG-005',  'TACT-005', 'v1'),
    ('TRN-SIG-006',  'TACT-004', 'v1'),
    ('TRN-SIG-007',  'TACT-004', 'v2'),
    ('TRN-SIG-007',  'TACT-002', 'v1'),
    ('TRN-SIG-007',  'TACT-008', 'v1'),
    ('TRN-SIG-008',  'TACT-002', 'v1'),
    ('TRN-SIG-008',  'TACT-009', 'v1'),
    ('TRN-SIG-009',  'TACT-005', 'v1'),
    ('TRN-SIG-010',  'TACT-006', 'v1'),
    ('TRN-SIG-010',  'TACT-001', 'v2'),
    ('TRN-SIG-011',  'TACT-007', 'v1'),
    ('TRN-SIG-011',  'TACT-001', 'v1'),
    ('TRN-SIG-011',  'TACT-003', 'v2'),
    ('TRN-SIG-011',  'TACT-010', 'v1'),
    ('TRN-SIG-012',  'TACT-008', 'v1'),
    ('TRN-SIG-013',  'TACT-009', 'v1'),
    ('TRN-SIG-013',  'TACT-003', 'v3'),
    ('TRN-SIG-013',  'TACT-004', 'v2'),
    ('TRN-SIG-014',  'TACT-010', 'v1'),
    ('TRN-SIG-014',  'TACT-005', 'v1'),
    ('TRN-SIG-015',  'TACT-001', 'v2'),
    ('TRN-SIG-016',  'TACT-003', 'v3'),
    ('TRN-SIG-016',  'TACT-002', 'v1'),
    ('TRN-SIG-017',  'TACT-004', 'v2'),
    ('TRN-SIG-017',  'TACT-001', 'v2'),
    ('TRN-SIG-017',  'TACT-006', 'v1'),
    ('TRN-SIG-017',  'TACT-008', 'v1'),
    ('TRN-SIG-018',  'TACT-002', 'v1'),
    ('TRN-SIG-019',  'TACT-006', 'v1'),
    ('TRN-SIG-019',  'TACT-004', 'v1'),
    ('TRN-SIG-020',  'TACT-008', 'v1'),
    ('TRN-SIG-020',  'TACT-003', 'v2'),
    ('TRN-SIG-020',  'TACT-010', 'v1'),
    ('TRN-SIG-021',  'TACT-001', 'v1'),
    ('TRN-SIG-022',  'TACT-003', 'v2'),
    ('TRN-SIG-022',  'TACT-007', 'v1'),
    ('TRN-SIG-023',  'TACT-004', 'v1'),
    ('TRN-SIG-023',  'TACT-005', 'v1'),
    ('TRN-SIG-023',  'TACT-009', 'v1'),
    ('TRN-SIG-023',  'TACT-001', 'v2'),
    ('TRN-SIG-024',  'TACT-005', 'v1'),
    ('TRN-SIG-025',  'TACT-007', 'v1'),
    ('TRN-SIG-025',  'TACT-003', 'v3'),
    ('TRN-SIG-025',  'TACT-002', 'v1'),
    ('TRN-SIG-026',  'TACT-009', 'v1'),
    ('TRN-SIG-026',  'TACT-004', 'v2'),
    ('TRN-SIG-027',  'TACT-010', 'v1'),
    ('TRN-SIG-028',  'TACT-001', 'v2'),
    ('TRN-SIG-028',  'TACT-008', 'v1'),
    ('TRN-SIG-029',  'TACT-003', 'v3'),
    ('TRN-SIG-029',  'TACT-004', 'v2'),
    ('TRN-SIG-029',  'TACT-006', 'v1'),
    ('TRN-SIG-030',  'TACT-004', 'v2'),
    ('TRN-SIG-030',  'TACT-001', 'v1'),
    ('TRN-SIG-030',  'TACT-005', 'v1'),
    ('TRN-SIG-030',  'TACT-009', 'v1');

INSERT INTO training_document (training_instance_id, document_version_id)
SELECT ti.id, dv.id
FROM tmp_training_document_seed s
JOIN training_instance ti ON ti.trainer_signature = s.trainer_signature COLLATE utf8mb4_unicode_ci
JOIN document_reference dr ON dr.reference_code = s.reference_code COLLATE utf8mb4_unicode_ci
JOIN document_version dv
    ON dv.document_reference_id = dr.id
   AND dv.version = s.version COLLATE utf8mb4_unicode_ci;

-- ---------------------------------------------------------
-- QUICK VALIDATION QUERIES
-- ---------------------------------------------------------
SELECT COUNT(*) AS training_instances_total FROM training_instance;

SELECT
    SUM(CASE WHEN training_start_date BETWEEN '2024-04-15' AND '2025-04-15' THEN 1 ELSE 0 END) AS instances_12_to_24_months,
    SUM(CASE WHEN training_start_date > '2025-04-15'  AND training_start_date <= '2026-04-15' THEN 1 ELSE 0 END) AS instances_0_to_12_months
FROM training_instance;

SELECT
    SUM(CASE WHEN dv.is_current THEN 1 ELSE 0 END) AS current_versions_used,
    SUM(CASE WHEN NOT dv.is_current THEN 1 ELSE 0 END) AS outdated_versions_used
FROM training_document td
JOIN document_version dv ON dv.id = td.document_version_id;

SELECT
    MIN(document_count) AS min_documents_per_training,
    MAX(document_count) AS max_documents_per_training
FROM (
    SELECT ti.id, COUNT(td.document_version_id) AS document_count
    FROM training_instance ti
    LEFT JOIN training_document td ON td.training_instance_id = ti.id
    GROUP BY ti.id
) d;

SELECT
    MIN(participant_count) AS min_participants_per_training,
    MAX(participant_count) AS max_participants_per_training
FROM (
    SELECT ti.id, COUNT(tt.employee_id) AS participant_count
    FROM training_instance ti
    LEFT JOIN training_trainee tt ON tt.training_instance_id = ti.id
    GROUP BY ti.id
) p;

-- ---------------------------------------------------------
-- END OF SAMPLE DATA V2
-- ---------------------------------------------------------


