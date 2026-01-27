# Multi-Environment Configuration Guide

## Overview

The UPSC AI Backend supports three environments:
- **Development** (`dev`) - Local development
- **Staging** (`staging`) - Pre-production testing
- **Production** (`prod`) - Live application

## Quick Start

### 1. Development (Local)

```bash
# Set environment
export SPRING_PROFILES_ACTIVE=dev

# Run with Maven
mvn spring-boot:run

# Or run with Docker
docker-compose up
```

**Features:**
- H2 in-memory database
- All test endpoints enabled
- Verbose logging (DEBUG level)
- H2 console at `/h2-console`

### 2. Staging

```bash
# Create .env file from template
cp .env.example .env

# Edit .env with your staging credentials
nano .env

# Run with Docker Compose
docker-compose -f docker-compose.staging.yml up -d
```

**Features:**
- PostgreSQL database
- Production-like configuration
- Moderate logging (INFO level)
- No test endpoints

### 3. Production

```bash
# Set environment variables (use secrets manager in production)
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-rds-endpoint.amazonaws.com
export DB_USERNAME=your-db-user
export DB_PASSWORD=your-secure-password
export JWT_SECRET=your-production-jwt-secret

# Run application
java -jar target/upsc-backend-0.0.1-SNAPSHOT.jar
```

**Features:**
- PostgreSQL database (managed service)
- Minimal logging (WARN level)
- Optimized connection pooling
- No test endpoints
- Compression enabled

---

## Environment Comparison

| Feature | Development | Staging | Production |
|---------|------------|---------|------------|
| **Database** | H2 (in-memory) | PostgreSQL | PostgreSQL (RDS) |
| **Log Level** | DEBUG | INFO | WARN |
| **Show SQL** | Yes | No | No |
| **H2 Console** | Enabled | Disabled | Disabled |
| **Test Endpoints** | Enabled | Disabled | Disabled |
| **Actuator** | All endpoints | Limited | Limited |
| **Stack Traces** | Always | Never | Never |
| **CORS** | localhost | staging domain | production domain |
| **DDL Auto** | create-drop | validate | validate |

---

## Configuration Files

```
src/main/resources/
├── application.yml              # Base configuration
├── application-dev.yml          # Development overrides
├── application-staging.yml      # Staging overrides
└── application-prod.yml         # Production overrides
```

---

## Environment Variables

### Required for All Environments

```bash
SPRING_PROFILES_ACTIVE=dev|staging|prod
```

### Required for Staging/Production

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=upsc_ai
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Security
JWT_SECRET=your-secret-key

# OAuth2
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret

# External Services
OPENAI_API_KEY=your-api-key

# Frontend
FRONTEND_URL=https://your-domain.com
```

---

## Running Different Environments

### Development

```bash
# Method 1: Maven
mvn spring-boot:run

# Method 2: Java
mvn package
java -jar target/upsc-backend-0.0.1-SNAPSHOT.jar

# Method 3: Docker
docker-compose up
```

### Staging

```bash
# With Docker Compose
docker-compose -f docker-compose.staging.yml up -d

# View logs
docker-compose -f docker-compose.staging.yml logs -f app

# Stop
docker-compose -f docker-compose.staging.yml down
```

### Production

```bash
# Build
mvn clean package -DskipTests

# Run with production profile
java -Dspring.profiles.active=prod -jar target/upsc-backend-0.0.1-SNAPSHOT.jar

# Or use Docker
docker build -t upsc-backend:latest .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-user \
  -e DB_PASSWORD=your-password \
  upsc-backend:latest
```

---

## Health Checks

Each environment exposes health check endpoints:

```bash
# Development
curl http://localhost:8080/actuator/health

# Staging
curl https://staging-api.upsc-ai.com/actuator/health

# Production
curl https://api.upsc-ai.com/actuator/health
```

---

## Database Setup

### Development
No setup needed - H2 runs in-memory.

### Staging/Production

1. **Create Database:**
```sql
CREATE DATABASE upsc_ai_staging;
CREATE USER upsc_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE upsc_ai_staging TO upsc_user;
```

2. **Update Environment Variables:**
```bash
export DB_HOST=your-postgres-host
export DB_NAME=upsc_ai_staging
export DB_USERNAME=upsc_user
export DB_PASSWORD=secure_password
```

---

## Troubleshooting

### Wrong Profile Active

```bash
# Check active profile
curl http://localhost:8080/actuator/info | jq '.app.environment'

# Force profile
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Database Connection Issues

```bash
# Test PostgreSQL connection
psql -h localhost -U postgres -d upsc_ai

# Check logs
tail -f logs/application.log | grep -i "database\|connection"
```

### Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or use different port
export SERVER_PORT=8081
mvn spring-boot:run
```

---

## Best Practices

1. **Never commit `.env` files** - Add to `.gitignore`
2. **Use secrets manager** in production (AWS Secrets Manager, etc.)
3. **Rotate secrets regularly** (JWT secret, database passwords)
4. **Use different credentials** for each environment
5. **Monitor logs** in staging/production
6. **Test in staging** before deploying to production
7. **Keep profiles synchronized** with infrastructure

---

## Next Steps

1. ✅ Set up development environment
2. ⏳ Configure staging environment (when ready)
3. ⏳ Set up production environment (when ready)
4. ⏳ Configure CI/CD pipeline
5. ⏳ Set up monitoring and alerting
