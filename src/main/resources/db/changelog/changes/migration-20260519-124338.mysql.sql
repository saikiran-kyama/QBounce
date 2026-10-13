-- liquibase formatted sql

-- changeset saiki:1779194628019-1
CREATE TABLE contact_record (email VARCHAR(255) NOT NULL, accepted BIT NULL, account_owner VARCHAR(255) NULL, annual_revenue BIGINT NULL, city VARCHAR(100) NULL, company VARCHAR(255) NULL, company_address VARCHAR(500) NULL, company_city VARCHAR(100) NULL, company_country VARCHAR(100) NULL, company_linkedin_url VARCHAR(500) NULL, company_name_for_emails VARCHAR(255) NULL, company_phone VARCHAR(50) NULL, company_state VARCHAR(100) NULL, contact_owner VARCHAR(255) NULL, corporate_phone VARCHAR(50) NULL, country VARCHAR(100) NULL, created_at datetime(6) NOT NULL, demoed BIT NULL, departments VARCHAR(255) NULL, facebook_url VARCHAR(500) NULL, first_name VARCHAR(100) NULL, free_email BIT NULL, home_phone VARCHAR(50) NULL, industry VARCHAR(255) NULL, is_valid BIT NULL, keywords TEXT NULL, last_contacted datetime(6) NULL, last_name VARCHAR(100) NULL, last_raised_at date NULL, latest_funding VARCHAR(100) NULL, latest_funding_amount BIGINT NULL, lists TEXT NULL, middle_name VARCHAR(100) NULL, mobile_phone VARCHAR(50) NULL, number_of_employees INT NULL, number_of_retail_locations INT NULL, other_phone VARCHAR(50) NULL, person_linkedin_url VARCHAR(500) NULL, primary_intent_score DECIMAL(5, 2) NULL, primary_intent_topic VARCHAR(255) NULL, reason VARCHAR(255) NULL, replied BIT NULL, secondary_email VARCHAR(255) NULL, secondary_email_source VARCHAR(100) NULL, secondary_intent_score DECIMAL(5, 2) NULL, secondary_intent_topic VARCHAR(255) NULL, seniority VARCHAR(100) NULL, seo_description TEXT NULL, stage VARCHAR(100) NULL, state VARCHAR(100) NULL, status VARCHAR(50) NULL, sub_status VARCHAR(50) NULL, technologies TEXT NULL, tertiary_email VARCHAR(255) NULL, tertiary_email_source VARCHAR(100) NULL, testingcol VARCHAR(255) NULL, title VARCHAR(150) NULL, total_funding BIGINT NULL, twitter_url VARCHAR(500) NULL, type VARCHAR(50) NULL, validated_at datetime(6) NULL, validated_by VARCHAR(50) NULL, website VARCHAR(500) NULL, work_direct_phone VARCHAR(50) NULL, CONSTRAINT contact_recordPK PRIMARY KEY (email));

-- changeset saiki:1779194628019-2
CREATE TABLE domain_cache (domain VARCHAR(255) NOT NULL, created_at datetime(6) NULL, source VARCHAR(20) NOT NULL, type ENUM NOT NULL, CONSTRAINT domain_cachePK PRIMARY KEY (domain));

-- changeset saiki:1779194628019-3
CREATE TABLE tenant_config (id BIGINT AUTO_INCREMENT NOT NULL, allow_private BIT NULL, allow_public BIT NULL, allow_role_based BIT NULL, allow_temporary BIT NULL, allow_unknown BIT NULL, tenant_id VARCHAR(255) NOT NULL, CONSTRAINT tenant_configPK PRIMARY KEY (id));

-- changeset saiki:1779194628019-4
ALTER TABLE email_cache ADD is_valid BIT NULL;

-- changeset saiki:1779194628019-5
ALTER TABLE email_cache ADD reason VARCHAR(255) NULL;

-- changeset saiki:1779194628019-6
ALTER TABLE email_cache ADD type VARCHAR(50) NULL;

-- changeset saiki:1779194628019-7
ALTER TABLE tenant_config ADD CONSTRAINT UC_TENANT_CONFIGTENANT_ID_COL UNIQUE (tenant_id);

-- changeset saiki:1779194628019-8
ALTER TABLE email_cache DROP COLUMN validated_by;

-- changeset saiki:1779194628019-9
ALTER TABLE email_cache DROP COLUMN validation_result;

