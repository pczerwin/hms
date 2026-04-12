
-- =========================================================
-- HMS — Hygiene Management System
-- MVP DATABASE SCHEMA (Manual, Pre-Flyway)
-- Version: 0.1
-- Database: MySQL 8.x
-- Charset: utf8mb4
-- Expected timezone: UTC
--
-- NOTE:
-- - This script DROPS existing tables before creation.
-- - Intended for DEV / MVP use only.
-- - Schema evolution is tracked via Git history.
-- =========================================================
-- DB creation:
-- CREATE DATABASE hygiene_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE hygiene_app;
--
-- Timezone set up: SET time_zone = '+00:00'; OR for entire DB SET GLOBAL time_zone = '+00:00';
-- Timezone verification: SELECT @@global.time_zone, @@session.time_zone;
-- Execution: mysql -u root -p hygiene_app < docs/schema/schema_v1.sql

-- ---------------------------------------------------------
-- SESSION SAFETY
-- ---------------------------------------------------------
USE hygiene_app;
SET time_zone = '+00:00';
SET FOREIGN_KEY_CHECKS = 0;


-- ---------------------------------------------------------
-- DROP EXISTING TABLES (ORDER DOES NOT MATTER)
-- ---------------------------------------------------------
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS training_document;
DROP TABLE IF EXISTS training_trainee;
DROP TABLE IF EXISTS training_instance;
DROP TABLE IF EXISTS document_version;
DROP TABLE IF EXISTS document_reference;
DROP TABLE IF EXISTS employee;
DROP TABLE IF EXISTS users;


SET FOREIGN_KEY_CHECKS = 1;


-- ---------------------------------------------------------
-- USERS (SYSTEM ACCESS)
-- ---------------------------------------------------------
CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- EMPLOYEES (TRAINING SUBJECTS)
-- ---------------------------------------------------------
CREATE TABLE employee (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          employee_number VARCHAR(20) UNIQUE,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          department VARCHAR(100),
                          job_role VARCHAR(100),
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- DOCUMENT REFERENCES (LOGICAL DOCUMENT ID)
-- ---------------------------------------------------------
CREATE TABLE document_reference (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    reference_code VARCHAR(100) NOT NULL UNIQUE,
                                    origin_department VARCHAR(50) NOT NULL,
                                    mandatory BOOLEAN NOT NULL DEFAULT FALSE,
                                    active BOOLEAN NOT NULL DEFAULT TRUE,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- DOCUMENT VERSIONS (IMMUTABLE)
-- ---------------------------------------------------------
CREATE TABLE document_version (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  document_reference_id BIGINT NOT NULL,
                                  document_name VARCHAR(255) NOT NULL,
                                  version VARCHAR(20) NOT NULL,
                                  version_issue_date DATE NOT NULL,
                                  is_current BOOLEAN NOT NULL DEFAULT FALSE,
                                  default_training_validity_days INT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_docver_reference
                                      FOREIGN KEY (document_reference_id)
                                          REFERENCES document_reference(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- TRAINING INSTANCE (IMMUTABLE EVENT)
-- ---------------------------------------------------------
CREATE TABLE training_instance (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   trainer_name VARCHAR(255) NOT NULL,
                                   trainer_type ENUM(
                                       'EMPLOYEE',
                                       'CONTRACTOR',
                                       'OEM',
                                       'INDUCTION',
                                       'CERTIFICATION'
                                       ) NOT NULL,
                                   training_start_date DATE NOT NULL,
                                   training_end_date DATE NOT NULL,
                                   training_duration VARCHAR(50),
                                   training_expiry_date DATE NOT NULL,
                                   comments TEXT,
                                   trainer_signature VARCHAR(255),
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- TRAINING ↔ EMPLOYEE (ATTENDANCE REGISTER)
-- ---------------------------------------------------------
CREATE TABLE training_trainee (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  training_instance_id BIGINT NOT NULL,
                                  employee_id BIGINT NOT NULL,

                                  CONSTRAINT fk_tt_training
                                      FOREIGN KEY (training_instance_id)
                                          REFERENCES training_instance(id),

                                  CONSTRAINT fk_tt_employee
                                      FOREIGN KEY (employee_id)
                                          REFERENCES employee(id),

                                  CONSTRAINT uq_training_employee
                                      UNIQUE (training_instance_id, employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- TRAINING ↔ DOCUMENT VERSION (MATERIALS COVERED)
-- ---------------------------------------------------------
CREATE TABLE training_document (
                                   id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                   training_instance_id BIGINT NOT NULL,
                                   document_version_id BIGINT NOT NULL,

                                   CONSTRAINT fk_td_training
                                       FOREIGN KEY (training_instance_id)
                                           REFERENCES training_instance(id),

                                   CONSTRAINT fk_td_docver
                                       FOREIGN KEY (document_version_id)
                                           REFERENCES document_version(id),

                                   CONSTRAINT uq_training_document
                                       UNIQUE (training_instance_id, document_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- AUDIT LOG (APPEND-ONLY)
-- ---------------------------------------------------------
CREATE TABLE audit_log (
                           operation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           entity_type VARCHAR(100) NOT NULL,
                           entity_id BIGINT NOT NULL,
                           operation VARCHAR(50) NOT NULL,
                           old_value TEXT,
                           new_value TEXT,
                           performed_by VARCHAR(100) NOT NULL,
                           performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ---------------------------------------------------------
-- END OF MVP SCHEMA
-- ---------------------------------------------------------
