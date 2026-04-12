-- =========================================================
-- HMS — SAMPLE DATA INITIALISATION
-- Version: v1
-- Purpose: DEV / DEMO DATA ONLY
-- Initialisation: mysql -u root -p hygiene_app < docs/schema/data_initialisation_v1.sql
-- =========================================================
USE hygiene_app;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------
-- CLEAN EXISTING SAMPLE DATA
-- ---------------------------------------------------------
DELETE FROM training_document;
DELETE FROM training_trainee;
DELETE FROM training_instance;
DELETE FROM document_version;
DELETE FROM document_reference;
DELETE FROM employee;


SET FOREIGN_KEY_CHECKS = 1;


-- ---------------------------------------------------------
-- EMPLOYEES (POPULAR FOOTBALLERS)
-- ---------------------------------------------------------
INSERT INTO employee (employee_number, first_name, last_name, department, job_role)
VALUES
    ('10',  'Lionel',   'Messi',       'Attack',     'Forward'),
    ('7',   'Cristiano','Ronaldo',     'Attack',     'Forward'),
    ('9',   'Robert',   'Lewandowski', 'Attack',     'Striker'),
    ('11',  'Neymar',   'Junior',      'Attack',     'Winger'),
    ('17',  'Kevin',    'De Bruyne',   'Midfield',   'Playmaker'),
    ('8',   'Toni',     'Kroos',       'Midfield',   'Central Midfielder'),
    ('10A', 'Luka',     'Modric',      'Midfield',   'Central Midfielder'),
    ('4',   'Virgil',   'van Dijk',    'Defence',    'Centre Back'),
    ('3',   'Sergio',   'Ramos',       'Defence',    'Centre Back'),
    ('1',   'Manuel',   'Neuer',       'Goalkeeping','Goalkeeper');


-- ---------------------------------------------------------
-- DOCUMENT REFERENCES (FOOTBALL TACTICS)
-- ---------------------------------------------------------

- ---------------------------------------------------------
-- DOCUMENT REFERENCES (unchanged)
-- ---------------------------------------------------------
INSERT INTO document_reference (reference_code, origin_department, mandatory)
VALUES
    ('TACT-001', 'TACTICS', TRUE),   -- High Press
    ('TACT-002', 'TACTICS', TRUE),   -- Low Block
    ('TACT-003', 'TACTICS', TRUE),   -- Counter Attack
    ('TACT-004', 'TACTICS', FALSE),  -- False 9
    ('TACT-005', 'TACTICS', TRUE),   -- Tiki Taka
    ('TACT-006', 'TACTICS', FALSE),  -- Gegenpressing
    ('TACT-007', 'TACTICS', FALSE),  -- Park the Bus
    ('TACT-008', 'TACTICS', TRUE),   -- Build from the Back
    ('TACT-009', 'TACTICS', FALSE),  -- Diamond Midfield
    ('TACT-010', 'TACTICS', FALSE);  -- Wing Overload


-- ---------------------------------------------------------
-- DOCUMENT VERSIONS
-- ---------------------------------------------------------

-- TACT-001: High Press (v1, v2)
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (1, 'v1', 'High Press', '2023-01-15', FALSE, 365),
    (1, 'v2', 'High Press – Advanced Execution', '2024-03-01', TRUE,  365);

-- TACT-002: Low Block (v1)
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (2, 'v1', 'Low Block Defensive Structure', '2022-09-10', TRUE, 365);

-- TACT-003: Counter Attack (v1, v2, v3)
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (3, 'v1', 'Counter Attack', '2021-06-01', FALSE, 365),
    (3, 'v2', 'Counter Attack – Quick Transition', '2022-11-20', FALSE, 365),
    (3, 'v3', 'Counter‑Attacking Play – Structured Transition', '2024-01-05', TRUE,  365);

-- TACT-004: False 9 (v1, v2)
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (4, 'v1', 'False 9 Role', '2020-02-12', FALSE, 180),
    (4, 'v2', 'False 9 Role – Positional Play', '2023-08-18', TRUE,  180);

-- TACT-005: Tiki Taka (v1)
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (5, 'v1', 'Tiki‑Taka Possession Play', '2019-05-01', TRUE, 365);

-- Remaining single-version documents
INSERT INTO document_version
(document_reference_id, version, document_name, version_issue_date, is_current, default_training_validity_days)
VALUES
    (6,  'v1', 'Gegenpressing High Intensity Press', '2021-10-01', TRUE, 365),
    (7,  'v1', 'Park the Bus – Deep Defensive Block', '2022-07-15', TRUE, 365),
    (8,  'v1', 'Build from the Back – Controlled Build‑Up', '2023-02-01', TRUE, 365),
    (9,  'v1', 'Diamond Midfield Structure', '2020-04-10', TRUE, 365),
    (10, 'v1', 'Wing Overload High Line', '2021-09-05', TRUE, 365);

-- ---------------------------------------------------------
-- END OF SAMPLE DATA
-- ---------------------------------------------------------