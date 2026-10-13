-- liquibase formatted sql

-- changeset S_A:1779090741813-1
ALTER TABLE email_cache ADD validation_result VARCHAR(500) NULL;

