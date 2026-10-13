-- liquibase formatted sql

-- changeset S_A:1779088537694-1
ALTER TABLE contact_record ADD middle_name VARCHAR(100) NULL;

-- changeset S_A:1779088537694-2
ALTER TABLE contact_record ADD testingcol VARCHAR(255) NULL;

-- changeset S_A:1779088537694-3
DROP TABLE flyway_schema_history;

-- changeset S_A:1779088537694-4
ALTER TABLE email_cache DROP COLUMN validation_result;

