# Environment Configuration Guide

## Quick Start

We use environment-specific `.env` files like modern startups. Here's how to use them:

### For Local Development

```bash
# Copy the development template to .env.local
cp .env.development .env.local

# Edit if needed (optional - works out of the box)
nano .env.local

# Run the application
mvn spring-boot:run
```

The app will automatically load `.env.local` if it exists, otherwise it uses `.env.development`.

---

## Environment Files

| File | Purpose | Committed to Git? | When to Use |
|------|---------|-------------------|-------------|
| `.env.development` | Development template | ✅ Yes | Local development |
| `.env.staging` | Staging template | ✅ Yes | Staging deployment |
| `.env.production` | Production template | ✅ Yes | Production deployment |
| `.env.local` | Your personal overrides | ❌ No | Your local machine |
| `.env` | Active environment | ❌ No | Runtime (auto-generated) |

---

## How It Works

### Development (Default)

```bash
# Option 1: Use .env.development directly
mvn spring-boot:run

# Option 2: Create .env.local for personal settings
cp .env.development .env.local
# Edit .env.local with your API keys
mvn spring-boot:run
```

### Staging

```bash
# Copy staging template
cp .env.staging .env.local

# Fill in actual staging credentials
nano .env.local

# Deploy
docker-compose -f docker-compose.staging.yml up -d
```

### Production

```bash
# NEVER use .env files in production!
# Use environment variables or secrets manager instead

# AWS Example:
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=$(aws secretsmanager get-secret-value --secret-id prod/db/host)
export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id prod/db/password)

# Run
java -jar target/upsc-backend.jar
```

---

## File Priority (Highest to Lowest)

1. **System Environment Variables** (highest priority)
2. `.env.local` (your personal overrides, gitignored)
3. `.env.{profile}` (e.g., `.env.development`)
4. `.env` (fallback, gitignored)
5. `application-{profile}.yml` (Spring defaults)

---

## Common Workflows

### New Developer Onboarding

```bash
# 1. Clone repo
git clone <repo-url>
cd upsc-backend

# 2. Copy development env (optional)
cp .env.development .env.local

# 3. Add your API keys (if you have them)
nano .env.local
# Add GOOGLE_CLIENT_ID, OPENAI_API_KEY, etc.

# 4. Run
mvn spring-boot:run
```

### Switching Environments

```bash
# Development
cp .env.development .env.local
mvn spring-boot:run

# Staging (with Docker)
cp .env.staging .env.local
# Edit .env.local with staging credentials
docker-compose -f docker-compose.staging.yml up -d
```

### Testing with Different Databases

```bash
# Use H2 (default)
cp .env.development .env.local
mvn spring-boot:run

# Use local PostgreSQL
cp .env.development .env.local
# Edit .env.local:
# DB_URL=jdbc:postgresql://localhost:5432/upsc_ai_dev
# DB_USERNAME=postgres
# DB_PASSWORD=postgres
mvn spring-boot:run
```

---

## Security Best Practices

### ✅ DO

- ✅ Commit `.env.development`, `.env.staging`, `.env.production` (templates)
- ✅ Use `.env.local` for personal/sensitive values
- ✅ Use secrets manager in production (AWS Secrets Manager, etc.)
- ✅ Rotate secrets regularly
- ✅ Use different credentials for each environment

### ❌ DON'T

- ❌ Commit `.env.local` or `.env` to git
- ❌ Put real secrets in template files
- ❌ Use development secrets in production
- ❌ Share your `.env.local` file
- ❌ Use `.env` files in production (use secrets manager)

---

## Example: Adding Your Google OAuth Credentials

```bash
# 1. Get credentials from Google Cloud Console
# https://console.cloud.google.com/

# 2. Copy development template
cp .env.development .env.local

# 3. Edit .env.local
nano .env.local

# 4. Replace these lines:
GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-actual-client-secret

# 5. Save and run
mvn spring-boot:run
```

---

## Troubleshooting

### Environment not loading?

```bash
# Check which profile is active
curl http://localhost:8080/actuator/info | jq '.app.environment'

# Force a specific profile
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Variables not being picked up?

```bash
# Check if .env.local exists
ls -la .env.local

# Check file contents
cat .env.local

# Make sure no spaces around = sign
# ✅ CORRECT: DB_HOST=localhost
# ❌ WRONG:   DB_HOST = localhost
```

### Need to reset to defaults?

```bash
# Remove your local overrides
rm .env.local

# Use template directly
mvn spring-boot:run
```

---

## Advanced: Loading .env Files

### With Maven

```bash
# Maven automatically loads .env.local if it exists
mvn spring-boot:run
```

### With Java

```bash
# Load .env.local
export $(cat .env.local | xargs)
java -jar target/upsc-backend.jar
```

### With Docker

```bash
# Docker Compose automatically loads .env file
docker-compose up

# Or specify env file
docker-compose --env-file .env.staging up
```

---

## Summary

**For Development:**
```bash
cp .env.development .env.local  # Optional
mvn spring-boot:run
```

**For Staging:**
```bash
cp .env.staging .env.local
# Edit with real staging credentials
docker-compose -f docker-compose.staging.yml up
```

**For Production:**
```bash
# Use secrets manager, NOT .env files!
export SPRING_PROFILES_ACTIVE=prod
# Set other env vars from secrets manager
java -jar app.jar
```

---

## Need Help?

- Check `ENVIRONMENTS.md` for detailed environment documentation
- Check `.env.example` for all available variables
- Check `application.yml` for default values
