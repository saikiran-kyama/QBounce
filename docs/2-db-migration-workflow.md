# QBounce — Developer Guide

---

## Part 1 — Database Migrations

### How It Works

Liquibase manages all schema changes. Every migration is a versioned SQL file in `src/main/resources/db/changelog/changes/`. Liquibase tracks which files have been applied in the `DATABASECHANGELOG` table and only runs new ones.

---

### Auto Apply (on app startup)

Liquibase runs automatically when the application starts. Controlled by:

```yaml
# application-dev.yaml
spring:
  liquibase:
    enabled: true   # ← set to true for auto-apply on startup
```

**To enable:**
Set `enabled: true` in the active profile yaml, or pass the env var:
```powershell
$env:AUTO_APPLY_MIGRATIONS = "true"
.\mvnw.cmd spring-boot:run
```

**To disable** (apply migrations manually only):
```yaml
spring:
  liquibase:
    enabled: false
```

> **Current state:** `application-dev.yaml` has `enabled: true` — so locally, every app startup automatically applies any pending migrations.

---

### Manual Apply (via script)

Use the helper script to apply migrations and regenerate jOOQ models without starting the app:

```powershell
# From project root
.\scripts\db-migrate.ps1 apply
```

This runs two steps:
1. `liquibase:update` — applies all unapplied `.mysql.sql` files to `qbounce_db_dev`
2. `jooq-codegen:generate` — regenerates `com.qbounce.generated` from the live schema

---

### Generate a New Migration (after editing an entity)

After you change a JPA entity, generate the diff SQL:

```powershell
.\scripts\db-migrate.ps1 diff
```

Or let it auto-generate on compile:
```powershell
.\mvnw.cmd compile -Pdev
```

A new file is created at:
```
src/main/resources/db/changelog/changes/migration-YYYYMMDD-HHmmss.mysql.sql
```

If no schema change is detected, the empty file is automatically deleted.

---

### Check Pending Migrations

```powershell
.\scripts\db-migrate.ps1 status
```

Shows which migration files have not yet been applied to the local DB.

---

### Settings Reference

| Setting | File | Value | Effect |
|---|---|---|---|
| `spring.liquibase.enabled` | `application-dev.yaml` | `true` | Auto-apply on every app start |
| `spring.liquibase.enabled` | `application-dev.yaml` | `false` | Manual apply only via script |
| `AUTO_APPLY_MIGRATIONS` env var | — | `true` / `false` | Overrides the yaml value at runtime |
| `spring.jpa.hibernate.ddl-auto` | `application-dev.yaml` | `none` | Hibernate does NOT touch the schema (Liquibase owns it) |

---

---

## Part 2 — How QBounce Works

QBounce is an **email validation cache service**. Its job is to validate email addresses as cheaply as possible by serving results from cache before falling back to the ZeroBounce paid API.

---

### Sample API Request & Response

The main endpoint validates a list of emails for a specific tenant. The `tenantId` is used to apply the tenant's specific acceptance policy (Layer 4).

**Request:**
```http
POST /emails/validate
Content-Type: application/json
X-API-Key: busybrainsdev

{
  "tenantId": "tenant_123",
  "data": [
    "user@gmail.com",
    "fake@mailnull.com",
    "unknown@catch-all-domain.com"
  ]
}
```

**Response:**
```json
{
  "data": [
    {
      "email": "user@gmail.com",
      "accepted": true,
      "type": "PUBLIC",
      "reason": "Valid public domain"
    },
    {
      "email": "fake@mailnull.com",
      "accepted": false,
      "type": "TEMPORARY",
      "reason": "Blocked by tenant policy for temporary domains"
    },
    {
      "email": "unknown@catch-all-domain.com",
      "accepted": false,
      "type": "UNKNOWN",
      "reason": "Catch-all domains not allowed"
    }
  ]
}
```

---

### Request Flow

For every email in the request, the service works through three layers in order, stopping as soon as it has an answer.

---

#### Layer 1 — Domain Cache

**What:** Checks if the entire domain (e.g. `mailnull.com`) is already classified.

**When it stops here:**
- Domain is `TEMPORARY` → immediately returns `invalid`. No further lookup needed.

**When it continues:**
- Domain is `PUBLIC` or `PRIVATE` → moves to Layer 2 to check the specific email address.
- Domain is unknown → also moves to Layer 2.

> **Cost: free.** No API call. Read from local DB only.

---

#### Layer 2 — Email Cache

**What:** Checks if this exact email address was validated recently (within 30 days).

**When it stops here:**
- A valid (non-expired) cache entry exists → returns the cached result (`valid` / `invalid` / `unknown`). Done.

**When it continues:**
- No entry found, or entry is older than 30 days → moves to Layer 3.

> **Cost: free.** No API call. Read from local DB only.

---

#### Layer 3 — ZeroBounce API

**What:** Calls the ZeroBounce API to validate the email for the first time.

**Steps after the API responds:**
1. ZeroBounce status is normalized to `valid`, `invalid`, or `unknown`.
2. The domain type from the response (`free_email`, `disposable`, `do_not_mail`) is written to `domain_cache` — so future emails from this domain skip Layer 3.
3. The email result is written to `email_cache` — so this exact email skips Layer 3 for the next 30 days.
4. Result is returned.

> **Cost: 1 ZeroBounce credit per email.**

---

#### Layer 4 — Tenant Config (Acceptance Decision)

**What:** After a validation result is obtained (from any of the three layers above), the tenant config determines whether that result is **accepted** for the specific tenant making the request.

The raw ZeroBounce result (`valid` / `invalid` / `unknown`) tells you what the email technically is. The tenant config tells you whether that email type is **acceptable** for that tenant's use case.

**How it works:**

Each tenant has a row in `tenant_config` with five boolean flags:

| Flag | Default | Meaning |
|---|---|---|
| `allow_public` | `true` | Accept emails from free providers (gmail, yahoo, etc.) |
| `allow_private` | `true` | Accept emails from corporate/private domains |
| `allow_temporary` | `false` | Accept disposable/throwaway emails |
| `allow_role_based` | `true` | Accept role-based addresses (info@, admin@, etc.) |
| `allow_unknown` | `false` | Accept emails where deliverability couldn't be determined |

**Decision logic:**

```
email validated as "valid"
  + domain type = PUBLIC
  + tenant.allowPublic = true
  → accepted = true

email validated as "valid"
  + domain type = TEMPORARY
  + tenant.allowTemporary = false
  → accepted = false   ← blocked by tenant policy even though email is technically valid

email validated as "unknown"
  + tenant.allowUnknown = false
  → accepted = false
```

**The key distinction:**

- The validation result (`valid` / `invalid` / `unknown`) comes from ZeroBounce or the cache — it reflects the technical state of the email.
- The `accepted` field is the **final decision** for that tenant — it reflects the tenant's business policy on top of the technical result.

A `TEMPORARY` domain email can be technically `valid` (it exists and delivers), but still be `accepted = false` for a tenant who doesn't allow throwaway addresses.

---

#### Summary

| Step | What happens | Cost |
|---|---|---|
| 1. Domain cache | Classify domain type (TEMPORARY / PUBLIC / etc.) | Free |
| 2. Email cache | Check if this email was validated in last 30 days | Free |
| 3. ZeroBounce API | Live validation (only on double cache miss) | 1 credit |
| 4. Tenant config | Apply tenant's acceptance policy to the result | Free |

**Best case:** Layer 1 hits (TEMPORARY domain) → immediate `invalid`, 0 API calls.  
**Typical case:** Layer 1 miss → Layer 2 hit → tenant config applied → 0 API calls.  
**Worst case:** Both caches miss → Layer 3 → tenant config applied → 1 API call.

---

### Email Status Values

| Status | Meaning |
|---|---|
| `valid` | Email is deliverable |
| `invalid` | Undeliverable, spam trap, abuse account, do-not-mail, or temporary domain |
| `unknown` | Catch-all domain, ZeroBounce couldn't determine, or API error |

---

### Email Cache (`email_cache` table)

Stores the result of every ZeroBounce API call so the same email is never validated twice within the validity window.

| Field | Description |
|---|---|
| `email` | Primary key — normalized to lowercase |
| `validation_result` | `valid` / `invalid` / `unknown` |
| `validated_by` | Always `"Zerobounce"` |
| `validated_at` | Timestamp of when the API result was received |
| `is_valid`, `status`, `sub_status`, `type`, `free_email`, `reason` | Raw ZeroBounce fields stored for auditing |

**Cache validity:** `30 days` (configurable via `CACHE_VALIDITY_DAYS`)  
An entry is considered valid if `validated_at > now - 30 days`.

**Resilience rules:**
- If the cache **read** fails → fall back to ZeroBounce API for all emails (request never fails)
- If the cache **write** fails → API result is still returned to the caller (failure is only logged)

---

### Domain Cache (`domain_cache` table)

Pre-classifies entire email domains so emails from known domains can be handled without an API call. Checked **before** the email cache on every request.

#### Domain Types

| Type | Meaning |
|---|---|
| `PUBLIC` | Free email provider (gmail, yahoo, outlook, etc.) |
| `TEMPORARY` | Disposable / throwaway email domain → always `invalid` |
| `PRIVATE` | Corporate or private domain |
| `ROLE_BASED` | Domains associated with role-based addresses (info@, admin@) |

#### Startup Seeding

On every app startup, `DomainCacheSeeder` automatically populates the table from two static files (skips domains that already exist):

```
src/main/resources/domains/
├── public_domains.txt     → loaded as DomainType.PUBLIC
└── temporary_domains.txt  → loaded as DomainType.TEMPORARY
```

No ZeroBounce credit is spent on emails from these pre-seeded domains.

#### Runtime Learning

After every ZeroBounce API call, the domain classification is written back to `domain_cache` (upsert — only inserts if the domain is not already known):

```
ZeroBounce response
  ├── free_email = true      → upsertPublicDomain(domain)
  ├── disposable = true      → upsertTemporaryDomain(domain)
  └── do_not_mail = true     → upsertRoleBasedDomain(domain)
```

The domain cache **grows automatically** as new addresses are validated, reducing future API calls over time.

#### Domain Cache vs Email Cache

| | `domain_cache` | `email_cache` |
|---|---|---|
| **Key** | domain (e.g. `gmail.com`) | full email (e.g. `user@gmail.com`) |
| **Scope** | Entire domain | Single address |
| **Expires** | Never (permanent) | After 30 days |
| **Priority** | Checked first | Checked second |
| **Source** | Seeded files + ZeroBounce learning | ZeroBounce API only |

---

### Dual Datasource Routing

QBounce uses two MySQL connections to separate read and write load:

| Operation | Datasource | Annotation |
|---|---|---|
| Cache lookup (`SELECT`) | **READ** replica | `@Transactional(readOnly = true)` |
| Cache write (`INSERT`) | **WRITE** primary | `@Transactional` |

The routing is transparent — services just use `@Transactional` and the AOP-based `RoutingDataSource` picks the correct connection automatically.

---

### ZeroBounce API

- Called only on **cache miss**
- Endpoint: `GET https://api.zerobounce.net/v2/validate?email=&api_key=`
- Configured via:
  ```yaml
  zerobounce:
    api:
      key: ${ZEROBOUNCE_API_KEY}
      connect-timeout-ms: 5000
      read-timeout-ms: 10000
  ```
- If the API call fails or returns an unexpected status, the result is set to `unknown` and cached to avoid hammering the API on repeated requests for a broken address.

---

### Authentication

All endpoints require the `X-API-Key` header:

```http
POST /emails/validate
X-API-Key: busybrainsdev
```

Configured via:
```yaml
api:
  security:
    key: ${API_KEY:busybrainsdev}
```
