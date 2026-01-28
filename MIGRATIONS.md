# Database Migrations Guide

## Overview

This project uses **Flyway** for database version control and migrations. Flyway automatically manages database schema changes across all environments (development, staging, production).

---

## Quick Start

### Development (H2)

```bash
# Just run the application - migrations run automatically
mvn spring-boot:run
```

### Staging/Production (PostgreSQL)

```bash
# Migrations run automatically on application startup
SPRING_PROFILES_ACTIVE=staging mvn spring-boot:run
```

---

## Migration Files

Migrations are located in: `src/main/resources/db/migration/`

### Naming Convention

```
V{version}__{description}.sql
```

**Examples:**
- `V1__Initial_Schema.sql`
- `V2__Add_Indexes_And_Constraints.sql`
- `V3__Seed_Data.sql`
- `V4__Add_User_Preferences.sql`

**Rules:**
- Version numbers must be unique and sequential
- Use double underscore `__` between version and description
- Description should be in PascalCase or snake_case
- Once applied, migrations are **immutable** (never edit them)

---

## Current Migrations

### V1: Initial Schema
Creates core tables:
- `users` - User accounts and authentication
- `questions` - Question bank
- `question_options` - MCQ options
- `tests` - Test definitions
- `test_questions` - Test-question mapping
- `test_attempts` - User test attempts
- `user_answers` - User responses

### V2: Indexes and Constraints
Adds:
- Performance indexes for common queries
- Check constraints for data integrity
- Triggers for `updated_at` timestamps (PostgreSQL)

### V3: Seed Data
Inserts:
- Default admin user (email: `admin@upsc-ai.com`, password: `admin123`)
- Sample test user (email: `test@upsc-ai.com`, password: `test123`)
- Sample questions and test

---

## Creating New Migrations

### Step 1: Create Migration File

```bash
# Create new migration file
touch src/main/resources/db/migration/V4__Your_Description.sql
```

### Step 2: Write SQL

```sql
-- V4__Add_User_Preferences.sql

CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    preference_key VARCHAR(100) NOT NULL,
    preference_value TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, preference_key)
);

CREATE INDEX idx_user_preferences_user_id ON user_preferences(user_id);
```

### Step 3: Test Migration

```bash
# Run application to apply migration
mvn spring-boot:run

# Check migration status
curl http://localhost:8080/actuator/flyway | jq
```

---

## Migration Workflow

### Development Workflow

1. **Create migration file** with next version number
2. **Write SQL** for schema changes
3. **Run application** - Flyway applies migration automatically
4. **Verify** in H2 console: http://localhost:8080/h2-console
5. **Commit** migration file to Git

### Staging/Production Workflow

1. **Test in development first**
2. **Deploy to staging** - migrations run automatically
3. **Verify** staging database
4. **Deploy to production** - migrations run automatically
5. **Monitor** application logs

---

## Checking Migration Status

### Via Actuator Endpoint

```bash
# Get migration history
curl http://localhost:8080/actuator/flyway | jq

# Expected output
{
  "contexts": {
    "application": {
      "flywayBeans": {
        "flyway": {
          "migrations": [
            {
              "type": "SQL",
              "version": "1",
              "description": "Initial Schema",
              "state": "SUCCESS"
            }
          ]
        }
      }
    }
  }
}
```

### Via Database

```sql
-- Check migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

---

## Environment-Specific Behavior

| Feature | Development | Staging | Production |
|---------|------------|---------|------------|
| **Auto-migrate** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Validate** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Clean allowed** | ✅ Yes | ❌ No | ❌ No |
| **Baseline** | ✅ Yes | ✅ Yes | ✅ Yes |
| **Out-of-order** | ✅ Allowed | ✅ Allowed | ❌ Not allowed |

---

## Common Operations

### Clean Database (Development Only)

```bash
# WARNING: This deletes all data!
mvn flyway:clean

# Then re-run migrations
mvn spring-boot:run
```

### Repair Migration History

If a migration fails:

```bash
# Mark failed migration as deleted
mvn flyway:repair

# Fix the SQL in the migration file
# Then re-run
mvn spring-boot:run
```

### Baseline Existing Database

For databases that already have tables:

```bash
# Set baseline version
export FLYWAY_BASELINE_VERSION=1
export FLYWAY_BASELINE_ON_MIGRATE=true

# Run application
mvn spring-boot:run
```

---

## Troubleshooting

### Migration Failed

**Error:** `Migration checksum mismatch`

**Solution:**
```bash
# Never edit applied migrations!
# If you must, use repair:
mvn flyway:repair
```

---

### Out of Order Migration

**Error:** `Detected resolved migration not applied to database`

**Solution:**
```bash
# In development, allow out-of-order
export FLYWAY_OUT_OF_ORDER=true

# In production, fix version numbers
```

---

### Database Already Has Tables

**Error:** `Found non-empty schema(s) without schema history table`

**Solution:**
```bash
# Enable baseline
export FLYWAY_BASELINE_ON_MIGRATE=true
mvn spring-boot:run
```

---

### H2 vs PostgreSQL Compatibility

Some SQL features differ between H2 and PostgreSQL:

**PostgreSQL-specific features:**
- `BIGSERIAL` (use `BIGINT AUTO_INCREMENT` for H2)
- Triggers and functions
- Advanced constraints

**Solution:** Migrations are designed to work with both, but triggers (V2) only work with PostgreSQL.

---

## Best Practices

### ✅ DO

- **Test migrations in development first**
- **Keep migrations small and focused**
- **Use descriptive names**
- **Never edit applied migrations**
- **Back up database before production migrations**
- **Review migration SQL carefully**

### ❌ DON'T

- **Don't edit applied migrations**
- **Don't skip version numbers**
- **Don't use database-specific syntax unless necessary**
- **Don't run `flyway:clean` in production**
- **Don't commit broken migrations**

---

## Migration Checklist

Before deploying a new migration:

- [ ] Migration tested in development (H2)
- [ ] Migration tested with PostgreSQL
- [ ] Migration is idempotent (can run multiple times safely)
- [ ] Migration has proper indexes
- [ ] Migration has proper constraints
- [ ] Migration is backward compatible (if needed)
- [ ] Database backup taken (production)
- [ ] Rollback plan prepared

---

## Rollback Strategy

> **Note:** Flyway Community Edition doesn't support automatic rollback.

### Manual Rollback

1. **Create undo migration:**
   ```sql
   -- V5__Undo_User_Preferences.sql
   DROP TABLE IF EXISTS user_preferences;
   ```

2. **Or restore from backup:**
   ```bash
   # PostgreSQL
   pg_restore -d upsc_ai backup.dump
   ```

---

## Configuration

### Environment Variables

```bash
# Enable/disable Flyway
FLYWAY_ENABLED=true

# Baseline for existing databases
FLYWAY_BASELINE_ON_MIGRATE=true
FLYWAY_BASELINE_VERSION=0

# Allow out-of-order (dev only)
FLYWAY_OUT_OF_ORDER=false
```

### Application Properties

See `application.yml` for Flyway configuration:
- `spring.flyway.enabled`
- `spring.flyway.locations`
- `spring.flyway.baseline-on-migrate`
- `spring.flyway.validate-on-migrate`

---

## Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health | jq '.components.db'
```

### Migration Metrics

```bash
curl http://localhost:8080/actuator/flyway | jq '.contexts.application.flywayBeans.flyway.migrations | length'
```

---

## Additional Resources

- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Flyway SQL Syntax](https://flywaydb.org/documentation/concepts/migrations#sql-based-migrations)
- [Spring Boot Flyway Integration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)

---

## Support

For issues or questions:
1. Check migration logs: `logs/application.log`
2. Check Flyway history: `SELECT * FROM flyway_schema_history`
3. Review this guide
4. Contact the development team
